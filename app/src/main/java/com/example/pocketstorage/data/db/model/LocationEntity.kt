package com.example.pocketstorage.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pocketstorage.core.utils.UNDEFINED_ID
import com.example.pocketstorage.domain.model.Location

@Entity(tableName = "location_table")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = UNDEFINED_ID,
    val name: String,
    val index: String,
    val address: String
)

fun LocationEntity.toLocation(): Location {
    return Location(
        id = this.id,
        name = this.name,
        index = this.index,
        address = this.address
    )
}

fun Location.toLocationEntity(): LocationEntity {
    return LocationEntity(
        id = this.id,
        name = this.name,
        index = this.index,
        address = this.address
    )
}