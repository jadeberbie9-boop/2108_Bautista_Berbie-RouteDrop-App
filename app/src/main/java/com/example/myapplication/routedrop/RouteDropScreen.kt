package com.example.myapplication.routedrop

import com.example.myapplication.map.MapLocationPickerDialog

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect

import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.compose.ui.tooling.preview.Preview


enum class BottomTab {
    HOME, RATES, NEARBY, ME
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDropScreen(
    loggedInUser: User,
    viewModel: AppViewModel,
    onLogout: () -> Unit
) {
    val globalDeliveries = viewModel.globalDeliveries
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }

    var reqVehicle by remember { mutableStateOf("Motorcycle") }
    
    // Notification Helper (Simulates FCM Push Notification)
    val notificationHelper = remember { NotificationHelper(context) }

    // State for Courier tracking Map
    var deliveryToTrack by remember { mutableStateOf<Delivery?>(null) }

    // State for Customer flow
    var pickupName by remember { mutableStateOf(loggedInUser.address) }
    var pickupLat by remember { mutableDoubleStateOf(14.0667) }
    var pickupLng by remember { mutableDoubleStateOf(120.6333) }

    var dropOffName by remember { mutableStateOf("Batangas State University") }
    var dropOffLat by remember { mutableDoubleStateOf(13.7565) }
    var dropOffLng by remember { mutableDoubleStateOf(121.0583) }

    var pkgDesc by remember { mutableStateOf("School Documents") }
    var pkgInstructions by remember { mutableStateOf("Please handle carefully.") }
    var recipientPhoneInput by remember { mutableStateOf("") }

    var isGeotagging by remember { mutableStateOf(false) }
    var activeGeotagTarget by remember { mutableStateOf<String?>(null) } // "PICKUP" or "DROPOFF"
    var mapPickerTarget by remember { mutableStateOf<String?>(null) } // "PICKUP" or "DROPOFF"
    
    // Live tracking for Courier
    var isLiveTracking by remember { mutableStateOf(false) }

    var currentTab by remember { mutableStateOf(BottomTab.HOME) }

    // Route tracking states
    var routeDistance by remember { mutableDoubleStateOf(0.0) }
    var routeDuration by remember { mutableStateOf("") }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var isCalculatingRoute by remember { mutableStateOf(false) }

    LaunchedEffect(pickupLat, pickupLng, dropOffLat, dropOffLng) {
        isCalculatingRoute = true
        val routeInfo = RoutingHelper.getRoute(pickupLat, pickupLng, dropOffLat, dropOffLng)
        if (routeInfo != null) {
            routeDistance = routeInfo.distanceKm
            routeDuration = if (routeInfo.durationMinutes < 1) "Less than 1 min" else "~ ${routeInfo.durationMinutes} min"
            routePoints = routeInfo.geoPoints
        } else {
            routeDistance = calculateDistanceKm(pickupLat, pickupLng, dropOffLat, dropOffLng)
            val minutes = (routeDistance / 30.0 * 60).toInt()
            routeDuration = if (minutes < 1) "Less than 1 min (Straight-line)" else "~ $minutes min (Straight-line)"
            routePoints = emptyList()
        }
        isCalculatingRoute = false
    }

