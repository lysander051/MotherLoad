package com.example.motherload.ui.game.inventory

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.motherland.MotherLoad
import com.example.motherload.R

class ItemFragment:Fragment() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val ret = inflater.inflate(R.layout.fragment_item, container, false)
        if (resources.configuration.isNightModeActive){
            ret.findViewById<LinearLayout>(R.id.backgroundInventory).setBackgroundResource(R.drawable.background_for_list_title_night)
        }
        else{
            ret.findViewById<LinearLayout>(R.id.backgroundInventory).setBackgroundResource(R.drawable.background_for_list_title_day)
        }

        return ret
    }
}