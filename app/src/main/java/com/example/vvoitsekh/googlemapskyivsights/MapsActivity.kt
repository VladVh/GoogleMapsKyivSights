package com.example.vvoitsekh.googlemapskyivsights

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.example.vvoitsekh.googlemapskyivsights.databinding.ActivityMapsBinding
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import dagger.android.AndroidInjection
import javax.inject.Inject

import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.example.vvoitsekh.googlemapskyivsights.db.RoadDurationDao
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.model.TravelMode
import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.errors.ApiException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mBinding: ActivityMapsBinding

    private var mSelectedMarker: Marker? = null
    private val mPolygons = ArrayList<Polygon>()

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

        mViewModel.checkDatabase()

        mBinding.listview.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val data = parent.getItemAtPosition(position) as Route
            val places = mViewModel.getPlaces().slice(data.points)
            val polygonOpt = PolygonOptions().addAll(places.map { LatLng(it.lat, it.lng) })

            mPolygons[0].remove()
            val polygon = mMap.addPolygon(polygonOpt)
            mPolygons.add(polygon)
        }

//        val values = arrayOf("Android", "iPhone", "WindowsMobile", "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2", "Android", "iPhone", "WindowsMobile")
//
//        val list = ArrayList<String>()
//        for (i in values.indices) {
//            list.add(values[i])
//        }
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
        mMap.setOnMarkerClickListener(this)
        // Add a marker in Sydney and move the camera

        val kyiv = LatLng(50.44, 30.52)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Kyiv"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kyiv, 11F))

        val markers = mViewModel.getMarkers()
        for(marker in markers) {
            mMap.addMarker(marker)
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        mSelectedMarker = marker
        return true
    }


    fun findPaths(view: View) {
        if (mSelectedMarker != null) {
            if (mRouteDao.getAll().size == 0)
                Toast.makeText(this, "loading data", Toast.LENGTH_SHORT)
            else {
                mViewModel.generateRoutes(mSelectedMarker?.title!!, mBinding.editTime.text.toString().toLong().toSeconds())
                mViewModel.routes.sortBy { it.points.size }
                val routes = mViewModel.routes.take(10)

                val adapter = StableArrayAdapter(this,
                        R.layout.listview_item, routes.toTypedArray())
                mBinding.listview.adapter = adapter
                mBinding.listview.visibility = View.VISIBLE
            }
        }
    }

    private inner class StableArrayAdapter(context: Context, textViewResourceId: Int,
                                           objects: Array<Route>) : ArrayAdapter<Route>(context, textViewResourceId, objects) {

        internal var mIdMap = HashMap<Route, Int>()

        init {
            for (i in objects.indices) {
                mIdMap.put(objects[i], i)
            }
        }


        override fun getItemId(position: Int): Long {
            val item = getItem(position)
            return mIdMap[item]!!.toLong()
        }



        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val data = getItem(position)
            if (convertView == null) {
                val viewHolder = ViewHolder()
                var view = LayoutInflater.from(context).inflate(R.layout.listview_item, parent, false)
                viewHolder.points = view.findViewById(R.id.points)
                viewHolder.time = view.findViewById(R.id.time)
                viewHolder.points.text = "places: ${data.points.size}"
                viewHolder.time.text = "time: ${data.time.toMins()}"
                view.tag = viewHolder
                return view
            }
            return convertView
        }
    }

    private inner class ViewHolder {
        lateinit var points:TextView
        lateinit var time:TextView
    }
}

private fun Long.toSeconds(): Long {
    return this * 3600
}

private fun Long.toMins(): String {
    return "${this / 3600} hour, ${this % 3600} min "
}
