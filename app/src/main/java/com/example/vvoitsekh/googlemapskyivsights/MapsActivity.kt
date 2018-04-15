package com.example.vvoitsekh.googlemapskyivsights

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.vvoitsekh.googlemapskyivsights.databinding.ActivityMapsBinding
import com.example.vvoitsekh.googlemapskyivsights.di.MapsViewModel
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.android.AndroidInjection
import javax.inject.Inject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var mBinding: ActivityMapsBinding

    @Inject lateinit var mViewModel: MapsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_maps)
        mBinding.setLifecycleOwner(this)
        //setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera

        val kyiv = LatLng(50.44, 30.52)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Kyiv"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kyiv, 11F))

        val markers = mViewModel.getMarkers()
        for(marker in markers) {
            mMap.addMarker(marker)
        }
    }
}
