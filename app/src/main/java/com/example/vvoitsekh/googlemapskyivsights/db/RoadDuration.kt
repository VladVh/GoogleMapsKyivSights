package com.example.vvoitsekh.googlemapskyivsights.db

import android.arch.persistence.room.*
import com.google.maps.model.LatLng


/**
 * Created by Vlad on 28.04.2018.
 */

@Entity(tableName = "Durations")
@TypeConverters(DirectionsConverter::class)
data class RoadDuration(
        @PrimaryKey(autoGenerate = true) var id: Long?,
        @ColumnInfo(name = "from") var from: Int,
        @ColumnInfo(name = "to") var to: Int,
        @ColumnInfo(name = "duration") var duration: Long,
        @ColumnInfo(name = "directions") var directions: DirectionPolyline) {

    override fun toString(): String {
        return "$id!$from!$to!$duration!${DirectionsConverter().fromDirections(directions)})"
    }
}

