package com.roadsense.edge.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    suspend fun getAllEvents(): List<Event>

    @Query("DELETE FROM events")
    suspend fun clearAll()
}
