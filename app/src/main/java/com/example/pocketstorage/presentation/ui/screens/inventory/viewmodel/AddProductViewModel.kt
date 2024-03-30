package com.example.pocketstorage.presentation.ui.screens.inventory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketstorage.domain.model.Category
import com.example.pocketstorage.domain.model.Inventory
import com.example.pocketstorage.domain.model.Location
import com.example.pocketstorage.domain.usecase.db.GetCategoriesByBuildingIdUseCase
import com.example.pocketstorage.domain.usecase.db.GetLocationsUseCase
import com.example.pocketstorage.domain.usecase.db.InsertInventoryUseCase
import com.example.pocketstorage.presentation.ui.screens.inventory.CreateProductEvent
import com.example.pocketstorage.presentation.ui.screens.inventory.InventoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val getLocationsUseCase: GetLocationsUseCase,
    private val getCategoriesByBuildingIdUseCase: GetCategoriesByBuildingIdUseCase,
    private val insertInventoryUseCase: InsertInventoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(InventoryUiState())
    val state = _state.asStateFlow()


    fun event(createProductEvent: CreateProductEvent) {
        when (createProductEvent) {
            is CreateProductEvent.CreateBuilding -> {
                val name = state.value.name
                val description = state.value.description
                val locationId = state.value.locationId
                val categoryId = state.value.categoryId
                val pathToImage = state.value.pathToImage

                if (name.isBlank() || description.isBlank() || locationId.isNullOrBlank()
                    || categoryId.isNullOrBlank() || pathToImage.isNullOrBlank()) {
                    return
                }

                val product = Inventory(
                    name = name,
                    description = description,
                    locationId = locationId,
                    categoryId = categoryId,
                    pathToImage = pathToImage
                )
                viewModelScope.launch {
                    insertInventoryUseCase.invoke(product)
                    _state.update {
                        it.copy(
                            name = "",
                            description = "",
                            locationId = "",
                            categoryId = "",
                            pathToImage = ""
                        )
                    }
                }

            }

            is CreateProductEvent.SetNameProduct -> {
                _state.update {
                    it.copy(
                        name = createProductEvent.nameProduct
                    )
                }

            }

            is CreateProductEvent.SetDescription -> {
                _state.update {
                    it.copy(
                        description = createProductEvent.description
                    )
                }
            }

            is CreateProductEvent.SetCategoryId -> {
                _state.update {
                    it.copy(
                        categoryId = createProductEvent.categoryId
                    )
                }
            }

            is CreateProductEvent.SetLocationId -> {
                _state.update {
                    it.copy(
                        locationId = createProductEvent.locationId
                    )
                }
            }

            is CreateProductEvent.SetPathToImage -> {
                _state.update {
                    it.copy(
                        pathToImage = createProductEvent.pathToImage
                    )
                }
            }

            is CreateProductEvent.ShowListBuilding ->{
                viewModelScope.launch {
                    getLocationsUseCase()
                        .collect { locations ->
                            _state.update {
                                it.copy(locations = locations)
                            }
                        }
                }
            }

            is CreateProductEvent.ShowListCategory ->{
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            selectedIdBuilding = createProductEvent.locationId
                        )
                    }
                    val buildingId = _state.value.selectedIdBuilding
                    getCategoriesByBuildingIdUseCase.invoke(buildingId)
                        .collect { category ->
                            _state.update {
                                it.copy(
                                    categories = category
                                )
                            }
                        }
                }
            }

            else -> {}
        }
    }

}