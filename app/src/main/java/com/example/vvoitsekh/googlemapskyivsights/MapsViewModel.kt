package com.example.vvoitsekh.googlemapskyivsights

import android.arch.lifecycle.ViewModel
import android.location.Location
import android.os.AsyncTask
import android.util.Log
import com.example.vvoitsekh.googlemapskyivsights.db.DirectionPolyline
import com.example.vvoitsekh.googlemapskyivsights.db.RoadDuration
import com.example.vvoitsekh.googlemapskyivsights.db.RoadDurationDao
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.DirectionsApi
import com.google.maps.DistanceMatrixApi
import com.google.maps.GeoApiContext
import com.google.maps.errors.ApiException
import com.google.maps.model.TravelMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Vlad on 14.04.2018.
 */
class MapsViewModel @Inject constructor(mRepository: PlacesRepository, private val mRouteDao: RoadDurationDao) : ViewModel() {

    private var placesOfInterest: List<Showplace> = mRepository.getPlaces()
    private val indexArray = Array<Int>(placesOfInterest.size + 1, { it -> it })
    val routes = ArrayList<Route>()
    lateinit var currentLocation: LatLng

    fun getMarkers(): List<MarkerOptions> {
        return placesOfInterest.map { item -> MarkerOptions().position(item.getPosition()).title(item.name) }
    }

    fun getPlaces(): List<Showplace> = placesOfInterest

    fun checkDatabase() {
        if (mRouteDao.getAll().size != placesOfInterest.size * (placesOfInterest.size - 1)) {
            //mRouteDao.deleteAll()
            //NetworkDistanceCall(mRouteDao).execute(placesOfInterest)
            //NetworkDirectionsCall(mRouteDao).execute(placesOfInterest)
        }
    }

    fun generateRoutes(markerTitle: Location, limit: Long) {
        //var start = placesOfInterest.indexOf(placesOfInterest.find { it.name == markerTitle })

        val start = 0
        val durations = mRouteDao.getAll()
        var adjacencyMatrix = Array(placesOfInterest.size, { LongArray(placesOfInterest.size) })
        for (elem in durations)
            adjacencyMatrix[elem.from][elem.to] = elem.duration

        var used = Array<Boolean>(placesOfInterest.size, { it -> false })
        var minEdge = Array<Long>(placesOfInterest.size, { i -> Long.MAX_VALUE })
        val selEdge = Array<Int>(placesOfInterest.size, { i -> -1 })
        var pairs = Array(placesOfInterest.size, { ArrayList<Int>(placesOfInterest.size) })

        for (i in 0 until placesOfInterest.size) {
            var v = -1
            for (j in 0 until placesOfInterest.size) {
                if (!used[j] && (v == -1 || minEdge[j] < minEdge[v]))
                    v = j
            }

            used[v] = true
            for (t in 0 until placesOfInterest.size) {
                if (adjacencyMatrix[v][t] != 0L && adjacencyMatrix[v][t] < minEdge[t]) {
                    minEdge[t] = adjacencyMatrix[v][t]
                    selEdge[t] = v
                }
            }
        }

        for (i in 0 until placesOfInterest.size) {
            if (selEdge[i] != -1) {
                pairs[i].add(selEdge[i])
                pairs[selEdge[i]].add(i)
            }
        }

        var newRoute = ArrayList<Int>()
        newRoute.add(start)
        for (elem in pairs[start]) {
            newRoute.add(elem)
            findRoute(newRoute, adjacencyMatrix, pairs, start, elem, start, adjacencyMatrix[start][elem], limit)
            newRoute.remove(elem)
        }


    }

    private fun findRoute(route: ArrayList<Int>,
                          adjM: Array<LongArray>,
                          pairs: Array<ArrayList<Int>>,
                          prev: Int, current: Int, start: Int, currentLen: Long, maxLen: Long) {
        for (elem in pairs[current]) {
            if (elem != prev && (currentLen + adjM[current][elem] + adjM[elem][start]) < maxLen) {
                route.add(elem)
                val curTime = currentLen + adjM[current][elem]
                routes.add(Route(route.clone() as ArrayList<Int>, currentLen + adjM[current][elem] + adjM[elem][start]))
                findRoute(route, adjM, pairs, current, elem, start,
                        (curTime), maxLen)
                route.remove(elem)
            }
        }
    }

