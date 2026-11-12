package com.example.myapplication.routedrop

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        onResult: (latitude: Double, longitude: Double, address: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude
                getAddressFromCoordinates(lat, lng) { address ->
                    onResult(lat, lng, address)
                }
            } else {
                onError("Unable to retrieve location. Make sure GPS is enabled.")
            }
        }.addOnFailureListener { exception ->
            onError(exception.localizedMessage ?: "Failed to get location.")
        }
    }

    @Suppress("DEPRECATION")
    fun getAddressFromCoordinates(
        lat: Double,
        lng: Double,
        onAddressResolved: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(context, java.util.Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        val addressName = addresses.firstOrNull()?.let { addr ->
                            (0..addr.maxAddressLineIndex).joinToString(", ") { addr.getAddressLine(it) }
                        } ?: "Tagged Location ($lat, $lng)"
                        
                        CoroutineScope(Dispatchers.Main).launch { onAddressResolved(addressName) }
                    }
                } else {
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    val addressName = addresses?.firstOrNull()?.let { addr ->
                        (0..addr.maxAddressLineIndex).joinToString(", ") { addr.getAddressLine(it) }
                    } ?: "Tagged Location ($lat, $lng)"
                    
                    withContext(Dispatchers.Main) { onAddressResolved(addressName) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onAddressResolved("Tagged Location ($lat, $lng)") }
            }
        }
    }

    @Suppress("DEPRECATION")
    fun searchLocationByName(
        query: String,
        onResult: (latitude: Double, longitude: Double, addressName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (query.isBlank()) {
            onError("Search query cannot be empty.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(context, java.util.Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(query, 5) { addresses ->
                        val bestMatch = addresses.firstOrNull()
                        CoroutineScope(Dispatchers.Main).launch {
                            if (bestMatch != null) {
                                val addressName = (0..bestMatch.maxAddressLineIndex).joinToString(", ") { bestMatch.getAddressLine(it) }
                                onResult(bestMatch.latitude, bestMatch.longitude, if (addressName.isNotBlank()) addressName else query)
                            } else {
                                onError("No location found for '$query'")
                            }
                        }
                    }
                } else {
                    val addresses = geocoder.getFromLocationName(query, 5)
                    val bestMatch = addresses?.firstOrNull()
                    withContext(Dispatchers.Main) {
                        if (bestMatch != null) {
                            val addressName = (0..bestMatch.maxAddressLineIndex).joinToString(", ") { bestMatch.getAddressLine(it) }
                            onResult(bestMatch.latitude, bestMatch.longitude, if (addressName.isNotBlank()) addressName else query)
                        } else {
                            onError("No location found for '$query'")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError("Error searching location: ${e.localizedMessage}") }
            }
        }
    }
}
