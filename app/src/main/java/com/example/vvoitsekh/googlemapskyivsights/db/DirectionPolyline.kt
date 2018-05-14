package com.example.vvoitsekh.googlemapskyivsights.db

import android.arch.persistence.room.Entity
import com.google.maps.model.LatLng

/**
 * Created by Vlad on 13.05.2018.
 */
data class DirectionPolyline(val points: List<LatLng>)