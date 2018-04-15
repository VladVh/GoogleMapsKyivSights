package com.example.vvoitsekh.googlemapskyivsights.di

import com.example.vvoitsekh.googlemapskyivsights.MapsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by v.voitsekh on 12.01.2018.
 */


@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector
    abstract fun bindActivity(): MapsActivity

}