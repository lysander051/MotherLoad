package com.example.motherload.ui.game.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.motherload.R

class AchatFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val ret = inflater.inflate(R.layout.fragment_item_top_banner, container, false)
        ret.findViewById<LinearLayout>(R.id.backgroundInventory).setBackgroundResource(R.drawable.background_for_list_title_day)

        return ret
    }
}