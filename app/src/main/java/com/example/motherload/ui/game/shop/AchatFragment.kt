package com.example.motherload.ui.game.shop

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.motherland.MotherLoad
import com.example.motherload.R

class AchatFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val ret = inflater.inflate(R.layout.fragment_item_top_banner, container, false)
        //Selon la version de l'Api du device on peut connaître si le mode nuit est actif
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (resources.configuration.isNightModeActive) {
                ret.findViewById<LinearLayout>(R.id.backgroundInventory)
                    .setBackgroundResource(R.drawable.background_for_list_title_night)
            } else {
                ret.findViewById<LinearLayout>(R.id.backgroundInventory)
                    .setBackgroundResource(R.drawable.background_for_list_title_day)
            }
        } else {
            val sharedPref =
                MotherLoad.instance.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            if (sharedPref.getInt("theme", 1) == 2) {
                ret.findViewById<LinearLayout>(R.id.backgroundInventory)
                    .setBackgroundResource(R.drawable.background_for_list_title_night)
            } else {
                ret.findViewById<LinearLayout>(R.id.backgroundInventory)
                    .setBackgroundResource(R.drawable.background_for_list_title_day)
            }

        }

        return ret
    }
}