package com.example.motherload.UI.Game

import InventoryAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.example.motherload.Utils.setSafeOnClickListener
import com.squareup.picasso.Picasso


class InventoryFragment : Fragment(), InventoryAdapter.ItemClickListener {
    private var viewModel: InventoryViewModel? = null
    private lateinit var ret: View
    private var lastItemClick: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[InventoryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ret = inflater.inflate(R.layout.fragment_inventory, container, false)
        val retour = ret.findViewById<ImageView>(R.id.boutonRetour)
        val amelioration = ret.findViewById<Button>(R.id.boutonAmelioration)
        val pickaxe = ret.findViewById<ImageView>(R.id.pickaxeImage)

        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.inventoryTitle, ItemFragment())
        transaction.commit()

        viewModel!!.getStatus( object :
            InventoryCallback{
            override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {
                setIterface(pickaxe, money, inventory)
            }
            override fun getItems(itemDescription: MutableList<ItemDescription>) {}
        }
        )

        retour.setSafeOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            retour.startAnimation(animation)
            activity?.supportFragmentManager?.popBackStack()
        }

        amelioration.setSafeOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.amelioration_pioche)
            pickaxe.startAnimation(animation)
        }

        return ret
    }

    private fun setIterface(pickaxe: Int, money: Int, inventory: List<Item>){
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
                setItems(itemDescription)
            }
        }
        )
    }

    private fun setItems(listItemDescription: List<ItemDescription>){
        Log.d("coucou",listItemDescription.size.toString())
        val recyclerView: RecyclerView = ret.findViewById(R.id.itemInventory)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = InventoryAdapter(listItemDescription, this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(item: ItemDescription) {
        if (item.id != lastItemClick){
            ret.findViewById<LinearLayout>(R.id.descriptionItems).visibility = View.VISIBLE
            lastItemClick = item.id
            setOneItemDescription(item)
        }
        else{
            ret.findViewById<LinearLayout>(R.id.descriptionItems).visibility = View.GONE
            lastItemClick = ""
        }
    }

    private fun setOneItemDescription(item: ItemDescription){
        val image: ImageView = ret.findViewById(R.id.imageOneItem)
        val name: TextView = ret.findViewById(R.id.nameOneItem)
        val description: TextView = ret.findViewById(R.id.descriptionOneItem)
        val type: TextView = ret.findViewById(R.id.typeOneItem)
        val rarity: TextView = ret.findViewById(R.id.rarityOneItem)
        val quantity: TextView = ret.findViewById(R.id.quantityOneItem)

        Picasso.get().load(item.image).into(image)
        name.text = item.nom
        description.text = item.desc_fr
        if (item.type == "M")
            type.text = "Minerai"
        else
            type.text = "Artefact"
        rarity.text = item.rarity
        quantity.text = item.quantity
    }
}