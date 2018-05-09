package com.example.vvoitsekh.googlemapskyivsights

import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.vvoitsekh.googlemapskyivsights.databinding.ActivityMapsBinding
import com.example.vvoitsekh.googlemapskyivsights.db.RoadDurationDao
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.tasks.Task
import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.errors.ApiException
import com.google.maps.model.TravelMode
import dagger.android.AndroidInjection
import javax.inject.Inject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val TAG = MapsActivity::class.java.toString()
    private val mDefaultLocation = LatLng(50.44, 30.52)

    private lateinit var mMap: GoogleMap
    private lateinit var mBinding: ActivityMapsBinding
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private val mPolygons = ArrayList<Polygon>()
    private var mLocationPermissionGranted = false
    private var mRequestingLocation = false
    private var mLastKnownLocation: Location? = null

    @Inject lateinit var mViewModel: MapsViewModel
    @Inject lateinit var mRouteDao: RoadDurationDao

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

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mViewModel.checkDatabase()

        mBinding.listview.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val data = parent.getItemAtPosition(position) as Route
            val places = mViewModel.getPlaces().slice(data.points)
            val polygonOpt = PolygonOptions().addAll(places.map { LatLng(it.lat, it.lng) })

            if (mPolygons.isNotEmpty()) {
                val deleted = mPolygons.removeAt(0)
                deleted.remove()
            }

            val polygon = mMap.addPolygon(polygonOpt)
            mPolygons.add(polygon)
        }

        //startLocationUpdates()

    }

    private fun updateLocationUI() {
        try {
            if (mLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                val loc = locationResult
                locationResult.addOnCanceledListener {
                    Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show()
                }
                locationResult.addOnFailureListener {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
                locationResult.addOnCompleteListener(this, {
                    @Override
                    fun onComplete(task: Task<Location>) {
                        if (task.isSuccessful) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.result

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    LatLng(mLastKnownLocation?.latitude!!,
                                            mLastKnownLocation?.longitude!!), 12f))
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Log.e(TAG, "Exception: %s", task.exception)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 12f))
                            mMap.uiSettings.isMyLocationButtonEnabled = false
                        }
                    }
                })
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }


    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
            updateLocationUI()
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mLocationPermissionGranted = true
        }
        updateLocationUI()
        startLocationUpdates()
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
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Kyiv"))
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 11F))

        updateLocationUI()

        //getDeviceLocation()

        val markers = mViewModel.getMarkers()
        for (marker in markers) {
            mMap.addMarker(marker)
        }
    }

    private fun startLocationUpdates() {
        if (!mLocationPermissionGranted)
            getLocationPermission()
        else
            mRequestingLocation = true
        // Create the location request to start receiving updates
        val request = createLocationRequest()
        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(request)
        val locationSettingsRequest = builder.build()
        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        try {
            mFusedLocationProviderClient.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    // do work here
                    onLocationChanged(locationResult.lastLocation)
                }
            }, Looper.myLooper())
        } catch (ex: SecurityException) {
            Log.e(TAG, "request location updates failed: " + ex.message)
            mRequestingLocation = false
        }
    }

    private fun onLocationChanged(location: Location?) {
        mLastKnownLocation = location

    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest().apply {
            interval = 10000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }


    override fun onMyLocationButtonClick(): Boolean {
        return true
    }


    fun findPaths(view: View) {
        mLastKnownLocation?.apply {
            if (mRouteDao.getAll().isEmpty())
                Toast.makeText(this@MapsActivity, "loading data", Toast.LENGTH_SHORT).show()
            else {
                NetworkPointCall(LatLng(mLastKnownLocation!!.latitude, mLastKnownLocation!!.longitude))
                        .execute(mViewModel.getPlaces())

//                mViewModel.generateRoutes(this, mBinding.editTime.text.toString().toLong().toSeconds())
//                mViewModel.routes.sortBy { it.points.size }
//                val routes = mViewModel.routes.take(10)
//
//                val adapter = StableArrayAdapter(this@MapsActivity,
//                        R.layout.listview_item, routes.toTypedArray())
//                mBinding.listview.adapter = adapter
//                mBinding.listview.visibility = View.VISIBLE
            }
        }
    }

    inner class NetworkPointCall(private var location: LatLng) : AsyncTask<List<Showplace>, Void, List<Long>>() {
        override fun doInBackground(vararg params: List<Showplace>): List<Long> {
            val context = GeoApiContext.Builder().apiKey("AIzaSyCwgJJ26wafmQdFLI6whLUExGCEBeL5aPA").build()
            val places = params[0]
            val routes = ArrayList<Long>()
            try {
                val points = places.map { "${it.lat},${it.lng}" }
                val req = DistanceMatrixApi.newRequest(context)
                val origin = "${location.latitude},${location.longitude}"
                val destinations = points.joinToString("|")
                val trix = req.origins(origin)
                        .destinations(destinations)
                        .mode(TravelMode.WALKING)
                        .language("en-US")
                        .await()

                for (elem in trix.rows[0].elements) {
                    routes.add(elem.duration.inSeconds)
                }
            } catch (e: ApiException) {
                Log.e("ERROR", e.message)
            } catch (e: Exception) {
                Log.e("ERROR", e.message)
            }
            return routes
        }

        override fun onPostExecute(result: List<Long>?) {
            if (result != null && result.isNotEmpty()) {
                mViewModel.findLoops(result, mBinding.editTime.text.toString().toLong().toSeconds())

                mViewModel.routes.sortBy { it.points.size }
                val routes = mViewModel.routes.take(10)

                val adapter = StableArrayAdapter(this@MapsActivity,
                        R.layout.listview_item, routes.toTypedArray())
                mBinding.listview.adapter = adapter
                mBinding.listview.visibility = View.VISIBLE
            }


        }
    }


    private fun Long.toSeconds(): Long {
        return this * 3600
    }
}
