package com.example.myapplication

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BakingScreen(
    bakingViewModel: BakingViewModel = viewModel()
) {
    var result by remember { mutableStateOf("") }
    var somme by remember { mutableStateOf("") }
    var selectedBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    val uiState by bakingViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val pickImages = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri>? ->
            uris?.let {
                selectedBitmaps = it.mapNotNull { uri ->
                    try {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()), // Entire middle section scrollable
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Image section
            if (selectedBitmaps.isEmpty()) {
                Spacer(modifier = Modifier.weight(0.4f))
                IconButton(
                    onClick = {
                        pickImages.launch("image/*")
                    },
                    modifier = Modifier.size(100.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAPhoto,
                        contentDescription = "Sélectionner des images depuis la galerie",
                        modifier = Modifier.size(70.dp),
                        tint = Color(0xFF3D5A73)

                    )
                }
                Spacer(modifier = Modifier.weight(0.6f))
            } else {
                Spacer(modifier = Modifier.weight(0.4f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp) // Fixed max height for scrollable area
                        .padding(horizontal = 16.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedBitmaps) { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .width(150.dp)
                            )
                        }
                    }
                }
                if(result.isEmpty()){
                    Spacer(modifier = Modifier.weight(0.6f))
                }else{
                    Spacer(modifier = Modifier.weight(0.12f))
                }
            }


            // Result Section
            if (uiState is UiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                if (uiState is UiState.Error) {
                    result = (uiState as UiState.Error).errorMessage
                } else if (uiState is UiState.Success) {
                    var temp = (uiState as UiState.Success).outputText
                    if(temp == "Il ne s'agit pas de la même voiture sur toutes les photos.\n"){
                        result = temp
                        somme = ""
                    }else{
                        result = temp.substring(0, temp.length - 3).substringBeforeLast("€\n") + "€"
                        somme =(uiState as UiState.Success).outputText.substringAfterLast(": ")
                            .substringBeforeLast("\n")
                    }

                }
                if (result.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .padding(32.dp)
                            .verticalScroll(rememberScrollState())
                            .align(Alignment.CenterHorizontally),
                    ){
                        Column {
                            Text(
                                text = result,
                                textAlign = TextAlign.Start,
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(7.dp)
                            )

                        }
                    }
                    if(somme.isNotEmpty()){
                        if(result.isNotEmpty()){
                            Spacer(modifier = Modifier.weight(0.02f))
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF3D5A73),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .fillMaxWidth()
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = somme,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                        if(result.isNotEmpty()){
                            Spacer(modifier = Modifier.weight(0.06f))
                        }else{
                            Spacer(modifier = Modifier.weight(0.08f))
                        }
                    }else{
                        Spacer(modifier = Modifier.weight(0.2f))
                    }



                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if(result.isNotEmpty()){
                Button(
                    onClick = {
                        bakingViewModel.sendPrompt(selectedBitmaps)
                    },
                    enabled = selectedBitmaps.isNotEmpty(),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(text = stringResource(R.string.action_ask_ai))
                }

                Button(
                    onClick = {
                        result = "";
                        somme = "";
                        selectedBitmaps = emptyList()
                        bakingViewModel.resetState()
                    },
                    modifier = Modifier
                ) {
                    Text(text = stringResource(R.string.reset_button))
                }
            }else{
                Button(
                    onClick = {
                        bakingViewModel.sendPrompt(selectedBitmaps)
                    },
                    enabled = selectedBitmaps.isNotEmpty(),
                    modifier = Modifier
                ) {
                    Text(text = stringResource(R.string.action_ask_ai))
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showSystemUi = true)
@Composable
fun BakingScreenPreview() {
    BakingScreen()
}