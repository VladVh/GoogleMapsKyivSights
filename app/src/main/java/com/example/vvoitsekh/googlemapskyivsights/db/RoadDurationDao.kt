package com.example.vvoitsekh.googlemapskyivsights.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.google.maps.model.LatLng

/**
 * Created by Vlad on 28.04.2018.
 */
@Dao
interface RoadDurationDao {

    @Query("Select * from Durations")
    fun getAll(): List<RoadDuration>

    @Insert(onConflict = REPLACE)
    fun insert(roadDuration: RoadDuration)

    @Query("Update Durations SET directions= :points WHERE id = :sid")
    fun updateDurations(points: DirectionPolyline, sid: Long)

    @Query("Update Durations SET duration= :newDuration WHERE id= :sid")
    fun updateTime(newDuration: Long, sid:Long)

    @Query("Delete from Durations")
    fun deleteAll()
}