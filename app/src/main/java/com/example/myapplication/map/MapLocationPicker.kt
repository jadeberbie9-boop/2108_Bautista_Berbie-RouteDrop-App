package com.example.myapplication.map

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import com.example.myapplication.routedrop.LocationHelper

import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker


@Composable
fun MapLocationPickerDialog(
    title: String,
    initialLat: Double,
    initialLng: Double,
    onLocationConfirmed: (latitude: Double, longitude: Double, addressName: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val keyboardController = LocalSoftwareKeyboardController.current

    var pinnedLat by remember { mutableDoubleStateOf(initialLat) }
    var pinnedLng by remember { mutableDoubleStateOf(initialLng) }
    var addressName by remember { mutableStateOf("Fetching address...") }
    var searchQuery by remember { mutableStateOf("") }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var markerRef by remember { mutableStateOf<Marker?>(null) }
    
    var isFetchingLocation by remember { mutableStateOf(false) }

    fun updatePinAndCamera(lat: Double, lng: Double, address: String? = null, forceCenter: Boolean = false) {
        pinnedLat = lat
        pinnedLng = lng
        val geoPoint = GeoPoint(lat, lng)

        mapViewRef?.let { mapView ->
            if (forceCenter) {
                mapView.controller.animateTo(geoPoint)
            }
            markerRef?.let { marker ->
                marker.position = geoPoint
                marker.snippet = "Lat: %.4f, Lng: %.4f".format(lat, lng)
            }
            mapView.invalidate()
        }

        if (address != null) {
            addressName = address
        } else {
            locationHelper.getAddressFromCoordinates(lat, lng) { resolvedAddr ->
                addressName = resolvedAddr
            }
        }
    }

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
                    // update pin and center the map on the user's location
                    updatePinAndCamera(lat, lng, addr, forceCenter = true)
                    Toast.makeText(context, "Location tagged!", Toast.LENGTH_SHORT).show()
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

    fun performSearch() {
        keyboardController?.hide()
        if (searchQuery.isBlank()) return

        locationHelper.searchLocationByName(
            query = searchQuery,
            onResult = { lat, lng, resolvedAddress ->
                updatePinAndCamera(lat, lng, resolvedAddress, forceCenter = true)
                Toast.makeText(context, "Location found!", Toast.LENGTH_SHORT).show()
            },
            onError = { err ->
                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            }
        )
    }

    LaunchedEffect(pinnedLat, pinnedLng) {
        locationHelper.getAddressFromCoordinates(pinnedLat, pinnedLng) { resolvedAddr ->
            addressName = resolvedAddr
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp)
                ) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                // Location Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search location (e.g. Batangas, SM Manila)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { performSearch() })
                    )
                    Button(onClick = { performSearch() }) {
                        Text("Search")
                    }
                }

                // OpenStreetMap Canvas Overlay
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(15.0)
                                val initialGeo = GeoPoint(initialLat, initialLng)
                                controller.setCenter(initialGeo)

                                val marker = Marker(this).apply {
                                    position = initialGeo
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    this.title = "Selected Pinpoint"
                                }
                                overlays.add(marker)
                                markerRef = marker

                                val eventsReceiver = object : MapEventsReceiver {
                                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                        updatePinAndCamera(p.latitude, p.longitude)
                                        return true
                                    }

                                    override fun longPressHelper(p: GeoPoint): Boolean {
                                        updatePinAndCamera(p.latitude, p.longitude)
                                        return true
                                    }
                                }
                                overlays.add(MapEventsOverlay(eventsReceiver))

                                mapViewRef = this
                            }
                        },
                        update = { mapView ->
                            // Do nothing on regular updates so the map doesn't snap back when panning
                            mapViewRef = mapView
                        }
                    )

                    // Helper Overlay Instruction
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Text(
                            "Tap map or use search bar to pinpoint location",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

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

                // Selected Coordinates & Address Footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Selected Address:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(addressName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Coords: %.5f, %.5f".format(pinnedLat, pinnedLng),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onLocationConfirmed(
                                    pinnedLat,
                                    pinnedLng,
                                    addressName
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm Pinpoint")
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef?.onDetach()
        }
    }
}
