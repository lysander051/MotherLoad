package com.example.motherland

import AppDatabase
import android.app.Application
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.example.motherload.data.Repository

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
    }

}