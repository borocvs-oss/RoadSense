package com.roadsense.edge.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: Type,
    val value: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Type {
        HARD_BRAKE,
        SHARP_TURN,
        SPEEDING
    }
}
