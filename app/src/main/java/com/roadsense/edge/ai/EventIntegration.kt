package com.roadsense.edge.ai

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.roadsense.edge.data.Event
import com.roadsense.edge.data.EventDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

class EventIntegration(context: Context) {

    private val eventDetector = EventDetector()
    private val eventDao = EventDatabase.getDatabase(context).eventDao()

    private val _allEvents = MutableLiveData<List<Event>>()
    val allEvents: LiveData<List<Event>> get() = _allEvents

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Pozovi na svakom senzorskom očitavanju
     * accelX/Y/Z -> akcelerometar
     * gyroZ -> giroskop
     * currentSpeed -> GPS trenutna brzina
     * expectedSpeed -> GPS očekivana brzina
     */
    fun processSensorData(
        accelX: Float,
        accelY: Float,
        accelZ: Float,
        gyroZ: Float,
        currentSpeed: Float,
        expectedSpeed: Float
    ) {
        // Akceleracija
        eventDetector.detectAccelerationEvent(accelX, accelY, accelZ)?.let { event ->
            saveEvent(event)
        }

        // Giroskop
        eventDetector.detectGyroEvent(gyroZ)?.let { event ->
            saveEvent(event)
        }

        // Brzina
        eventDetector.detectSpeedEvent(currentSpeed, expectedSpeed)?.let { event ->
            saveEvent(event)
        }
    }

    private fun saveEvent(event: Event) {
        coroutineScope.launch {
            eventDao.insertEvent(event)
            val events = eventDao.getAllEvents()
            _allEvents.postValue(events)
        }
    }
}
