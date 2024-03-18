package com.example.pocketstorage.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pocketstorage.core.utils.UNDEFINED_ID
import com.example.pocketstorage.core.utils.getUniqueIdentifier
import com.example.pocketstorage.domain.model.Inventory

@Entity(tableName = "inventory_table")
data class InventoryEntity(
    @PrimaryKey
    val id: String = UNDEFINED_ID,
    val name: String,
    val barcode: Long,
    val description: String,
    val locationId: Long,
    val categoryId: Long,
    val pathToImage: String
)

fun InventoryEntity.toInventory(): Inventory {
    return Inventory(
        id = this.id,
        name = this.name,
        barcode = this.barcode,
        description = this.description,
        locationId = this.locationId,
        categoryId = this.categoryId,
        pathToImage = this.pathToImage
    )
}

fun Inventory.toInventoryEntity(): InventoryEntity {
    return InventoryEntity(
        id = getUniqueIdentifier(),
        name = this.name,
        barcode = this.barcode,
        description = this.description,
        locationId = this.locationId,
        categoryId = this.categoryId,
        pathToImage = this.pathToImage
    )
}
