package com.example.motherload.ui.game.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.motherload.R

class ItemFragment:Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val ret = inflater.inflate(R.layout.fragment_item, container, false)
        ret.findViewById<LinearLayout>(R.id.backgroundInventory).setBackgroundResource(R.drawable.background_for_list_title)

        return ret
    }
}