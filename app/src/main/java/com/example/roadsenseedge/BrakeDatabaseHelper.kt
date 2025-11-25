package com.example.roadsenseedge

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class BrakeRecord(
    val timestamp: Long,
    val speed: Float,
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float
)

class BrakeDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "brakes.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_BRAKES = "brakes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_SPEED = "speed"
        private const val COLUMN_ACCEL_X = "accelX"
        private const val COLUMN_ACCEL_Y = "accelY"
        private const val COLUMN_ACCEL_Z = "accelZ"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_BRAKES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TIMESTAMP LONG,
                $COLUMN_SPEED REAL,
                $COLUMN_ACCEL_X REAL,
                $COLUMN_ACCEL_Y REAL,
                $COLUMN_ACCEL_Z REAL
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BRAKES")
        onCreate(db)
    }

    fun insertBrake(timestamp: Long, speed: Float, accelX: Float, accelY: Float, accelZ: Float) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_SPEED, speed)
            put(COLUMN_ACCEL_X, accelX)
            put(COLUMN_ACCEL_Y, accelY)
            put(COLUMN_ACCEL_Z, accelZ)
        }
        db.insert(TABLE_BRAKES, null, values)
        db.close()
    }

    fun getAllBrakes(): List<BrakeRecord> {
        val list = mutableListOf<BrakeRecord>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BRAKES,
            arrayOf(COLUMN_TIMESTAMP, COLUMN_SPEED, COLUMN_ACCEL_X, COLUMN_ACCEL_Y, COLUMN_ACCEL_Z),
            null, null, null, null,
            "$COLUMN_TIMESTAMP DESC"
        )
        if (cursor.moveToFirst()) {
            do {
                val brake = BrakeRecord(
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                    speed = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SPEED)),
                    accelX = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ACCEL_X)),
                    accelY = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ACCEL_Y)),
                    accelZ = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_ACCEL_Z))
                )
                list.add(brake)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}
