package com.roadsense.edge.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.roadsense.edge.ai.EventIntegration
import com.roadsense.edge.data.Event
import com.roadsense.edge.R
import kotlinx.android.synthetic.main.activity_driving_mode.*

class DrivingModeActivity : AppCompatActivity() {

    private lateinit var eventIntegration: EventIntegration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driving_mode)

        eventIntegration = EventIntegration(this)

        // Observer koji prati sve događaje i ažurira UI
        eventIntegration.allEvents.observe(this, Observer { events ->
            updateUI(events)
        })
    }

    private fun updateUI(events: List<Event>) {
        val hardBrakes = events.count { it.type == Event.Type.HARD_BRAKE }
        val sharpTurns = events.count { it.type == Event.Type.SHARP_TURN }
        val speeding = events.count { it.type == Event.Type.SPEEDING }

        tvHardBrakes.text = "Nagla kočenja: $hardBrakes"
        tvSharpTurns.text = "Sharp turns: $sharpTurns"
        tvSpeeding.text = "Prekoračenja brzine: $speeding"

        val totalEvents = hardBrakes + sharpTurns + speeding
        tvRiskLevel.text = when {
            totalEvents == 0 -> "Rizik: NIZAK"
            totalEvents in 1..3 -> "Rizik: SREDNJI"
            totalEvents > 3 -> "Rizik: VISOK"
            else -> "Rizik: N/A"
        }
    }
}
