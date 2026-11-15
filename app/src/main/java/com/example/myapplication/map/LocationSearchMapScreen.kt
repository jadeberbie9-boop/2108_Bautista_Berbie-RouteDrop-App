package com.example.myapplication.map

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

import android.view.MotionEvent
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
    defaultCenter: GeoPoint = GeoPoint(14.0667, 120.6333) // Default center
) {
    val context = LocalContext.current

    // Navy Blue Theme Colors for high contrast over the map
    val navyBackground = Color(0xFF0F172A)
    val navySurface = Color(0xFF1E293B)
    val navyAccent = Color(0xFF38BDF8)
    val lightText = Color.White
    val lightSubtext = Color(0xFF94A3B8)
    
    // State variables for the search bar and results
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // State to hold the currently selected location to show on the map
    var selectedLocation by remember { mutableStateOf<SearchResult?>(null) }
    var lastAnimatedLocation by remember { mutableStateOf<SearchResult?>(null) }
    
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

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Osmdroid Map View (Full screen background)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(MapStyles.GOOGLE_MAPS)
                    setMultiTouchControls(true)
                    controller.setZoom(12.0)
                    controller.setCenter(defaultCenter)
                    setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                v.parent?.requestDisallowInterceptTouchEvent(true)
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                v.parent?.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                        if (event.action == MotionEvent.ACTION_UP) {
                            v.performClick()
                        }
                        false
                    }
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

        // 2. High-Contrast Navy Blue Floating Search Bar Overlay at Top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 12.dp,
                tonalElevation = 12.dp,
                color = navyBackground
            ) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            searchJob?.cancel()
                            
                            if (query.length > 2) {
                                searchJob = coroutineScope.launch {
                                    delay(800) 
                                    isSearching = true
                                    errorMessage = null
                                    try {
                                        val results = searchNominatim(query, "RouteDrop/1.0 (donnamaepbautista@gmail.com)")
                                        searchResults = results
                                        if (results.isEmpty()) {
                                            errorMessage = "No location results found"
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
                        placeholder = { Text("Search location...", color = lightSubtext) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = navyAccent) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    searchResults = emptyList()
                                    errorMessage = null
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = lightText)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = TextStyle(color = lightText, fontSize = 15.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = navyAccent,
                            unfocusedBorderColor = navyAccent.copy(alpha = 0.5f),
                            focusedContainerColor = navySurface,
                            unfocusedContainerColor = navySurface,
                            focusedTextColor = lightText,
                            unfocusedTextColor = lightText
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    if (isSearching) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = navyAccent,
                            trackColor = navySurface
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFF87171),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    if (searchResults.isNotEmpty()) {
                        HorizontalDivider(color = Color(0xFF334155))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                        ) {
                            items(searchResults) { result ->
                                ListItem(
                                    headlineContent = { Text(result.displayName, fontSize = 14.sp, color = lightText) },
                                    leadingContent = { Icon(Icons.Default.Place, contentDescription = null, tint = navyAccent) },
                                    colors = ListItemDefaults.colors(
                                        containerColor = navyBackground
                                    ),
                                    modifier = Modifier.clickable {
                                        selectedLocation = result
                                        searchResults = emptyList()
                                        searchQuery = result.displayName
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Current Location FAB (Bottom Right Overlay)
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
            containerColor = navySurface,
            contentColor = navyAccent
        ) {
            if (isFetchingLocation) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = navyAccent)
            } else {
                Icon(Icons.Default.Place, contentDescription = "Current Location", modifier = Modifier.size(24.dp))
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
