package com.example.vvoitsekh.googlemapskyivsights

import com.example.vvoitsekh.googlemapskyivsights.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.HasActivityInjector

/**
 * Created by v.voitsekh on 05.04.2018.
 */
class MyApplication : DaggerApplication(), HasActivityInjector {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val appComponent  = DaggerAppComponent.builder().application(this).build()
        appComponent.inject(this)
        return appComponent
    }
}