package com.example.vvoitsekh.googlemapskyivsights.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.example.vvoitsekh.databindinglivedata.di.ViewModelModule
import com.example.vvoitsekh.googlemapskyivsights.PlacesRepository
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
    fun provideDb() = PlacesRepository()
//    @Singleton
//    @Provides
//    fun provideDb(context: Context) = TasksDatabase.buildDatabase(context)
//
//    @Singleton
//    @Provides
//    fun provideTasksDao(database: TasksDatabase) = database.taskDao()

    @Singleton
    @Provides
    fun provideContext(application: Application): Context = application

}