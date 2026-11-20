package com.example.myapplication.routedrop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.HttpURLConnection
import java.net.URL

data class RouteInfo(
    val distanceKm: Double,
    val durationMinutes: Int,
    val geoPoints: List<GeoPoint>
)

interface RouteProvider {
    suspend fun getRoute(
        startLat: Double, startLng: Double,
        endLat: Double, endLng: Double
    ): RouteInfo?
}

object RoutingHelper : RouteProvider {
    override suspend fun getRoute(
        startLat: Double, startLng: Double,
        endLat: Double, endLng: Double
    ): RouteInfo? = withContext(Dispatchers.IO) {
        try {
            // OSRM API requires coordinates in Longitude,Latitude order
            val urlStr = "https://router.project-osrm.org/route/v1/driving/$startLng,$startLat;$endLng,$endLat?overview=full&geometries=geojson"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            // Required: Identify our app to OSRM to comply with free-tier rules
            connection.setRequestProperty("User-Agent", "RouteDrop/1.0 (donnamaepbautista@gmail.com)")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val routes = json.getJSONArray("routes")
                
                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    
                    // Parse precise distance and duration from OSRM
                    val distance = route.getDouble("distance") / 1000.0 // meters to km
                    val duration = (route.getDouble("duration") / 60.0).toInt() // seconds to min
                    
                    // Parse the GeoJSON Polyline array for drawing on the map
                    val geometry = route.getJSONObject("geometry")
                    val coordinates = geometry.getJSONArray("coordinates")
                    
                    val geoPoints = mutableListOf<GeoPoint>()
                    for (i in 0 until coordinates.length()) {
                        val coord = coordinates.getJSONArray(i)
                        val lon = coord.getDouble(0)
                        val lat = coord.getDouble(1)
                        geoPoints.add(GeoPoint(lat, lon))
                    }
                    
                    return@withContext RouteInfo(distance, duration, geoPoints)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
