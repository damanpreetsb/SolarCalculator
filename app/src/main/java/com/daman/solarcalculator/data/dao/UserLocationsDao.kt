package com.daman.solarcalculator.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.daman.solarcalculator.data.entities.UserLocations

@Dao
interface UserLocationsDao {
    @Query("SELECT * from USER_LOCATIONS")
    fun getAll(): List<UserLocations>

    @Query("SELECT * from USER_LOCATIONS where locality = :locality")
    fun get(locality: String): UserLocations?

    @Insert(onConflict = REPLACE)
    fun insert(homeFeed: UserLocations)

    @Query("DELETE from USER_LOCATIONS where locality = :locality")
    fun delete(locality: String)

    @Query("DELETE from USER_LOCATIONS")
    fun deleteAll()
}