package com.example.motherload.ui.game.shop

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherload.R
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.data.callback.ShopCallback
import com.example.motherload.utils.PopUpDisplay
import com.example.motherload.utils.setSafeOnClickListener

class ShopFragment : Fragment(), ShopAchatAdapter.ShopItemClickListener, ShopVenteAdapter.ShopItemClickListener {
    private var viewModel: ShopViewModel? = null
    private lateinit var ret: View
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var isRefreshing = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[ShopViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ret = inflater.inflate(R.layout.fragment_shop, container, false)
        val retour = ret.findViewById<ImageView>(R.id.boutonRetour)
        val achat = ret.findViewById<Button>(R.id.buttonAchat)
        val vente = ret.findViewById<Button>(R.id.buttonVendre)

        buyDiplay()

        achat.setSafeOnClickListener {
            buyDiplay()
        }

        vente.setSafeOnClickListener {
            stopRefreshing()
            sellDiplay()
        }

        retour.setSafeOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            retour.startAnimation(animation)
            activity?.supportFragmentManager?.popBackStack()
        }

        return ret
    }

    fun buyDiplay(){
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.inventoryTitle, AchatFragment())
        transaction.commit()

        isRefreshing = true // Indique que le rafraîchissement est en cours
        // Exécutez le rafraîchissement périodique toutes les 5 secondes
        handler.postDelayed(object : Runnable {
            override fun run() {
                viewModel!!.getMarketItems(object :
                    ShopCallback {
                    override fun getMarketItems(items: List<Triple<Int, Item, Int>>) {
                        val listItems = mutableListOf<Item>()
                        for (i in items){
                            listItems.add(i.second)
                        }
                        viewModel!!.getItemsDescription(listItems, object :
                            ShopCallback {
                            override fun getMarketItems(items: List<Triple<Int, Item, Int>>) {}
                            override fun getItemsDescription(itemsDescription: MutableList<ItemDescription>) {

                                val updatedItems = items.map { item ->
                                    val correspondingItemDescription = itemsDescription.find { it.id == item.second.id && it.quantity == item.second.quantity}
                                    Triple(item.first, correspondingItemDescription, item.third)
                                }
                                for(e in updatedItems){
                                    e.second?.let { Log.d("coucou", it.quantity) }
                                }
                                setItemsBuy(updatedItems)
                            }
                            override fun getInventory(items: MutableList<Item>) {}
                            override fun buyItem() {}
                            override fun sellItem() {}
                            override fun erreur() {}
                        }
                        )
                    }
                    override fun getItemsDescription(items: MutableList<ItemDescription>) {}
                    override fun getInventory(items: MutableList<Item>) {}
                    override fun buyItem() {}
                    override fun sellItem() {}
                    override fun erreur() {}

                }
                )
                if (isRefreshing) {
                    handler.postDelayed(this, 5000) // Rafraîchir toutes les 5 secondes
                }
            }
        }, 0)
    }
    fun sellDiplay(){
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.inventoryTitle, VenteFragment())
        transaction.commit()

        viewModel!!.getInventory(object :
            ShopCallback {
            override fun getMarketItems(items: List<Triple<Int, Item, Int>>) {}
            override fun getItemsDescription(items: MutableList<ItemDescription>) {}
            override fun buyItem() {}
            override fun sellItem() {}
            override fun erreur() {}
            override fun getInventory(items: MutableList<Item>) {
                viewModel!!.getItemsDescription(items, object :
                    ShopCallback {
                    override fun getMarketItems(items: List<Triple<Int, Item, Int>>) {}
                    override fun getItemsDescription(items: MutableList<ItemDescription>) {
                        setItemsSell(items)
                    }
                    override fun getInventory(items: MutableList<Item>) {}
                    override fun buyItem() {}
                    override fun sellItem() {}
                    override fun erreur() {}
                }
                )
            }
        }
        )
    }
    private fun setItemsBuy(listItemDescription: List<Triple<Int, ItemDescription?, Int>>){
        val recyclerView: RecyclerView = ret.findViewById(R.id.itemAchat)
        val layoutManagerState = recyclerView.layoutManager?.onSaveInstanceState()
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = ShopAchatAdapter(listItemDescription, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager?.onRestoreInstanceState(layoutManagerState)
    }
    private fun setItemsSell(listItemDescription: List<ItemDescription>){
        val recyclerView: RecyclerView = ret.findViewById(R.id.itemAchat)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = ShopVenteAdapter(listItemDescription, this)
        recyclerView.adapter = adapter
    }
    override fun onBuyButtonClick(order_id: Int, item: ItemDescription?, prix: Int) {
        PopUpDisplay.cancellablePopUp(this.requireActivity(),
            getString(R.string.acheter),
            getString(R.string.tes_vous_s_r_de_vouloir_acheter_cet_objet)
            ){ confirmed ->
            if (confirmed) {
                viewModel!!.buyItem(order_id, object :
                    ShopCallback {
                    override fun getMarketItems(items: List<Triple<Int, Item, Int>>) {}
                    override fun getItemsDescription(items: MutableList<ItemDescription>) {}
                    override fun getInventory(items: MutableList<Item>) {}
                    override fun sellItem() {}
                    override fun erreur() {
                        PopUpDisplay.simplePopUp(requireActivity(),
                            "Manque d'argent",
                            "Vous n'avez pas suffisament d'argent pour acheter cet objet")
                    }
                    override fun buyItem() {
                        buyDiplay()
                        PopUpDisplay.simplePopUp(requireActivity(),
                            getString(R.string.objet_achet),
                            getString(R.string.vous_avez_bien_achet, item?.quantity) +
                                    "${item?.nom} " +
                                    getString(R.string.pour_le_prix_de, prix.toString()))
                    }
                }
                )
            }
        }
    }
    override fun onSellButtonClick(quantity: Int, item: ItemDescription?, prix: Int) {
        PopUpDisplay.cancellablePopUp(this.requireActivity(),
            getString(R.string.vendre),
            getString(R.string.tes_vous_s_r_de_vouloir_vendre_cet_objet)
        ){ confirmed ->
            if (confirmed) {
                viewModel!!.sellItem(quantity, item?.id, prix, object :
                    ShopCallback {
                    override fun getMarketItems(items: List<Triple<Int, Item, Int>>) {}
                    override fun getItemsDescription(items: MutableList<ItemDescription>) {}
                    override fun getInventory(items: MutableList<Item>) {}
                    override fun buyItem() {}
                    override fun erreur() {}
                    override fun sellItem() {
                        sellDiplay()
                        PopUpDisplay.simplePopUp(requireActivity(),
                            getString(R.string.objet_vendu),
                            getString(R.string.vous_avez_bien_vendu, quantity.toString()) +
                                    "${item?.nom} " +
                                    getString(R.string.pour_le_prix_de, prix.toString()))
                    }
                }
                )
            }
        }
    }
    private fun stopRefreshing() {
        // Arrêtez le rafraîchissement s'il est en cours
        if (isRefreshing) {
            handler.removeCallbacksAndMessages(null)
            isRefreshing = false
        }
    }

    override fun onPause() {
        super.onPause()
        stopRefreshing()
    }
}