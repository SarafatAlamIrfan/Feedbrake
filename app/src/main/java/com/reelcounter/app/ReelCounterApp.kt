package com.reelcounter.app

import android.app.Application
import com.reelcounter.app.data.repository.ReelRepository

class ReelCounterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReelRepository.getInstance(this)
    }
}
