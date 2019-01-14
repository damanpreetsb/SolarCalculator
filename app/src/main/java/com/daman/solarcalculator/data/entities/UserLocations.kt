package com.daman.solarcalculator.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "USER_LOCATIONS")
data class UserLocations (
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @PrimaryKey @ColumnInfo(name = "locality") var locality: String
)