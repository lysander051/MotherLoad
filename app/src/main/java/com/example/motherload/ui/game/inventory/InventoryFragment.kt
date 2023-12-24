package com.example.motherload.ui.game.inventory

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motherload.injection.ViewModelFactory
import com.example.motherland.MotherLoad
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.R
import com.example.motherload.data.callback.ItemCallback
import com.example.motherload.utils.PopUpDisplay
import com.example.motherload.utils.setSafeOnClickListener
import com.squareup.picasso.Picasso


/**
 * @property viewModel le ViewModel utilisé par le fragment
 * @property ret la vue du fragment
 * @property lastItemClick l'id du dernier objet cliqué (dont son détail est donc affiché)
 * @property pickaxeLevel le niveau de la pioche
 * @property recipeString chaîne de charactère pour stocker les différentes recettes de pioche
 */
class InventoryFragment : Fragment(), InventoryAdapter.ItemClickListener {
    private var viewModel: InventoryViewModel? = null
    private lateinit var ret: View
    private var lastItemClick: String = ""
    private var pickaxeLevel = 0
    private var recipeString = ""
    private lateinit var amelioration: Button

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
        amelioration = ret.findViewById<Button>(R.id.boutonAmelioration)
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

        amelioration.setSafeOnClickListener {
            //Si on clique sur améliorer la pioche on fait la requête à l'aide du ViewModel
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
            refreshInterface()
        }

