package com.example.motherland.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherload.Data.Item
import com.example.motherload.R
import com.example.motherload.UI.Game.HomeViewModel
import com.example.motherload.UI.Game.InventoryViewModel
import com.example.motherload.UI.Inventory.ItemRecyclerViewAdapter


class InventoryFragment : Fragment() {

    private val iListener : OnListFragmentInteractionListener = object :
        OnListFragmentInteractionListener {
            override fun onListFragmentInteraction(item : Item?) {}
        }

    private var iItems : List<Item>? = null
    private var iAdapter : ItemRecyclerViewAdapter? = null
    private lateinit var viewModel: InventoryViewModel

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item:Item?)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val ret = inflater.inflate(R.layout.fragment_inventory, container, false)
        var retour = ret.findViewById<Button>(R.id.gotoHome)
       // var inventaire = ret.findViewById<ListView>(R.id.inventory)

        retour.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            retour.startAnimation(animation)
            activity?.supportFragmentManager?.popBackStack()
        }
        //val session = activity?.intent?.getLongExtra("SessionId",-1)
        //val signature = activity?.intent?.getLongExtra("Signature",-1)
        //viewModel.updateInventory(session,signature)

        if (ret is RecyclerView){
            val context = ret.context
            val recyclerView = ret
            recyclerView.layoutManager = LinearLayoutManager(context)
            if (iItems == null) iItems = ArrayList()
            if (iAdapter == null) iAdapter = ItemRecyclerViewAdapter(iListener)
            recyclerView.adapter = iAdapter
        }

        return ret
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[InventoryViewModel::class.java]
        viewModel.inventory.observe(viewLifecycleOwner, { inventory ->
            iAdapter?.updateInventory(inventory)
        })
    }


}
