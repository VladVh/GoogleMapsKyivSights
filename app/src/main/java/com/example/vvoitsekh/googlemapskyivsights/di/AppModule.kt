package com.example.vvoitsekh.googlemapskyivsights.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.example.vvoitsekh.databindinglivedata.di.ViewModelModule
import com.example.vvoitsekh.googlemapskyivsights.PlacesRepository
import com.example.vvoitsekh.googlemapskyivsights.db.SightsDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by v.voitsekh on 12.01.2018.
 */
@Module(includes = arrayOf(ViewModelModule::class))
class AppModule {

    @Singleton
    @Provides
    fun providePlacesRepository() = PlacesRepository()

    @Singleton
    @Provides
    fun provideDb(application: Application) =
            Room.databaseBuilder(application, SightsDatabase::class.java, "sights.db")
                    //.addMigrations(SightsDatabase.MIGRATION_3_4)
                    .allowMainThreadQueries().build()
//
    @Singleton
    @Provides
    fun provideRouteDao(database: SightsDatabase) = database.routeDao()

    @Singleton
    @Provides
    fun provideContext(application: Application): Context = application

}