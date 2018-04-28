package com.example.vvoitsekh.googlemapskyivsights.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

/**
 * Created by Vlad on 28.04.2018.
 */
@Dao
interface RouteDao {

    @Query("Select * from Distances")
    fun getAll(): List<Route>

    @Insert(onConflict = REPLACE)
    fun insert(route: Route)

    @Query("Delete from Distances")
    fun deleteAll()
}