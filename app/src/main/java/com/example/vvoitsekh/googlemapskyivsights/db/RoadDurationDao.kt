package com.example.vvoitsekh.googlemapskyivsights.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

/**
 * Created by Vlad on 28.04.2018.
 */
@Dao
interface RoadDurationDao {

    @Query("Select * from Durations")
    fun getAll(): List<RoadDuration>

    @Insert(onConflict = REPLACE)
    fun insert(roadDuration: RoadDuration)

    @Query("Delete from Durations")
    fun deleteAll()
}