    // Request Notification Permission on Android 13+
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Technically you'd use a launcher here, but this is a quick safety check
                // to at least log it or prompt gracefully later.
            }
        }
    }

    // Auto-update Courier Location if broadcasting
    LaunchedEffect(isLiveTracking) {
        if (isLiveTracking && loggedInUser is Courier) {
            while (true) {
                locationHelper.fetchCurrentLocation(
                    onResult = { lat, lng, _ ->
                        globalDeliveries
                            .filter { it.courier?.userId == loggedInUser.userId && it.status != "Delivered" }
                            .forEach { delivery ->
                                delivery.updateCourierLocation(lat, lng)
                            }
                    },
                    onError = {}
                )
                kotlinx.coroutines.delay(5000) // update every 5 seconds
            }
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            val target = activeGeotagTarget
            if (target != null) {
                isGeotagging = true
                locationHelper.fetchCurrentLocation(
                    onResult = { lat, lng, addr ->
                        isGeotagging = false
                        if (target == "PICKUP") {
                            pickupLat = lat
                            pickupLng = lng
                            pickupName = addr
                        } else {
                            dropOffLat = lat
                            dropOffLng = lng
                            dropOffName = addr
                        }
                        Toast.makeText(context, "Location tagged successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        isGeotagging = false
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        } else {
            Toast.makeText(context, "Location permission is required for geotagging.", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestGeotag(target: String) {
        activeGeotagTarget = target
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Map Location Picker Dialog launch
    mapPickerTarget?.let { target ->
        val isPickup = target == "PICKUP"
        MapLocationPickerDialog(
            title = if (isPickup) "Pinpoint Pickup Location" else "Pinpoint Drop-off Location",
            initialLat = if (isPickup) pickupLat else dropOffLat,
            initialLng = if (isPickup) pickupLng else dropOffLng,
            onLocationConfirmed = { lat, lng, address ->
                if (isPickup) {
                    pickupLat = lat
                    pickupLng = lng
                    pickupName = address
                } else {
                    dropOffLat = lat
                    dropOffLng = lng
                    dropOffName = address
                }
                mapPickerTarget = null
                Toast.makeText(context, "Pinpoint location selected!", Toast.LENGTH_SHORT).show()
            },
            onDismissRequest = {
                mapPickerTarget = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "RouteDrop Delivery",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Out", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == BottomTab.HOME,
                    onClick = { currentTab = BottomTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = currentTab == BottomTab.RATES,
                    onClick = { currentTab = BottomTab.RATES },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Rates") }
                )
                NavigationBarItem(
                    selected = currentTab == BottomTab.NEARBY,
                    onClick = { currentTab = BottomTab.NEARBY },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text("Nearby") }
                )
                NavigationBarItem(
                    selected = currentTab == BottomTab.ME,
                    onClick = { currentTab = BottomTab.ME },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Me") }
                )
            }
        }
    ) { paddingValues ->
        when (currentTab) {
            BottomTab.HOME -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Logged In User Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Logged In As:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(loggedInUser.display(), fontSize = 13.sp)
                }
            }

            if (loggedInUser is Courier) {
                // Courier Dashboard
                val myAcceptedDeliveries = globalDeliveries.filter { it.courier?.userId == loggedInUser.userId && it.status != "Delivered" }
                val availableRequests = globalDeliveries.filter { it.courier == null && it.status == "Pending" }

                // Earnings Dashboard
                val completedDeliveries = globalDeliveries.filter { it.courier?.userId == loggedInUser.userId && it.status == "Delivered" }
                val totalEarnings = completedDeliveries.sumOf { it.fare }
                val totalTips = completedDeliveries.sumOf { it.tipAmount }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Earnings Dashboard", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Trips:", fontWeight = FontWeight.Bold)
                            Text("${completedDeliveries.size}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fares Earned:", fontWeight = FontWeight.Bold)
                            Text("₱${"%.2f".format(totalEarnings)}")
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tips Earned:", fontWeight = FontWeight.Bold)
                            Text("₱${"%.2f".format(totalTips)}")
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Earnings:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("₱${"%.2f".format(totalEarnings + totalTips)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Status Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Status: ${if (loggedInUser.isOnline) "Online" else "Offline"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (loggedInUser.isOnline) Color(0xFF4CAF50) else Color.Gray
                    )
                    androidx.compose.material3.Switch(
                        checked = loggedInUser.isOnline,
                        onCheckedChange = { loggedInUser.isOnline = it }
                    )
                }

                // 1. My Accepted Deliveries Section
                if (myAcceptedDeliveries.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("My Accepted Deliveries", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { isLiveTracking = !isLiveTracking },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (isLiveTracking) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(if (isLiveTracking) Icons.Default.Stop else Icons.Default.Wifi, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isLiveTracking) "Stop Broadcast" else "Go Live")
                        }
                    }
                    
                    if (isLiveTracking) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sharing live location with customers...", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    myAcceptedDeliveries.forEach { delivery ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Request from: ${delivery.customer.name}", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("₱${"%.2f".format(delivery.fare)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Text("Pickup: ${delivery.pickup.locationName}")
                                Text("Drop-off: ${delivery.dropOff.locationName}")
                                Text("Status: ${delivery.status}", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                                
                                OrderTrackerBar(status = delivery.status)
                                
                                if (delivery.eta != "N/A" && delivery.eta != "Calculating...") {
                                    Text("ETA: ${delivery.eta}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { 
                                            delivery.updateStatus("In Transit") 
                                            viewModel.updateDelivery(delivery)
                                            notificationHelper.sendNotification("Courier is on the way!", "Your package is now in transit.")
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("In Transit")
                                    }
                                    Button(
                                        onClick = { 
                                            delivery.updateStatus("Delivered")
                                            viewModel.updateDelivery(delivery)
                                            notificationHelper.sendNotification("Package Delivered", "Your package has arrived safely.")
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Delivered")
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        isGeotagging = true
                                        locationHelper.fetchCurrentLocation(
                                            onResult = { lat, lng, _ ->
                                                isGeotagging = false
                                                delivery.updateCourierLocation(lat, lng)
                                                viewModel.updateDelivery(delivery)
                                                Toast.makeText(context, "Location shared to customer!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                isGeotagging = false
                                                Toast.makeText(context, "Error: $err", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isGeotagging) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    } else {
                                        Icon(Icons.Default.Place, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Share Single Location Update")
                                    }
                                }
                            }
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // 2. Available Requests Section
                if (loggedInUser.isOnline) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Available Customer Requests", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    if (availableRequests.isEmpty()) {
                        Text("No pending delivery requests right now.", fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    } else {
                        availableRequests.forEach { delivery ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Customer: ${delivery.customer.name}", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text("₱${"%.2f".format(delivery.fare)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text("Pickup: ${delivery.pickup.locationName}")
                                    Text("Drop-off: ${delivery.dropOff.locationName}")
                                    Text("Package: ${delivery.details.packageDescription} (${delivery.requestedVehicle})", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Button(
                                        onClick = { 
                                            delivery.assignCourier(loggedInUser) 
                                            viewModel.updateDelivery(delivery)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Accept Delivery Request")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("You are offline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Go online to receive new delivery requests.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else if (loggedInUser is Customer) {
                // Customer Request Flow

                // Pickup Geotag Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pickup Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pickupName,
                        onValueChange = { pickupName = it },
                        label = { Text("Pickup Location Name/Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Coords: %.4f, %.4f".format(pickupLat, pickupLng),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { mapPickerTarget = "PICKUP" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Pick on Map", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { requestGeotag("PICKUP") },
                            enabled = !isGeotagging,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tag GPS", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Dropoff Geotag Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Drop-off Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dropOffName,
                        onValueChange = { dropOffName = it },
                        label = { Text("Drop-off Location Name/Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Coords: %.4f, %.4f".format(dropOffLat, dropOffLng),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { mapPickerTarget = "DROPOFF" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Pick on Map", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { requestGeotag("DROPOFF") },
                            enabled = !isGeotagging,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tag GPS", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Package Details Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery Package Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pkgDesc,
                        onValueChange = { pkgDesc = it },
                        label = { Text("Package Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pkgInstructions,
                        onValueChange = { pkgInstructions = it },
                        label = { Text("Instructions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recipientPhoneInput,
                        onValueChange = { recipientPhoneInput = it },
                        label = { Text("Recipient Phone Number (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Requested Vehicle:", fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Motorcycle", "Car", "Truck").forEach { vehicle ->
                            OutlinedButton(
                                onClick = { reqVehicle = vehicle },
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (reqVehicle == vehicle) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(vehicle)
                            }
                        }
                    }
                }
            }

            // Loading Indicator during Geotagging
            AnimatedVisibility(visible = isGeotagging || isCalculatingRoute) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (isCalculatingRoute) "Calculating optimal route..." else "Fetching high-accuracy GPS coordinates...", 
                        fontSize = 14.sp
                    )
                }
            }

            // Calculate estimated fare dynamically
            val estimatedFare = calculateFare(routeDistance, reqVehicle)

            // Preview Map (Route from Pickup to Drop-off)
            Card(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                var previewMapViewRef by remember { mutableStateOf<MapView?>(null) }
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(13.0)
                                previewMapViewRef = this
                            }
                        },
                        update = { mapView ->
                            mapView.overlays.removeAll { it is Marker || it is Polyline }
                            
                            val pickupGeo = GeoPoint(pickupLat, pickupLng)
                            val dropOffGeo = GeoPoint(dropOffLat, dropOffLng)
                            
                            // Draw Route Polyline
                            if (routePoints.isNotEmpty()) {
                                val polyline = Polyline().apply {
                                    setPoints(routePoints)
                                    outlinePaint.color = android.graphics.Color.parseColor("#9C27B0") // Purple route
                                    outlinePaint.strokeWidth = 10f
                                }
                                mapView.overlays.add(polyline)
                                
                                // Center on the middle of the route
                                val midLat = (pickupLat + dropOffLat) / 2
                                val midLng = (pickupLng + dropOffLng) / 2
                                mapView.controller.setCenter(GeoPoint(midLat, midLng))
                            } else {
                                mapView.controller.setCenter(pickupGeo)
                            }
                            
                            val pMarker = Marker(mapView).apply {
                                position = pickupGeo
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Pickup: $pickupName"
                            }
                            val dMarker = Marker(mapView).apply {
                                position = dropOffGeo
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Drop-off: $dropOffName"
                            }
                            
                            mapView.overlays.add(pMarker)
                            mapView.overlays.add(dMarker)
                            mapView.invalidate()
                        }
                    )
                }
            }
            
            DisposableEffect(Unit) {
                // To avoid memory leaks with previewMapViewRef if needed
                onDispose { }
            }

            // Fare display
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Route Distance:", fontSize = 14.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("%.2f km".format(routeDistance), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estimated ETA:", fontSize = 14.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(routeDuration, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Estimated Fare:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("₱${"%.2f".format(estimatedFare)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Create Delivery Button
            Button(
                onClick = {
                    val ts = System.currentTimeMillis()
                    val pLoc = PickupLoc("P-$ts", pickupName, pickupLat, pickupLng, loggedInUser.name)
                    val dLoc = DropOffLoc("D-$ts", dropOffName, dropOffLat, dropOffLng, loggedInUser.name)
                    val details = DeliveryDetails(pkgDesc, pkgInstructions)
                    val deliveryId = "DEL-${System.currentTimeMillis()}"
                    val delivery = Delivery(
                        deliveryId = deliveryId,
                        customer = loggedInUser,
                        courier = null,
                        pickup = pLoc,
                        dropOff = dLoc,
                        details = details,
                        requestedVehicle = reqVehicle,
                        fare = estimatedFare,
                        recipientPhone = recipientPhoneInput
                    )

                    viewModel.addDelivery(delivery)
                    
                    // Reset form fields
                    pkgDesc = ""
                    pkgInstructions = ""
                    recipientPhoneInput = ""

                    Toast.makeText(context, "Delivery Request Created!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create Delivery Request")
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Your Active Deliveries", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            // Multiple active deliveries for Customer
            val myDeliveries = globalDeliveries.filter { it.customer.userId == loggedInUser.userId }
            if (myDeliveries.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "📦",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Active Deliveries",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You don't have any active delivery requests. Tap 'Create Delivery Request' above to start!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                myDeliveries.forEach { delivery ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Delivery [${delivery.deliveryId}]",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text("₱${"%.2f".format(delivery.fare)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status = delivery.status)
                            }
                            
                            OrderTrackerBar(status = delivery.status)
    
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Courier: ${delivery.courier?.name ?: "Looking for courier..."}")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Pickup: ${delivery.pickup.getAddress()}")
                            Text("Drop-off: ${delivery.dropOff.getAddress()}")
                            
                            if (delivery.isCourierLocationAvailable && delivery.status != "Delivered") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("ETA: ${delivery.eta}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { deliveryToTrack = delivery },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Live Map Tracking")
                                }
                            }
                        }
                    }
                }
            }
            } else if (loggedInUser is Recipient) {
                // Recipient Dashboard
                Text("Your Incoming Deliveries", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                val myIncomingDeliveries = globalDeliveries.filter { it.recipientPhone == loggedInUser.phoneNumber }
                if (myIncomingDeliveries.isEmpty()) {
                    Text("No incoming deliveries right now.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                } else {
                    myIncomingDeliveries.forEach { delivery ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Delivery [${delivery.deliveryId}]",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    StatusBadge(status = delivery.status)
                                }
                                
                                OrderTrackerBar(status = delivery.status)
        
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("From: ${delivery.customer.name}")
                                Text("Courier: ${delivery.courier?.name ?: "Looking for courier..."}")
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Pickup: ${delivery.pickup.getAddress()}")
                                Text("Drop-off: ${delivery.dropOff.getAddress()}")
                                
                                if (delivery.isCourierLocationAvailable && delivery.status != "Delivered") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("ETA: ${delivery.eta}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { deliveryToTrack = delivery },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Live Map Tracking")
                                    }
                                }
                            }
                        }
                    }
                }
            } // Close Recipient flow block
        } // Close Column
        } // Close HOME tab
        BottomTab.RATES -> {
            ShippingRatesScreen(paddingValues = paddingValues)
        }
        BottomTab.NEARBY -> {
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                com.example.myapplication.map.LocationSearchMapScreen()
            }
        }
        BottomTab.ME -> {
            ProfileScreen(loggedInUser = loggedInUser, globalDeliveries = globalDeliveries, paddingValues = paddingValues)
        }
        } // Close when block
    } // Close Scaffold
    
    // Tracking UI Overlay
    deliveryToTrack?.let { delivery ->
        CourierMapDialog(
            delivery = delivery,
            onDismissRequest = { deliveryToTrack = null }
        )
    }

    // Rating Dialog for Customer
    if (loggedInUser is Customer) {
        val unratedDelivery = globalDeliveries.firstOrNull { 
            it.customer.userId == loggedInUser.userId && it.status == "Delivered" && !it.isRated
        }
        unratedDelivery?.let { delivery ->
            RatingReviewDialog(
                delivery = delivery,
                onSubmit = { rating, tip, comment ->
                    delivery.rating = rating
                    delivery.tipAmount = tip
                    delivery.reviewComment = comment
                    delivery.isRated = true
                    viewModel.updateDelivery(delivery)
                    Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                },
                onDismiss = {
                    delivery.isRated = true // Marked as rated so it won't pop up again
                    viewModel.updateDelivery(delivery)
                }
            )
        }
    }
}

@Composable
fun ProfileScreen(loggedInUser: User, globalDeliveries: List<Delivery>, paddingValues: PaddingValues) {
    var name by remember { mutableStateOf(loggedInUser.name) }
    var phone by remember { mutableStateOf(loggedInUser.phoneNumber) }
    var address by remember { mutableStateOf(loggedInUser.address) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("My Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Default Location/Address") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = {
                loggedInUser.name = name
                loggedInUser.phoneNumber = phone
                loggedInUser.address = address
                
                // Save updated info so they don't lose it on restart
                val authManager = AuthManager(context)
                authManager.saveUser(loggedInUser)
                
                Toast.makeText(context, "Profile Updated successfully!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Information")
        }
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        Text("Delivery History", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        val pastDeliveries = globalDeliveries.filter { 
            (it.customer.userId == loggedInUser.userId || it.courier?.userId == loggedInUser.userId) && it.status == "Delivered"
        }
        
        if (pastDeliveries.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🚚",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Past Deliveries",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You haven't completed any deliveries yet. Once a package is delivered, it will appear here.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            pastDeliveries.forEach { delivery ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Delivery [${delivery.deliveryId}]", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1AC022), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Delivered", color = Color(0xFF1AC022), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("From: ${delivery.pickup.locationName}")
                        Text("To: ${delivery.dropOff.locationName}")
                        Text("Package: ${delivery.details.packageDescription}", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        
                        if (delivery.isRated) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Your Rating: ", fontSize = 14.sp)
                                Row {
                                    for (i in 1..5) {
                                        Icon(
                                            if (i <= delivery.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            if (delivery.tipAmount > 0) {
                                Text("Tip: ₱${"%.2f".format(delivery.tipAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            if (delivery.reviewComment.isNotBlank()) {
                                Text("\"${delivery.reviewComment}\"", fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderTrackerBar(status: String) {
    val steps = listOf("Pending", "Accepted", "In Transit", "Delivered")
    val currentStepIndex = when (status) {
        "Pending" -> 0
        "Courier Assigned" -> 1
        "In Transit" -> 2
        "Delivered" -> 3
        else -> 0
    }

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
        steps.forEachIndexed { index, label ->
            val isCompleted = index <= currentStepIndex
            val isCurrent = index == currentStepIndex
            val circleColor = if (isCompleted) MaterialTheme.colorScheme.primary else Color.LightGray
            
            // Step item
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    // The connecting lines
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f).height(3.dp).background(if (index == 0) Color.Transparent else if (index <= currentStepIndex) MaterialTheme.colorScheme.primary else Color.LightGray))
                        Box(modifier = Modifier.weight(1f).height(3.dp).background(if (index == steps.size - 1) Color.Transparent else if (index < currentStepIndex) MaterialTheme.colorScheme.primary else Color.LightGray))
                    }
                    // The circle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(circleColor, shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val bgColor = when (status) {
        "Pending" -> Color(0xFFED6C02)
        "Courier Assigned" -> Color(0xFF0288D1)
        "Picked Up" -> Color(0xFF0288D1)
        "In Transit" -> Color(0xFF9C27B0)
        "Delivered" -> Color(0xFF1AC022)
        else -> MaterialTheme.colorScheme.secondary
    }

    Box(
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CourierMapDialog(
    delivery: Delivery,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    
    val targetLat = if (delivery.status == "Courier Assigned") delivery.pickup.latitude else delivery.dropOff.latitude
    val targetLng = if (delivery.status == "Courier Assigned") delivery.pickup.longitude else delivery.dropOff.longitude
    val targetName = if (delivery.status == "Courier Assigned") "Pickup" else "Drop-off"

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Track the last known location so we don't force snap the user when they pan the map
        var lastCourierLat by remember { mutableDoubleStateOf(delivery.courierLat) }
        var lastCourierLng by remember { mutableDoubleStateOf(delivery.courierLng) }

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
                        text = "Courier Real-Time Tracking",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                // Map View
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
                                controller.setZoom(16.0)
                                
                                val courierGeoPoint = GeoPoint(delivery.courierLat, delivery.courierLng)
                                controller.setCenter(courierGeoPoint)

                                val marker = Marker(this).apply {
                                    position = courierGeoPoint
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    title = "Courier Location"
                                    snippet = "Lat: %.4f, Lng: %.4f".format(delivery.courierLat, delivery.courierLng)
                                }
                                overlays.add(marker)
                                mapViewRef = this
                            }
                        },
                        update = { mapView ->
                            val courierGeoPoint = GeoPoint(delivery.courierLat, delivery.courierLng)
                            
                            // Only animate if the courier actually moved
                            if (delivery.courierLat != lastCourierLat || delivery.courierLng != lastCourierLng) {
                                mapView.controller.animateTo(courierGeoPoint)
                                lastCourierLat = delivery.courierLat
                                lastCourierLng = delivery.courierLng
                            }
                            
                            // Clear map
                            mapView.overlays.removeAll { it is Marker || it is Polyline }
                            
                            // 1. Draw Route Polyline (if we have one)
                            if (delivery.routePoints.isNotEmpty()) {
                                val polyline = Polyline().apply {
                                    setPoints(delivery.routePoints)
                                    outlinePaint.color = android.graphics.Color.parseColor("#0288D1") // Blue
                                    outlinePaint.strokeWidth = 12f
                                }
                                mapView.overlays.add(polyline)
                            }
                            
                            // 2. Draw Target Destination Marker
                            val targetMarker = Marker(mapView).apply {
                                position = GeoPoint(targetLat, targetLng)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = targetName
                            }
                            mapView.overlays.add(targetMarker)

                            // 3. Draw Courier Marker
                            val courierMarker = Marker(mapView).apply {
                                position = courierGeoPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Courier Location"
                                snippet = delivery.eta
                            }
                            mapView.overlays.add(courierMarker)
                            
                            mapView.invalidate()
                        }
                    )
                }
                
                // Footer details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var showChatDialog by remember { mutableStateOf(false) }

                    Text(
                        "Tracking Courier: ${delivery.courier?.name}", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp
                    )
                    Text(
                        "Coords: %.5f, %.5f".format(delivery.courierLat, delivery.courierLng),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val phoneNumber = delivery.courier?.phoneNumber ?: ""
                                if (phoneNumber.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$phoneNumber")
                                    }
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Call Courier")
                        }
                        
                        Button(
                            onClick = { showChatDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Chat")
                        }
                    }

                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Map")
                    }

                    if (showChatDialog) {
                        ChatDialog(
                            courierName = delivery.courier?.name ?: "Courier",
                            onDismiss = { showChatDialog = false }
                        )
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

@Composable
fun RatingReviewDialog(
    delivery: Delivery,
    onSubmit: (rating: Int, tip: Double, comment: String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var tipInput by remember { mutableStateOf("") }
    var commentInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rate Your Delivery",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Courier: ${delivery.courier?.name ?: "Unknown"}",
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Star Rating
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Rate $i stars",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tip Input
                OutlinedTextField(
                    value = tipInput,
                    onValueChange = { tipInput = it },
                    label = { Text("Leave a Tip (₱)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Comment Input
                OutlinedTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    label = { Text("Leave a comment (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Not Now", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            val tipAmount = tipInput.toDoubleOrNull() ?: 0.0
                            onSubmit(rating, tipAmount, commentInput)
                        },
                        enabled = rating > 0
                    ) {
                        Text("Submit Review")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CourierDashboardPreview() {
    val mockCustomer = Customer("C-123", "Alice", "password", "alice@test.com", "555-0101")
    val mockCourier = Courier("CR-456", "Bob Courier", "bob@test.com", "password", "555-0202", "Motorcycle")
    
    val pLoc = PickupLoc("P-1", "SM City Batangas", 13.7565, 121.0583, "Alice")
    val dLoc = DropOffLoc("D-1", "Batangas State University", 13.7753, 121.0740, "Alice")
    val details = DeliveryDetails("Documents", "Handle with care")
    
    val pendingDelivery = Delivery("DEL-1", mockCustomer, null, pLoc, dLoc, details, "Motorcycle", 120.0)
    
    MaterialTheme {
        RouteDropScreen(
            loggedInUser = mockCourier,
            viewModel = AppViewModel(), // Mock viewmodel for preview
            onLogout = {}
        )
    }
}

@Composable
fun ChatDialog(
    courierName: String,
    onDismiss: () -> Unit
) {
    var messages by remember { mutableStateOf(listOf<String>()) }
    var input by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Chat with $courierName",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                // Messages list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (messages.isEmpty()) {
                        Text(
                            text = "No messages yet. Say hi!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    messages.forEach { msg ->
                        val isUser = msg.startsWith("You:")
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = msg,
                                    modifier = Modifier.padding(8.dp),
                                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Input area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message...") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (input.isNotBlank()) {
                                messages = messages + "You: $input"
                                input = ""
                            }
                        }
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
}
