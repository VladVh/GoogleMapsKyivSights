package com.example.vvoitsekh.googlemapskyivsights.db

import android.arch.persistence.room.*
import android.content.Context

/**
 * Created by Vlad on 28.04.2018.
 */
@Database(entities = arrayOf(Route::class), version = 1)
@TypeConverters(ShowplaceConverter::class)
abstract class SightsDatabase : RoomDatabase() {

    abstract fun routeDao(): RouteDao
}