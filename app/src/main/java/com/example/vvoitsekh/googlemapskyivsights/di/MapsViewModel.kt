package com.example.vvoitsekh.googlemapskyivsights.di

import android.arch.lifecycle.ViewModel
import com.example.vvoitsekh.googlemapskyivsights.PlacesRepository
import com.example.vvoitsekh.googlemapskyivsights.Showplace
import com.google.android.gms.maps.model.MarkerOptions
import javax.inject.Inject

/**
 * Created by Vlad on 14.04.2018.
 */
class MapsViewModel @Inject constructor(val mRepository: PlacesRepository): ViewModel() {

    private var placesOfInterest: List<Showplace> = mRepository.getPlaces()

    fun getMarkers(): List<MarkerOptions> {
        return placesOfInterest.map { item -> MarkerOptions().position(item.getPosition()).title(item.name) }
    }
}