    fun findLoops(location: Location, result: List<Long>, limit: Long) {
        val start = placesOfInterest.size
        currentLocation = LatLng(location.latitude, location.longitude)

        val durations = mRouteDao.getAll()
        var adjacencyMatrix = Array(placesOfInterest.size + 1, { LongArray(placesOfInterest.size + 1) })
        for (elem in durations)
            adjacencyMatrix[elem.from][elem.to] = elem.duration
        for ((index, len) in result.withIndex()) {
            adjacencyMatrix[index][placesOfInterest.size] = len
            adjacencyMatrix[placesOfInterest.size][index] = len
        }
        routes.clear()

        var curLen = 0L
        var currentPath = ArrayList<Int>()
        currentPath.add(start)
        for ((index, elem) in placesOfInterest.withIndex()) {
            curLen = adjacencyMatrix[index][start]

            var possibleRoutes: List<Int>? = null
            if (2 * curLen < limit) {
                currentPath.add(index)
                possibleRoutes = indexArray.filter { item -> !currentPath.contains(item) }

                if (possibleRoutes.isNotEmpty())
                    traverseNextLayer(start, index, curLen, limit, currentPath, possibleRoutes, adjacencyMatrix)

                currentPath.remove(index)
            }
        }
    }

    private fun traverseNextLayer(start: Int, prev: Int, curLen: Long, limit: Long, currentPath: ArrayList<Int>,
                                  possibleRoutes: List<Int>, adjacencyMatrix: Array<LongArray>) {
        for (elem in possibleRoutes) {
            if (curLen + adjacencyMatrix[prev][elem] + adjacencyMatrix[elem][start] < limit
                    && !checkRouteIntersection(currentPath, elem)) {
                currentPath.add(elem)
                routes.add(Route(currentPath.clone() as ArrayList<Int>,
                        curLen + adjacencyMatrix[prev][elem] + adjacencyMatrix[elem][start]))

                val newLen = curLen + adjacencyMatrix[prev][elem]
                //if (newLen + adjacencyMatrix[elem][start] < limit) {
                val newRoutes = indexArray.filter { item -> !currentPath.contains(item) }
                if (newRoutes.isNotEmpty())
                    traverseNextLayer(start, elem, newLen, limit, currentPath, newRoutes, adjacencyMatrix)
                //}

                currentPath.remove(elem)
            }
        }
//        for (elem in possibleRoutes) {
//            currentPath.add(elem)
//            val newLen = curLen + adjacencyMatrix[prev][elem]
//            if (newLen + adjacencyMatrix[elem][start] < limit) {
//                val newRoutes = indexArray.filter { item -> !currentPath.contains(item) }
//                if (newRoutes.isNotEmpty())
//                    traverseNextLayer(start, elem, newLen, limit, currentPath, newRoutes, adjacencyMatrix)
//            }
//            currentPath.remove(elem)
//
//        }
    }

    private fun checkRouteIntersection(route: ArrayList<Int>, elem: Int): Boolean {
        if (route.size < 3)
            return false

        val start2 = LatLng(placesOfInterest[route.last()].lat, placesOfInterest[route.last()].lng)
        val end2 = LatLng(placesOfInterest[elem].lat, placesOfInterest[elem].lng)
        for (i in 1 until route.size - 2) {
            val start1 = LatLng(placesOfInterest[route[i]].lat, placesOfInterest[route[i]].lng)
            val end1 = LatLng(placesOfInterest[route[i + 1]].lat, placesOfInterest[route[i + 1]].lng)
            if (segmentIntersection(start1, end1, start2, end2))
                return true
        }
        val start1 = currentLocation
        val end1 = LatLng(placesOfInterest[route[1]].lat, placesOfInterest[route[1]].lng)
        if (segmentIntersection(start1, end1, start2, end2))
            return true

        return false
    }

    private inline fun intersect(a: Double, b: Double, c: Double, d: Double): Boolean {
        var a1 = a
        var b1 = b
        var c1 = c
        var d1 = d
        if (a > b) {
            var k = a1
            a1 = b1
            b1 = k
        }
        if (c > d) {
            var k = c1
            c1 = d1
            d1 = k
        }
        return maxOf(a1, c1) <= minOf(b1, d1)
    }

    private inline fun area(a: LatLng, b: LatLng, c: LatLng): Double {
        return (b.latitude - a.latitude) * (c.longitude - a.longitude) - (b.longitude - a.longitude) * (c.latitude - a.latitude)
    }

