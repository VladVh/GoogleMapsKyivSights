package com.example.vvoitsekh.googlemapskyivsights.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.TypeConverter
import com.example.vvoitsekh.googlemapskyivsights.Showplace


/**
 * Created by Vlad on 28.04.2018.
 */

@Entity(tableName = "Durations")
data class RoadDuration(
        @PrimaryKey(autoGenerate = true) var id: Long?,
        @ColumnInfo(name = "from") var from: Int,
        @ColumnInfo(name = "to") var to: Int,
        @ColumnInfo(name = "duration") var duration: Long)

//class ShowplaceConverter{
//    @TypeConverter
//    fun fromShowplace(value: Showplace?):String? {
//        return StringBuilder().append(value?.name).append("%").append(value?.lat).append("%").append(value?.lng).toString()
//    }
//
//    @TypeConverter
//    fun fromString(value: String):Showplace? {
//        val values = value.split('%')
//        return Showplace(values[0], values[1].toDouble(), values[2].toDouble())
//    }
//}
