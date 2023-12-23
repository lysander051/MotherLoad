package com.example.motherload.ui.game.inventory

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherland.MotherLoad
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.R
import com.example.motherload.data.callback.ItemCallback
import com.example.motherload.utils.PopUpDisplay
import com.example.motherload.utils.setSafeOnClickListener
import com.squareup.picasso.Picasso


class InventoryFragment : Fragment(), InventoryAdapter.ItemClickListener {
    private var viewModel: InventoryViewModel? = null
    private lateinit var ret: View
    private var lastItemClick: String = ""
    private var pickaxeLevel = 0
    private var recipeString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[InventoryViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ret = inflater.inflate(R.layout.fragment_inventory, container, false)
        val retour = ret.findViewById<ImageView>(R.id.boutonRetour)
        val amelioration = ret.findViewById<Button>(R.id.boutonAmelioration)
        val pickaxe = ret.findViewById<ImageView>(R.id.pickaxeImage)
        val recette = ret.findViewById<Button>(R.id.boutonRecette)
        val descriptionItemsLayout = ret.findViewById<LinearLayout>(R.id.descriptionItems)


        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.inventoryTitle, ItemFragment())
        transaction.commit()

        refreshInterface()

        retour.setSafeOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            retour.startAnimation(animation)
            activity?.supportFragmentManager?.popBackStack()
        }

        //todo on peut faire une pioche d'un id inéxistant?
        amelioration.setSafeOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.amelioration_pioche)
            viewModel!!.upgradePickaxe(pickaxeLevel, object :
                InventoryCallback {
                override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {}
                override fun upgradePickaxe() {
                    pickaxeLevel++
                    pickaxe.startAnimation(animation)
                    Handler(Looper.getMainLooper()).postDelayed({
                        setPickaxe()
                        refreshInterface()}, 1000)

                }
                override fun recipePickaxe(recipe: MutableMap<String, List<Item>>) {}
                override fun erreur(erreurId: Int) {
                    gestionErreur(erreurId)
                }
            }
            , requireActivity())
        }

        recette.setSafeOnClickListener {
            viewModel!!.recipePickaxe(object : InventoryCallback {
                override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {}
                override fun upgradePickaxe() {}
                override fun erreur(erreurId: Int) {}
                @RequiresApi(Build.VERSION_CODES.O)
                override fun recipePickaxe(recipeList: MutableMap<String, List<Item>>) {
                    val keys = recipeList.keys.toList()
                    for ((key, value) in recipeList){
                        Log.d("inventory", "$key, ${value.size}")
                    }
                    recipePickaxeDisplay(keys, 0, recipeList)
                }
            }, requireActivity())
        }

        descriptionItemsLayout.setOnClickListener {
            // Toggle visibility
            descriptionItemsLayout.visibility = if (descriptionItemsLayout.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        return ret
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshInterface(){
        viewModel!!.getStatus( object :
            InventoryCallback {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {
                pickaxeLevel = pickaxe
                setIterface(money, inventory)
            }
            override fun upgradePickaxe() {}
            override fun erreur(erreurId: Int) {}
            override fun recipePickaxe(recipe: MutableMap<String, List<Item>>) {}
        }
        , requireActivity())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun recipePickaxeDisplay(keys: List<String>, index: Int, recipeList: MutableMap<String, List<Item>>) {
        if (index < recipeList.size) {
            recipeString += "Pickaxe ${keys[index]} :\n"
            recipeList[keys[index]]?.let {
                viewModel!!.getItems(it, object : ItemCallback {
                    override fun getItemsDescription(itemDescription: MutableList<ItemDescription>) {
                        for (e in itemDescription) {
                            recipeString += "${e.quantity} * ${e.nom}\n"
                            if (index < recipeList.size-1) {
                                recipeString += "--------------------------\n"
                            }
                        }
                        recipePickaxeDisplay(keys, index + 1, recipeList)
                    }
                }, requireActivity())
            }

        } else {
            if (index == recipeList.size) {
                PopUpDisplay.simplePopUp(requireActivity(),
                    getString(R.string.recette), recipeString)
                recipeString = ""
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setIterface(money: Int, inventory: List<Item>){
        ret.findViewById<TextView>(R.id.moneyInBank).text = money.toString()
        setPickaxe()
        viewModel!!.getItems(inventory, object :
            ItemCallback {
            override fun getItemsDescription(itemDescription: MutableList<ItemDescription>) {
                val updatedItems = inventory.map { item ->
                    var correspondingItemDescription = itemDescription.find { it.id == item.id}
                    correspondingItemDescription?.quantity = item.quantity
                    correspondingItemDescription
                }
                setItems(updatedItems)
            }
        }
        , requireActivity())
    }

    private fun setPickaxe(){
        var pick = R.drawable.pickaxe_5
        if (pickaxeLevel==1) pick = R.drawable.pickaxe_1
        if (pickaxeLevel==2) pick = R.drawable.pickaxe_2
        if (pickaxeLevel==3) pick = R.drawable.pickaxe_3
        if (pickaxeLevel==4) pick = R.drawable.pickaxe_4
        ret.findViewById<ImageView>(R.id.pickaxeImage).setImageResource(pick)
    }

    private fun setItems(listItemDescription: List<ItemDescription?>){
        val recyclerView: RecyclerView = ret.findViewById(R.id.itemInventory)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = InventoryAdapter(listItemDescription, this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(item: ItemDescription) {
        val descriptionItemsLayout = ret.findViewById<LinearLayout>(R.id.descriptionItems)

        if (item.id != lastItemClick) {
            descriptionItemsLayout.visibility = View.VISIBLE
            lastItemClick = item.id
            setOneItemDescription(item)
        } else {
            if (descriptionItemsLayout.visibility == View.VISIBLE) {
                descriptionItemsLayout.visibility = View.GONE
                lastItemClick = ""
            } else {
                descriptionItemsLayout.visibility = View.VISIBLE
                lastItemClick = item.id
                setOneItemDescription(item)
            }
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
        if (MotherLoad.instance.resources.configuration.locales[0].language == "fr"){
            description.text = item.desc_fr
        }
        else{
            description.text = item.desc_en
        }
        if (item.type == "M")
            type.text = getString(R.string.minerai)
        else
            type.text = getString(R.string.artefact)
        rarity.text = item.rarity
        quantity.text = item.quantity
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun gestionErreur(erreurId :Int){
        if (erreurId == 0){
            viewModel!!.recipePickaxe(object : InventoryCallback {
                override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {}
                override fun upgradePickaxe() {}
                override fun erreur(erreurId: Int) {}
                @RequiresApi(Build.VERSION_CODES.O)
                override fun recipePickaxe(recipeList: MutableMap<String, List<Item>>) {
                    val recette = recipeList[(pickaxeLevel+1).toString()]
                    if (recette != null) {
                        viewModel!!.getItems(recette, object : ItemCallback {
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun getItemsDescription(itemDescription: MutableList<ItemDescription>) {
                                viewModel!!.getStatus(object : InventoryCallback {
                                    @RequiresApi(Build.VERSION_CODES.O)
                                    override fun getStatus(pickaxe: Int, money: Int, inventoryPlayer: List<Item>) {
                                        viewModel!!.getItems(inventoryPlayer, object : ItemCallback {
                                            override fun getItemsDescription(itemDescriptionPlayer: MutableList<ItemDescription>) {
                                                var missing = getString(R.string.il_vous_manque)
                                                for (i in 0 until itemDescription.size) {
                                                    val requiredQuantity = itemDescription[i]
                                                    val playerItem = itemDescriptionPlayer.find { it.id == requiredQuantity.id }

                                                    if (playerItem != null) {
                                                        val diff = requiredQuantity.quantity.toInt() - playerItem.quantity.toInt()
                                                        if (diff > 0) {
                                                            missing += "$diff ${requiredQuantity.nom}\n"
                                                        }
                                                    } else {
                                                        missing += "${requiredQuantity.quantity.toInt()} ${requiredQuantity.nom}\n"
                                                    }
                                                }
                                                PopUpDisplay.simplePopUp(requireActivity(),
                                                    getString(
                                                        R.string.objet_manquant
                                                    ), missing)
                                            }
                                        }, requireActivity())
                                    }
                                    override fun upgradePickaxe() {}
                                    override fun erreur(erreurId: Int) {}
                                    override fun recipePickaxe(recipeList: MutableMap<String, List<Item>>) {}
                                }, requireActivity())
                            }
                        }, requireActivity())
                    }
                }
            }, requireActivity())
        }
        if (erreurId == 1){
            PopUpDisplay.simplePopUp(requireActivity(),
                getString(R.string.f_licitation),
                getString(R.string.bravo_votre_pioche_est_am_lior_au_maximum))
        }
    }
}