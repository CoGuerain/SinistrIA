package com.example.myapplication

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BakingViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    fun sendPrompt(
        bitmaps: List<Bitmap>
    ) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentBuilder = content {
                    bitmaps.forEach { bitmap ->
                        image(bitmap)
                    }
                val prompt = "Voici des images concernant une ou plusieurs voitures. " +
                        "Tu vas comparer chaque photos entre elles selon la couleur, le modèle ou même la plaque pour savoir si c'est la même voiture sur toutes les photos. " +
                        "Si ce n'est pas la même voiture sur chaque photo tu me réponds juste \"Il ne s'agit pas de la meme voiture sur toutes les photos.\" puis plus rien après." +
                        "Si c'est la même voiture tu vas :  donne moi sa plaque et son modèle. Ensuite donne moi la liste des éléments qu'elles affichent et si ils sont endommagées. " +
                        "Si c'est endommagé évalue le coût de réparation même si ce n'est pas précis. Réponds moi selon ce pattern :" +
                        "- Piece : endommagé/pas endommagée -> Prix de réparation en euro. " +
                        "Ne rajoute pas d'autres choses je veux que se soit succint, pas de remarque de fin. Une fois ce pattern effectué je ne veux pas de remarque à la fin de ta réponse, laisse juste la liste du pattern. " +
                        "A la fin du pattern affiche moi la somme de tous les coûts de répartions avec ce nouveau pattern : - '*Somme des couts'."
                text(prompt)

                }

                val response = generativeModel.generateContent(contentBuilder)
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    fun resetState(){
        _uiState.value = UiState.Initial
    }
}