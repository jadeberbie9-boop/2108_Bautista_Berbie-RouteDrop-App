package com.example.myapplication.routedrop

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.osmdroid.util.GeoPoint

class AppViewModel : ViewModel() {
    val registeredUsers = mutableStateListOf<User>()
    val globalDeliveries = mutableStateListOf<Delivery>()

    private val db = FirebaseFirestore.getInstance()
    private var deliveriesListener: ListenerRegistration? = null

    init {
        // Start listening to Firestore deliveries collection in real-time
        listenToDeliveries()
    }

    private fun listenToDeliveries() {
        deliveriesListener = db.collection("deliveries")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AppViewModel", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val updatedDeliveries = mutableListOf<Delivery>()
                    for (doc in snapshot.documents) {
                        try {
                            val data = doc.data ?: continue
                            val d = mapToDelivery(doc.id, data)
                            updatedDeliveries.add(d)
                        } catch (e: Exception) {
                            Log.e("AppViewModel", "Error parsing delivery ${doc.id}", e)
                        }
                    }

                    // Replace local list with the fresh remote list
                    globalDeliveries.clear()
                    globalDeliveries.addAll(updatedDeliveries)
                }
            }
    }

    // Call this whenever a customer creates a delivery
    fun addDelivery(delivery: Delivery) {
        val data = deliveryToMap(delivery)
        db.collection("deliveries").document(delivery.deliveryId).set(data)
            .addOnSuccessListener { Log.d("AppViewModel", "Delivery added") }
            .addOnFailureListener { e -> Log.e("AppViewModel", "Error adding delivery", e) }
    }

    // Call this whenever the status, location, or assignment changes
    fun updateDelivery(delivery: Delivery) {
        val data = deliveryToMap(delivery)
        db.collection("deliveries").document(delivery.deliveryId).update(data)
            .addOnSuccessListener { Log.d("AppViewModel", "Delivery updated") }
            .addOnFailureListener { e -> Log.e("AppViewModel", "Error updating delivery", e) }
    }

    override fun onCleared() {
        super.onCleared()
        deliveriesListener?.remove()
    }

    // Mapper functions to handle our complex local models
    private fun deliveryToMap(delivery: Delivery): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "deliveryId" to delivery.deliveryId,
            "requestedVehicle" to delivery.requestedVehicle,
            "fare" to delivery.fare,
            "recipientPhone" to delivery.recipientPhone,
            "status" to delivery.status,
            "courierLat" to delivery.courierLat,
            "courierLng" to delivery.courierLng,
            "isCourierLocationAvailable" to delivery.isCourierLocationAvailable,
            "eta" to delivery.eta,
            "isRated" to delivery.isRated,
            "rating" to delivery.rating,
            "reviewComment" to delivery.reviewComment,
            "tipAmount" to delivery.tipAmount
        )

        // Customer mapping
        map["customer"] = mapOf(
            "userId" to delivery.customer.userId,
            "name" to delivery.customer.name,
            "emailAddress" to delivery.customer.emailAddress,
            "password" to delivery.customer.password,
            "phoneNumber" to delivery.customer.phoneNumber,
            "address" to delivery.customer.address
        )

        // Courier mapping (nullable)
        delivery.courier?.let { courier ->
            map["courier"] = mapOf(
                "userId" to courier.userId,
                "name" to courier.name,
                "emailAddress" to courier.emailAddress,
                "password" to courier.password,
                "phoneNumber" to courier.phoneNumber,
                "address" to courier.address,
                "vehicleType" to courier.vehicleType
            )
        }

        // Pickup Location mapping
        map["pickup"] = mapOf(
            "locationId" to delivery.pickup.locationId,
            "locationName" to delivery.pickup.locationName,
            "latitude" to delivery.pickup.latitude,
            "longitude" to delivery.pickup.longitude,
            "person" to delivery.pickup.person
        )

        // DropOff Location mapping
        map["dropOff"] = mapOf(
            "locationId" to delivery.dropOff.locationId,
            "locationName" to delivery.dropOff.locationName,
            "latitude" to delivery.dropOff.latitude,
            "longitude" to delivery.dropOff.longitude,
            "person" to delivery.dropOff.person
        )

        // Details mapping
        map["details"] = mapOf(
            "packageDescription" to delivery.details.packageDescription,
            "instructions" to delivery.details.instructions
        )

        return map
    }

    private fun mapToDelivery(id: String, data: Map<String, Any>): Delivery {
        val custMap = data["customer"] as Map<String, Any>
        val customer = Customer(
            userId = custMap["userId"] as String,
            name = custMap["name"] as String,
            emailAddress = custMap["emailAddress"] as String,
            password = custMap["password"] as String,
            phoneNumber = custMap["phoneNumber"] as String,
            address = custMap["address"] as String? ?: ""
        )

        val courMap = data["courier"] as? Map<String, Any>
        val courier = courMap?.let {
            Courier(
                userId = it["userId"] as String,
                name = it["name"] as String,
                emailAddress = it["emailAddress"] as String,
                password = it["password"] as String,
                phoneNumber = it["phoneNumber"] as String,
                address = it["address"] as String? ?: "",
                vehicleType = it["vehicleType"] as String? ?: "Motorcycle"
            )
        }

        val pMap = data["pickup"] as Map<String, Any>
        val pickup = PickupLoc(
            locationId = pMap["locationId"] as String,
            locationName = pMap["locationName"] as String,
            latitude = (pMap["latitude"] as Number).toDouble(),
            longitude = (pMap["longitude"] as Number).toDouble(),
            person = pMap["person"] as String
        )

        val dMap = data["dropOff"] as Map<String, Any>
        val dropOff = DropOffLoc(
            locationId = dMap["locationId"] as String,
            locationName = dMap["locationName"] as String,
            latitude = (dMap["latitude"] as Number).toDouble(),
            longitude = (dMap["longitude"] as Number).toDouble(),
            person = dMap["person"] as String
        )

        val detMap = data["details"] as Map<String, Any>
        val details = DeliveryDetails(
            packageDescription = detMap["packageDescription"] as String,
            instructions = detMap["instructions"] as String
        )

        val delivery = Delivery(
            deliveryId = id,
            customer = customer,
            courier = courier,
            pickup = pickup,
            dropOff = dropOff,
            details = details,
            requestedVehicle = data["requestedVehicle"] as String? ?: "Motorcycle",
            fare = (data["fare"] as? Number)?.toDouble() ?: 0.0,
            recipientPhone = data["recipientPhone"] as String? ?: ""
        )

        delivery.status = data["status"] as String? ?: "Pending"
        delivery.courierLat = (data["courierLat"] as? Number)?.toDouble() ?: 0.0
        delivery.courierLng = (data["courierLng"] as? Number)?.toDouble() ?: 0.0
        delivery.isCourierLocationAvailable = data["isCourierLocationAvailable"] as? Boolean ?: false
        delivery.eta = data["eta"] as String? ?: "Calculating..."
        delivery.isRated = data["isRated"] as? Boolean ?: false
        delivery.rating = (data["rating"] as? Number)?.toInt() ?: 0
        delivery.reviewComment = data["reviewComment"] as String? ?: ""
        delivery.tipAmount = (data["tipAmount"] as? Number)?.toDouble() ?: 0.0

        return delivery
    }
}
