package com.example.vvoitsekh.databindinglivedata.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.example.vvoitsekh.googlemapskyivsights.ViewModelFactory
import com.example.vvoitsekh.googlemapskyivsights.MapsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by v.voitsekh on 16.01.2018.
 */
@Module
abstract class ViewModelModule {
//    @Binds
//    @IntoMap
//    @ViewModelKey(TasksViewModel::class)
//    abstract fun bindTaskViewModel(viewModel: TasksViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MapsViewModel::class)
    abstract fun bindTaskViewModel(viewModel: MapsViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.NewInstanceFactory
}