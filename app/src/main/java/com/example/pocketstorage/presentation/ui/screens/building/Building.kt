package com.example.pocketstorage.presentation.ui.screens.building


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pocketstorage.R
import com.example.pocketstorage.domain.model.Location
import com.example.pocketstorage.presentation.ui.screens.building.viewmodel.BuildingViewModel


@Composable
fun Building(onClick: () -> Unit) {
    BuildingScreen(onClick)
}

@Preview(showBackground = true)
@Composable
fun PreviewBuildingScreen() {
    BuildingScreen(onClick = {})
}

@Composable
fun BuildingScreen(onClick: () -> Unit) {
    val viewModel = hiltViewModel<BuildingViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(true) {
        viewModel.refreshLocations()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .padding(top = 56.dp, start = 24.dp, bottom = 24.dp, end = 24.dp)
                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                TextFieldSearchBuildingName(
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "building",
                            color = colorResource(id = R.color.SpanishGrey)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "SearchById"
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.RetroBlue),
                        unfocusedBorderColor = colorResource(id = R.color.SpanishGrey),
                    ),
                    value = state.searchText,
                    onValueChange = viewModel::onSearchTextChange
                )
            }
        }

        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = 24.dp, bottom = 16.dp),
        ) {
            ButtonBuildingScreen(
                modifier = Modifier.wrapContentWidth(),
                rowContent = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add building",
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    Text(text = "Add building", fontSize = 16.sp)
                },
                onClick = {
                    onClick()
                }
            )
        }
        renderScreen(uiState)
    }
}

@Composable
private fun renderScreen(uiState: BuildingUiState) {
    when (val currentState = uiState) {
        is BuildingUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        is BuildingUiState.Success -> {
            if (currentState.isEmpty()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(text = "No locations",modifier = Modifier.align(Alignment.Center))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp)
                        .background(Color.White)
                ) {
                    items(currentState.locations) { locations ->
                        ListRowBuilding(model = locations)
                    }
                }
            }
        }
    }
}


@Composable
fun TextFieldSearchBuildingName(
    modifier: Modifier,
    label: @Composable () -> Unit,
    leadingIcon: @Composable () -> Unit,
    colors: TextFieldColors,
    value: String,
    onValueChange: (String) -> Unit

) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange, // тоже можно использовать такую конструкцию onValueChange = { newText-> viewModel.onSearchTextChange(newText) }
        label = label,
        leadingIcon = leadingIcon,
        colors = colors
    )
}


@Composable
fun ButtonBuildingScreen(
    modifier: Modifier,
    rowContent: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.RetroBlue)),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.wrapContentWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            rowContent()
        }
    }
}

@Composable
fun ListRowBuilding(model: Location) {
    Column(
        modifier = Modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .wrapContentHeight()
            .fillMaxWidth()
            .background(colorResource(id = R.color.AdamantineBlue)),
    ) {
        Text(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp),
            text = model.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = model.index,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = Color.White
        )
        Text(
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
            text = model.address,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
            color = Color.White
        )
    }

}