    private fun segmentIntersection(start1: LatLng, end1: LatLng, start2: LatLng, end2: LatLng): Boolean {
        return intersect(start1.latitude, end1.latitude, start2.latitude, end2.latitude) &&
                intersect(start1.longitude, end1.longitude, start2.longitude, end2.longitude) &&
                area(start1, end1, start2) * area(start1, end1, end2) <= 0 &&
                area(start2, end2, start1) * area(start2, end2, end1) <= 0
//        val dir1 = LatLng(end1.latitude - start1.latitude, end1.longitude - start1.longitude)
//        val dir2 = LatLng(end2.latitude - start2.latitude, end2.longitude - start2.longitude)
//        val a1 = -dir1.longitude
//        val b1 = dir1.latitude
//        val d1 = -(a1*start1.latitude + b1*start1.longitude)
//
//        val a2 = -dir2.longitude
//        val b2 = dir2.latitude
//        val d2 = -(a2*start2.latitude + b2*start2.longitude)
//
//        val seg1start = a2*start1.latitude + b2*start1.longitude + d2
//        val seg1end = a2*end1.latitude + b2*end1.longitude + d2
//
//        val seg2start = a1*start2.latitude + b1*start2.longitude + d1
//        val seg2end = a1*end2.latitude + b1*end2.longitude + d1
//
//        if (seg1start * seg1end >= 0 || seg2start * seg2end >= 0)
//            return false
//        return true
    }


    class NetworkDistanceCall(private var routeDao: RoadDurationDao) : AsyncTask<List<Showplace>, Void, List<RoadDuration>>() {
        override fun doInBackground(vararg params: List<Showplace>): List<RoadDuration> {
            val context = GeoApiContext.Builder().apiKey("AIzaSyCwgJJ26wafmQdFLI6whLUExGCEBeL5aPA").build()
            val places = params[0]
            var routes = ArrayList<RoadDuration>()
            try {
                routes = distanceMatrixApiRequest(places, context)

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

        private fun distanceMatrixApiRequest(places: List<Showplace>, context: GeoApiContext): ArrayList<RoadDuration> {
            val routes = ArrayList<RoadDuration>()
            val points = places.map { "${it.lat},${it.lng}" }
            for ((column, point) in points.withIndex()) {
                val req = DistanceMatrixApi.newRequest(context)
                val destinations = points.joinToString("|")
                val trix = req.origins(point)
                        .destinations(destinations)
                        .mode(TravelMode.WALKING)
                        .language("en-US")
                        .await()

                for (row in trix.rows) {
                    for ((index, elem) in row.elements.withIndex()) {
                        if (index != column)
                            routes.add(RoadDuration(
                                    (row.elements.size * (column + 1) + index).toLong(),
                                    column,
                                    index,
                                    elem.duration.inSeconds,
                                    DirectionPolyline(emptyList())))
                    }
                }
            }
            return routes
        }
    }

    class NetworkDirectionsCall(private var routeDao: RoadDurationDao) : AsyncTask<List<Showplace>, Void, List<RoadDuration>>() {
        override fun doInBackground(vararg params: List<Showplace>): ArrayList<RoadDuration> {
            val context = GeoApiContext.Builder()
                    .apiKey("AIzaSyCwgJJ26wafmQdFLI6whLUExGCEBeL5aPA")
                    .connectTimeout(3, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .build()
            val places = params[0]
            var routes = ArrayList<RoadDuration>()
            try {
                routes = directionsApiRequest(places, context)
            } catch (e: ApiException) {
                Log.e("ERROR", e.message)
            } catch (e: Exception) {
                Log.e("ERROR", e.message)
            }
            return routes
        }

        private fun directionsApiRequest(places: List<Showplace>, context: GeoApiContext): ArrayList<RoadDuration> {
            val routes = ArrayList<RoadDuration>()
            val points = places.map { "${it.lat},${it.lng}" }
            for ((row, point) in points.withIndex()) {
                for ((column, point2) in points.withIndex()) {
                    if(row != column) {
                        val req = DirectionsApi.newRequest(context)
                        val trix = req.origin(point)
                                .destination(point2)
                                .mode(TravelMode.WALKING)
                                .language("en-US")
                                .await()

                        for (elem in trix.routes) {
                            routes.add(RoadDuration(
                                    (points.size * (row + 1) + column).toLong(), row, column, -1, DirectionPolyline(elem.overviewPolyline.decodePath())))
                        }
                    }
                }
            }
            return routes
        }

        override fun onPostExecute(result: List<RoadDuration>) {
            val items = routeDao.getAll()
            for (route in result) {
                if (route.directions != null) {
                    routeDao.updateDurations(route.directions!!, route.id!!)
                }
            }
        }
    }
}