        recette.setSafeOnClickListener {
            //Si on clique sur les recettes on fait la requête pour les récupérer
            viewModel!!.recipePickaxe(object : InventoryCallback {
                override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {}
                override fun upgradePickaxe() {}
                override fun erreur(erreurId: Int) {}
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

    /**
     * Permet de refresh le contenu de l'inventaire
     */
    private fun refreshInterface(){
        viewModel!!.getStatus( object :
            InventoryCallback {
            override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {
                pickaxeLevel = pickaxe
                viewModel!!.recipePickaxe(object : InventoryCallback {
                    override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {}
                    override fun upgradePickaxe() {}
                    override fun erreur(erreurId: Int) {}
                    override fun recipePickaxe(recipeList: MutableMap<String, List<Item>>) {
                        val keys = recipeList.keys.toList()
                        val keyFound = keys.find {
                            var b : Boolean = false
                            for (e in keys){
                                b = b || (e == (pickaxeLevel + 1).toString())
                            }
                            if (!b) {
                                amelioration.isEnabled = false
                                amelioration.setText(getString(R.string.pioche_au_max))
                            }
                            b  // Retourne la valeur de b
                        }
                    }
                }, requireActivity())
                setIterface(money, inventory)
            }
            override fun upgradePickaxe() {}
            override fun erreur(erreurId: Int) {}
            override fun recipePickaxe(recipe: MutableMap<String, List<Item>>) {}
        }
        , requireActivity())
    }

    /**
     * Affiche dans une pop-up les différentes recettes de pioches possible
     */
    private fun recipePickaxeDisplay(keys: List<String>, index: Int, recipeList: MutableMap<String, List<Item>>) {
        if (index < recipeList.size) {
            recipeString += "Pickaxe ${keys[index]} :\n"
            recipeList[keys[index]]?.let {
                viewModel!!.getItems(it, object : ItemCallback {
                    override fun getItemsDescription(itemDescription: MutableList<ItemDescription>) {
                        val updatedItems : List<ItemDescription?> = recipeList[keys[index]]!!.map { item ->
                            val correspondingItemDescription = itemDescription.find { it.id == item.id}
                            correspondingItemDescription?.quantity = item.quantity
                            correspondingItemDescription
                        }
                        for (e in updatedItems) {
                            recipeString += "${e!!.quantity} * ${e.nom}\n"
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

    /**
     * Initialise l'interface avec son contenu
     *
     * @param money l'argent du joueur
     * @param inventory la liste d'objet du joueur
     */
    private fun setIterface(money: Int, inventory: List<Item>){
        ret.findViewById<TextView>(R.id.moneyInBank).text = money.toString()
        setPickaxe()
        viewModel!!.getItems(inventory, object :
            ItemCallback {
            override fun getItemsDescription(itemDescription: MutableList<ItemDescription>) {
                val updatedItems = inventory.map { item ->
                    val correspondingItemDescription = itemDescription.find { it.id == item.id}
                    correspondingItemDescription?.quantity = item.quantity
                    correspondingItemDescription
                }
                setItems(updatedItems)
            }
        }
        , requireActivity())
    }

    /**
     * Définit qu'elle image de pioche utiliser (en fonction du niveau)
     */
    private fun setPickaxe(){
        var pick = R.drawable.pickaxe_5
        if (pickaxeLevel==1) pick = R.drawable.pickaxe_1
        if (pickaxeLevel==2) pick = R.drawable.pickaxe_2
        if (pickaxeLevel==3) pick = R.drawable.pickaxe_3
        if (pickaxeLevel==4) pick = R.drawable.pickaxe_4
        ret.findViewById<ImageView>(R.id.pickaxeImage).setImageResource(pick)
    }

    /**
     * Définit la vue des items complet (ItemDescription)
     *
     * @param listItemDescription la liste d'items à gérer dans la vue
     */
    private fun setItems(listItemDescription: List<ItemDescription?>){
        val recyclerView: RecyclerView = ret.findViewById(R.id.itemInventory)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = InventoryAdapter(listItemDescription, this)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(item: ItemDescription) {
        val descriptionItemsLayout = ret.findViewById<LinearLayout>(R.id.descriptionItems)

        //Si c'est pas le dernier item sur lequel on clique alors on l'affiche
        if (item.id != lastItemClick) {
            descriptionItemsLayout.visibility = View.VISIBLE
            lastItemClick = item.id
            setOneItemDescription(item)
        } else {
            //Sinon si c'est le même que le dernier clique on le cache
            if (descriptionItemsLayout.visibility == View.VISIBLE) {
                descriptionItemsLayout.visibility = View.GONE
                lastItemClick = ""
            } else {
                //Sinon on le rend visible
                descriptionItemsLayout.visibility = View.VISIBLE
                lastItemClick = item.id
                setOneItemDescription(item)
            }
        }
    }

    /**
     * Rempli les champs texte correspondant au détail d'un item
     *
     * @param item l'item dont on défini les champs
     */
    private fun setOneItemDescription(item: ItemDescription){
        val image: ImageView = ret.findViewById(R.id.imageOneItem)
        val name: TextView = ret.findViewById(R.id.nameOneItem)
        val description: TextView = ret.findViewById(R.id.descriptionOneItem)
        val type: TextView = ret.findViewById(R.id.typeOneItem)
        val rarity: TextView = ret.findViewById(R.id.rarityOneItem)
        val quantity: TextView = ret.findViewById(R.id.quantityOneItem)

        Picasso.get().load(item.image).into(image)
        name.text = " " + item.nom
        if (MotherLoad.instance.resources.configuration.locales[0].language == "fr"){
            description.text = " " + item.desc_fr
        }
        else{
            description.text = " " + item.desc_en
        }
        if (item.type == "M")
            type.text = " " + getString(R.string.minerai)
        else
            type.text = " " + getString(R.string.artefact)
        rarity.text = " " + item.rarity
        quantity.text = " " + item.quantity
    }

    /**
     * Gère les différentes erreur renvoyé par la requête d'amélioration de la pioche
     *
     * @param erreurId l'id de l'erreur. 0 s'il manque des objets à l'utilisateur, et 1 si le joueur à une pioche de niveau maximum
     */
    private fun gestionErreur(erreurId :Int){
        if (erreurId == 0){
            viewModel!!.recipePickaxe(object : InventoryCallback {
                override fun getStatus(pickaxe: Int, money: Int, inventory: List<Item>) {}
                override fun upgradePickaxe() {}
                override fun erreur(erreurId: Int) {}
                override fun recipePickaxe(recipeList: MutableMap<String, List<Item>>) {
                    val recette = recipeList[(pickaxeLevel+1).toString()]
                    if (recette != null) {
                        viewModel!!.getItems(recette, object : ItemCallback {
                            override fun getItemsDescription(itemDescription: MutableList<ItemDescription>) {
                                viewModel!!.getStatus(object : InventoryCallback {
                                    override fun getStatus(pickaxe: Int, money: Int, inventoryPlayer: List<Item>) {
                                        viewModel!!.getItems(inventoryPlayer, object : ItemCallback {
                                            override fun getItemsDescription(itemDescriptionPlayer: MutableList<ItemDescription>) {
                                                var missing = getString(R.string.il_vous_manque)
                                                val updatedItemsRequires : List<ItemDescription?> = recette.map { item ->
                                                    val correspondingItemDescription = itemDescription.find { it.id == item.id}
                                                    correspondingItemDescription?.quantity = item.quantity
                                                    correspondingItemDescription
                                                }
                                                val updatedItemsPlayer : List<ItemDescription?> = inventoryPlayer.map { item ->
                                                    val correspondingItemDescription = itemDescriptionPlayer.find { it.id == item.id}
                                                    correspondingItemDescription?.quantity = item.quantity
                                                    correspondingItemDescription
                                                }
                                                for (i in 0 until updatedItemsRequires.size) {
                                                    val requiredQuantity = updatedItemsRequires[i]
                                                    val playerItem = updatedItemsPlayer.find { it!!.id == requiredQuantity!!.id }

                                                    if (playerItem != null) {
                                                        val diff = requiredQuantity!!.quantity.toInt() - playerItem.quantity.toInt()
                                                        if (diff > 0) {
                                                            missing += "$diff ${requiredQuantity.nom}\n"
                                                        }
                                                    } else {
                                                        missing += "${requiredQuantity!!.quantity.toInt()} ${requiredQuantity.nom}\n"
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