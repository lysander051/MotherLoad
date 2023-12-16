package com.example.motherload.UI.Game

import InventoryAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherload.Data.InventoryCallback
import com.example.motherload.Data.Item
import com.example.motherload.Data.ItemDescription
import com.example.motherload.R


class InventoryFragment : Fragment() {
    private var viewModel: InventoryViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[InventoryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val ret = inflater.inflate(R.layout.fragment_inventory, container, false)
        val retour = ret.findViewById<ImageView>(R.id.boutonRetour)

        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.inventoryTitle, ItemFragment())
        transaction.commit()

        viewModel!!.getStatus( object :
            InventoryCallback{
            override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {
                setIterface(pickaxe, money, inventory, ret)
            }
            override fun getItems(itemDescription: MutableList<ItemDescription>) {}
        }
        )

        retour.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            retour.startAnimation(animation)
            activity?.supportFragmentManager?.popBackStack()
        }

        return ret
    }

    private fun setIterface(pickaxe: Int, money: Int, inventory: List<Item>, ret: View){
        ret.findViewById<TextView>(R.id.moneyInBank).text = money.toString()
        var pick = R.drawable.pickaxe_1
        if (pickaxe==2) pick = R.drawable.pickaxe_2
        if (pickaxe==3) pick = R.drawable.pickaxe_3
        if (pickaxe==4) pick = R.drawable.pickaxe_4
        if (pickaxe==5) pick = R.drawable.pickaxe_5
        ret.findViewById<ImageView>(R.id.pickaxeImage).setImageResource(pick)
        viewModel!!.getItems(inventory, object :
            InventoryCallback{
            override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {}
            override fun getItems(itemDescription: MutableList<ItemDescription>) {
                setItems(itemDescription,ret)
            }
        }
        )


    }

    private fun setItems(listItemDescription: List<ItemDescription>, ret: View){
        Log.d("coucou",listItemDescription.size.toString())
        val recyclerView: RecyclerView = ret.findViewById(R.id.itemInventory)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = InventoryAdapter(listItemDescription)
        recyclerView.adapter = adapter
    }
}
