package com.example.vvoitsekh.googlemapskyivsights

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vvoitsekh.googlemapskyivsights.databinding.ActivityMapsBinding
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.LatLng
import dagger.android.AndroidInjection
import javax.inject.Inject

import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.vvoitsekh.googlemapskyivsights.db.RoadDuration
import com.example.vvoitsekh.googlemapskyivsights.db.RoadDurationDao
import com.google.android.gms.maps.model.Marker
import com.google.maps.model.TravelMode
import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.errors.ApiException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mBinding: ActivityMapsBinding

    private var mSelectedMarker: Marker? = null

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

        val context = GeoApiContext.Builder().apiKey("AIzaSyDubh9cgDqSQNf671ruFcOnsSeVCcnbsPk").build()
        try {
            val req = DistanceMatrixApi.newRequest(context)
            val trix = req.origins("50.452778,30.514444")
                    .destinations("50.434167,30.559167", "50.45,30.524167")
                    .mode(TravelMode.WALKING)
                    .language("en-US")
                    .await()
//            for (row in trix.rows) {
//                for ((index,elem) in row.elements.withIndex()) {
//                    Log.d("results", elem.duration.humanReadable)
//                    mRouteDao.insert(
//                            RoadDuration(index.toLong(), mViewModel.mRepository.getPlaces()[0], mViewModel.mRepository.getPlaces()[index], elem.duration.toString()))
//                }
//            }
            //Do something with result here
            // ....
        } catch (e: ApiException) {
            Log.e("ERROR", e.message)
        } catch (e: Exception) {
            Log.e("ERROR", e.message)
        }


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

    override fun onMarkerClick(marker: Marker?): Boolean {
        mSelectedMarker = marker
        return true
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
