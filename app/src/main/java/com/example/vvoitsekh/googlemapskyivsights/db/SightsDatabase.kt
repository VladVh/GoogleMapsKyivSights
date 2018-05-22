package com.example.vvoitsekh.googlemapskyivsights.db

import android.arch.persistence.room.*
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import com.google.maps.model.LatLng


/**
 * Created by Vlad on 28.04.2018.
 */
@Database(entities = arrayOf(RoadDuration::class), version = 1)
@TypeConverters(DirectionsConverter::class)
abstract class SightsDatabase : RoomDatabase() {

    abstract fun routeDao(): RoadDurationDao

    companion object {
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Durations " + " ADD COLUMN directions TEXT NOT NULL DEFAULT \" \"")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Durations " + " RENAME COLUMN Durations.from to fromId")
            }
        }
    }
}

class DirectionsConverter{

    @TypeConverter
    fun fromDirections(value: DirectionPolyline?): String? {
        return value?.points?.joinToString("|") { it -> "${it.lat},${it.lng}" }
        //return StringBuilder().append(value?.name).append("%").append(value?.lat).append("%").append(value?.lng).toString()
    }

    @TypeConverter
    fun fromString(value: String?): DirectionPolyline? {
        var coordinates = ArrayList<LatLng>()
        if (!value.isNullOrBlank()) {
            val values = value?.split('|')
            values?.forEach {
                var latlng = it.split(',')
                coordinates.add(LatLng(latlng[0].toDouble(), latlng[1].toDouble()))
            }
            return DirectionPolyline(coordinates)
        }
        return DirectionPolyline(emptyList())
    }
}
