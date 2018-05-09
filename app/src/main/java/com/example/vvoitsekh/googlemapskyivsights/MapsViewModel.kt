package com.example.vvoitsekh.googlemapskyivsights

import android.arch.lifecycle.ViewModel
import android.location.Location
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
class MapsViewModel @Inject constructor(mRepository: PlacesRepository, private val mRouteDao: RoadDurationDao) : ViewModel() {

    private var placesOfInterest: List<Showplace> = mRepository.getPlaces()
    private val indexArray = Array<Int>(placesOfInterest.size + 1, { it -> it})
    val routes = ArrayList<Route>()

    fun getMarkers(): List<MarkerOptions> {
        return placesOfInterest.map { item -> MarkerOptions().position(item.getPosition()).title(item.name) }
    }

    fun getPlaces(): List<Showplace> = placesOfInterest

    fun checkDatabase() {
        if (mRouteDao.getAll().size != placesOfInterest.size * (placesOfInterest.size - 1)) {
            mRouteDao.deleteAll()
            NetworkDatasetCall(mRouteDao).execute(placesOfInterest)
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

    fun findLoops(result: List<Long>, limit: Long) {
        val start = placesOfInterest.size
        val durations = mRouteDao.getAll()
        var adjacencyMatrix = Array(placesOfInterest.size + 1, { LongArray(placesOfInterest.size + 1) })
        for (elem in durations)
            adjacencyMatrix[elem.from][elem.to] = elem.duration
        for ((index,len) in result.withIndex()) {
            adjacencyMatrix[index][placesOfInterest.size] = len
            adjacencyMatrix[placesOfInterest.size][index] = len
        }
        var curLen = 0L
        var currentPath = ArrayList<Int>()
        currentPath.add(start)
        for ((index,elem) in placesOfInterest.withIndex()) {
            curLen = adjacencyMatrix[index][start]

            var possibleRoutes: List<Int>? = null
            if (2 * curLen < limit) {
                currentPath.add(index)
                possibleRoutes = indexArray.filter { item -> !currentPath.contains(item) }
            }

            if (possibleRoutes != null && possibleRoutes.isNotEmpty())
                traverseNextLayer(start, index, curLen, limit, currentPath, possibleRoutes, adjacencyMatrix)

        }
    }

    private fun traverseNextLayer(start: Int, prev:Int, curLen: Long, limit: Long, currentPath: ArrayList<Int>,
                                  possibleRoutes: List<Int>, adjacencyMatrix: Array<LongArray>) {
        for (elem in possibleRoutes) {
            if (curLen + adjacencyMatrix[prev][elem] + adjacencyMatrix[elem][start] < limit) {
                currentPath.add(elem)
                routes.add(Route(currentPath.clone() as ArrayList<Int>,
                        curLen + adjacencyMatrix[prev][elem] + adjacencyMatrix[elem][start]))
                currentPath.remove(elem)
            }
        }
        for (elem in possibleRoutes) {
            currentPath.add(elem)
            val newLen = curLen + adjacencyMatrix[prev][elem]
            if (newLen + adjacencyMatrix[elem][start] < limit) {
                val newRoutes = indexArray.filter { item -> !currentPath.contains(item) }
                if (newRoutes.isNotEmpty())
                    traverseNextLayer(start, elem, newLen, limit, currentPath, newRoutes, adjacencyMatrix)
            }
            currentPath.remove(elem)

        }
    }



    class NetworkDatasetCall(private var routeDao: RoadDurationDao) : AsyncTask<List<Showplace>, Void, List<RoadDuration>>() {
        override fun doInBackground(vararg params: List<Showplace>): List<RoadDuration> {
            val context = GeoApiContext.Builder().apiKey("AIzaSyCwgJJ26wafmQdFLI6whLUExGCEBeL5aPA").build()
            val places = params[0]
            val routes = ArrayList<RoadDuration>()
            try {
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
                                        elem.duration.inSeconds))
                        }
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