package com.example.vvoitsekh.googlemapskyivsights

import com.google.android.gms.maps.model.LatLng

/**
 * Created by Vlad on 14.04.2018.
 */
data class Showplace(val name:String, private val lat:Double, private val lng:Double) {
    fun getPosition() = LatLng(lat, lng)
}