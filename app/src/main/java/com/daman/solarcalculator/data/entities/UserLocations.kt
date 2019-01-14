package com.daman.solarcalculator.data.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "USER_LOCATIONS")
data class UserLocations (
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @PrimaryKey @ColumnInfo(name = "locality") var locality: String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(locality)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserLocations> {
        override fun createFromParcel(parcel: Parcel): UserLocations {
            return UserLocations(parcel)
        }

        override fun newArray(size: Int): Array<UserLocations?> {
            return arrayOfNulls(size)
        }
    }
}