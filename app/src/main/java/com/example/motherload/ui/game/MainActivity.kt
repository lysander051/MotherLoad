package com.example.motherload.ui.game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.motherLoad.Utils.AppPermission
import com.example.motherload.ui.game.home.HomeFragment
import com.example.motherload.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragmentContainerView, HomeFragment())
        transaction.commit()
    }
}