package com.example.motherload.ui.game

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.motherLoad.Utils.AppPermission
import com.example.motherland.MotherLoad
import com.example.motherload.ui.game.home.HomeFragment
import com.example.motherload.R
import com.example.motherload.ui.game.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppPermission.requestLocation(this)
        var transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragmentContainerView, HomeFragment())
        transaction.commit()
    }
}