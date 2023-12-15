package com.example.motherload.UI.Game

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherload.Data.InventoryCallback
import com.example.motherload.Data.Item
import com.example.motherload.R


class InventoryFragment : Fragment() {
    private var viewModel: InventoryViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[InventoryViewModel::class.java]
        viewModel!!.getStatus( object :
            InventoryCallback{
                override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {
                    makeListItems(pickaxe, money, inventory)
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val ret = inflater.inflate(R.layout.fragment_inventory, container, false)
        val retour = ret.findViewById<ImageView>(R.id.boutonRetour)
        val inventory = ret.findViewById<RecyclerView>(R.id.itemInventory)

        retour.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            retour.startAnimation(animation)
            activity?.supportFragmentManager?.popBackStack()
        }

        return ret
    }

    private fun makeListItems(pickaxe: Int, money: Int, inventory: List<Item>){

    }
}
