package com.example.motherload.UI.Game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.motherLoad.Utils.AppPermission
import com.example.motherload.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppPermission.requestLocation(this)
    }

}