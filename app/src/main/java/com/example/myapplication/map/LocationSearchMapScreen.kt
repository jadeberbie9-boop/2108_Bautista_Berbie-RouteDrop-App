package com.example.myapplication.map

import android.content.Context
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


data class SearchResult(
    val displayName: String,
    val lat: Double,
    val lon: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchMapScreen(
    modifier: Modifier = Modifier,
    defaultCenter: GeoPoint = GeoPoint(40.7128, -74.0060) // Default to NYC, adjust as needed
) {
    val context = LocalContext.current
    
    // State variables for the search bar and results
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // State to hold the currently selected location to show on the map
    var selectedLocation by remember { mutableStateOf<SearchResult?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }
    
    val locationHelper = remember { com.example.myapplication.routedrop.LocationHelper(context) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            isFetchingLocation = true
            locationHelper.fetchCurrentLocation(
                onResult = { lat, lng, addr ->
                    isFetchingLocation = false
                    selectedLocation = SearchResult(addr, lat, lng)
                    Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    isFetchingLocation = false
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                }
            )
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Bar UI
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                
                searchJob?.cancel()
                
                if (query.length > 2) {
                    searchJob = coroutineScope.launch {
                        delay(1000) 
                        isSearching = true
                        errorMessage = null
                        try {
                            val results = searchNominatim(query, "RouteDrop/1.0 (donnamaepbautista@gmail.com)")
                            searchResults = results
                            if (results.isEmpty()) {
                                errorMessage = "No results found"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Network error: ${e.message}"
                        } finally {
                            isSearching = false
                        }
                    }
                } else {
                    searchResults = emptyList()
                    errorMessage = null
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Search location...") },
            singleLine = true
        )

        // Show Loading Indicator or Error Messages
        if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        } else if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Dropdown-like list of Search Results
        if (searchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
            ) {
                items(searchResults) { result ->
                    ListItem(
                        headlineContent = { Text(result.displayName) },
                        modifier = Modifier.clickable {
                            selectedLocation = result
                            searchResults = emptyList()
                            searchQuery = result.displayName
                        }
                    )
                }
            }
        }

        // Osmdroid Map View
        var lastAnimatedLocation by remember { mutableStateOf<SearchResult?>(null) }
        
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(12.0)
                        controller.setCenter(defaultCenter)
                    }
                },
                update = { mapView ->
                    selectedLocation?.let { location ->
                        if (location != lastAnimatedLocation) {
                            val geoPoint = GeoPoint(location.lat, location.lon)
                            mapView.controller.animateTo(geoPoint)
                            mapView.controller.setZoom(16.0)
                            
                            mapView.overlays.removeAll { it is Marker }
                            val marker = Marker(mapView).apply {
                                position = geoPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = location.displayName
                            }
                            mapView.overlays.add(marker)
                            mapView.invalidate()
                            
                            lastAnimatedLocation = location
                        }
                    }
                }
            )

            // My Location Pointer Button
            FloatingActionButton(
                onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (isFetchingLocation) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Place, contentDescription = "Current Location", modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

/**
 * Helper function to hit the Nominatim free geocoding API.
 */
suspend fun searchNominatim(query: String, userAgent: String): List<SearchResult> = withContext(Dispatchers.IO) {
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val urlString = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=5"
    val url = URL(urlString)
    val connection = url.openConnection() as HttpURLConnection
    
    try {
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", userAgent)
        connection.setRequestProperty("Referer", "android-app://$userAgent")
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(response)
            val results = mutableListOf<SearchResult>()
            
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                results.add(
                    SearchResult(
                        displayName = item.getString("display_name"),
                        lat = item.getString("lat").toDouble(),
                        lon = item.getString("lon").toDouble()
                    )
                )
            }
            results
        } else {
            throw Exception("Server returned ${connection.responseCode}")
        }
    } finally {
        connection.disconnect()
    }
}
