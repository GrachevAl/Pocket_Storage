package com.example.pocketstorage

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun CreateBuilding() {
    ScaffoldWithTopBar()
}


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
@Preview(showBackground = true)
fun ScaffoldWithTopBar() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Create Building")
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                }
            )
        },
        content = { paddingValues -> // Добавлены paddingValues
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(start = 24.dp, top = paddingValues.calculateTopPadding(), end = 24.dp)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(24.dp)) // Добавлен Spacer с высотой 24dp
                Column(horizontalAlignment = Alignment.CenterHorizontally){

                    TextFieldCreateBuilding(
                        modifier = Modifier
                            .fillMaxWidth().padding(bottom = 8.dp),
                        label = {
                            Text(
                                text = "name",
                                color = colorResource(id = R.color.gray)
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = colorResource(id = R.color.blue),
                            unfocusedBorderColor = colorResource(id = R.color.gray)
                        )
                    )
                    TextFieldCreateBuilding(
                        modifier = Modifier
                            .fillMaxWidth().padding(bottom = 8.dp),
                        label = {
                            Text(
                                text = "address",
                                color = colorResource(id = R.color.gray)
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = colorResource(id = R.color.blue),
                            unfocusedBorderColor = colorResource(id = R.color.gray)
                        )
                    )
                    TextFieldCreateBuilding(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = {
                            Text(
                                text = "short code (ex: MSK-1)",
                                color = colorResource(id = R.color.gray)
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = colorResource(id = R.color.blue),
                            unfocusedBorderColor = colorResource(id = R.color.gray)
                        )
                    )

                    ButtonSaveBuilding {
                        //Click
                    }
                }
            }

        }
    )
}

@Composable
fun ButtonSaveBuilding(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple_500)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(top = 200.dp)
    ) {
        Text(text = "Save", color = Color.White)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldCreateBuilding(
    modifier: Modifier,
    label: @Composable () -> Unit,
    colors: TextFieldColors
) {
    var textId by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        modifier = modifier,
        value = textId,
        onValueChange = { textId = it },
        label = label,
        colors = colors
    )
}