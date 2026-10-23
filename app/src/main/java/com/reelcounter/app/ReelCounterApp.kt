package com.reelcounter.app

import android.app.Application
import com.reelcounter.app.data.local.ReelDatabase

class ReelCounterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize database eagerly
        ReelDatabase.getInstance(this)
    }
}
