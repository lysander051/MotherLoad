package com.example.motherload.UI.Game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.addCallback
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.LifecycleOwner
import com.example.motherLoad.Utils.AppPermission
import com.example.motherland.view.HomeFragment
import com.example.motherload.R

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