package com.sunnyweather.android.logic.network

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import kotlinx.coroutines.Dispatchers
import okhttp3.Dispatcher

object Repository {
    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlace(query)
            if (placeResponse.status == "ok") {
                val places = placeResponse.places
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }
}