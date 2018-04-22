package com.example.vvoitsekh.googlemapskyivsights

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vvoitsekh.googlemapskyivsights.databinding.ActivityMapsBinding
import com.example.vvoitsekh.googlemapskyivsights.di.MapsViewModel
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.android.AndroidInjection
import javax.inject.Inject
import com.example.vvoitsekh.googlemapskyivsights.R.id.listview

import android.widget.ArrayAdapter
import android.widget.TextView


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

        val values = arrayOf("Android", "iPhone", "WindowsMobile", "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2", "Android", "iPhone", "WindowsMobile")

        val list = ArrayList<String>()
        for (i in values.indices) {
            list.add(values[i])
        }
        val adapter = StableArrayAdapter(this,
                R.layout.listview_item, list)
        mBinding.listview.adapter = adapter
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

    fun findPaths(view: View) {
        mBinding.listview.visibility = View.VISIBLE
    }

    private inner class StableArrayAdapter(context: Context, textViewResourceId: Int,
                                           objects: List<String>) : ArrayAdapter<String>(context, textViewResourceId, objects) {

        internal var mIdMap = HashMap<String, Int>()

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
                viewHolder.point = view.findViewById(R.id.points)
                viewHolder.point.text = data
                view.tag = viewHolder
                return view
            }
            return convertView
        }
    }

    private inner class ViewHolder {
        lateinit var point:TextView
    }
}
