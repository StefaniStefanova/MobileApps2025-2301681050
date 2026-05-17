package com.example.pinmemory.util

import android.content.Context
import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.URL
import kotlin.coroutines.resume
import org.json.JSONObject

class LocationHelper(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): Pair<Double, Double>? {
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(Pair(location.latitude, location.longitude))
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }

    fun getLocationName(latitude: Double, longitude: Double): String {
        return try {
            val url = "https://nominatim.openstreetmap.org/reverse?lat=$latitude&lon=$longitude&format=json"
            val response = URL(url).openConnection().apply {
                setRequestProperty("User-Agent", "PinMemory/1.0")
                connectTimeout = 5000
                readTimeout = 5000
            }.getInputStream().bufferedReader().readText()

            val json = JSONObject(response)
            val address = json.optJSONObject("address")

            val name = json.optString("name", "")
            val road = address?.optString("road", "")
            val city = address?.optString("city", "")
                ?: address?.optString("town", "")
                ?: address?.optString("village", "")
                ?: address?.optString("municipality", "")

            when {
                name.isNotEmpty() -> name
                !road.isNullOrEmpty() && !city.isNullOrEmpty() -> "$road, $city"
                !road.isNullOrEmpty() -> road
                !city.isNullOrEmpty() -> city
                else -> "%.4f, %.4f".format(latitude, longitude)
            }
        } catch (e: Exception) {
            "%.4f, %.4f".format(latitude, longitude)
        }
    }
}