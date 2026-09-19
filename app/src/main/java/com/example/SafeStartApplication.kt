package com.example

import android.app.Application
import com.example.data.SafeStartRepository

class SafeStartApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SafeStartRepository.initialize(this)
    }
}
