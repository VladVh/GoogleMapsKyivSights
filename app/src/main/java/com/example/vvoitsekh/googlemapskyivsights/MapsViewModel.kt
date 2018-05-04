package com.example.vvoitsekh.googlemapskyivsights

import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import android.util.Log
import com.example.vvoitsekh.googlemapskyivsights.db.RoadDuration
import com.example.vvoitsekh.googlemapskyivsights.db.RoadDurationDao
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.errors.ApiException
import com.google.maps.model.TravelMode
import javax.inject.Inject

/**
 * Created by Vlad on 14.04.2018.
 */
class MapsViewModel @Inject constructor(val mRepository: PlacesRepository, private val mRouteDao: RoadDurationDao): ViewModel() {

    private var placesOfInterest: List<Showplace> = mRepository.getPlaces()

    fun getMarkers(): List<MarkerOptions> {
        return placesOfInterest.map { item -> MarkerOptions().position(item.getPosition()).title(item.name) }
    }

    fun checkDatabase() {
        if (mRouteDao.getAll().size != placesOfInterest.size * placesOfInterest.size) {
            mRouteDao.deleteAll()
            NetworkCall(mRouteDao).execute(placesOfInterest)
        }
    }

    fun generateRoutes(markerTitle: String) {
        var start = placesOfInterest.indexOf(placesOfInterest.find { it.name == markerTitle })

        val durations = mRouteDao.getAll()
        var adjacencyMatrix = Array<Array<Long>>(placesOfInterest.size, { i -> emptyArray() })
        for (elem in durations)
            adjacencyMatrix[elem.from][elem.to] = elem.duration

        var used = emptyArray<Boolean>()
        var minEdge = Array<Long>(placesOfInterest.size, { i -> Long.MAX_VALUE })
        minEdge[0] = 0
        
    }

    class NetworkCall(private var routeDao: RoadDurationDao) : AsyncTask<List<Showplace>, Void, List<RoadDuration>>() {
        override fun doInBackground(vararg params: List<Showplace>): List<RoadDuration> {
            val context = GeoApiContext.Builder().apiKey("AIzaSyDubh9cgDqSQNf671ruFcOnsSeVCcnbsPk").build()
            val places = params[0]
            val routes = ArrayList<RoadDuration>()
            try {
                val req = DistanceMatrixApi.newRequest(context)
                val pointsString = places.map { "${it.lat},${it.lng}"}.joinToString { "," }
                val trix = req.origins(pointsString)
                        .destinations(pointsString)
                        .mode(TravelMode.WALKING)
                        .language("en-US")
                        .await()
                for ((rowId, row) in trix.rows.withIndex()) {
                    for ((index,elem) in row.elements.withIndex()) {
                        routes.add(RoadDuration(
                                (row.elements.size * (rowId + 1) + index).toLong(),
                                rowId,
                                index,
                                elem.duration.inSeconds))
                    }
                }
            } catch (e: ApiException) {
                Log.e("ERROR", e.message)
            } catch (e: Exception) {
                Log.e("ERROR", e.message)
            }
            return routes
        }

        override fun onPostExecute(result: List<RoadDuration>) {
            for (route in result) {
                routeDao.insert(route)
            }
        }
    }
}