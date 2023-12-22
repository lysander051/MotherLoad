package com.example.motherland

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class MotherLoad : Application() {
    companion object {
        lateinit var instance: MotherLoad
            private set
    }

    var requestQueue : RequestQueue? = null
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        requestQueue = Volley.newRequestQueue(this)
        val theme = getSharedPreferences("Settings",0).getInt("theme", AppCompatDelegate.getDefaultNightMode())
        AppCompatDelegate.setDefaultNightMode(theme)
    }

}