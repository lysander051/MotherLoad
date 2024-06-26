package com.example.motherload.ui.game.shop

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motherload.injection.ViewModelFactory
import com.example.motherload.R
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.data.callback.ItemCallback
import com.example.motherload.data.callback.ShopCallback
import com.example.motherload.utils.PopUpDisplay
import com.example.motherload.utils.setSafeOnClickListener

/**
 * @property viewModel le ViewModel utilisé par le fragment
 * @property ret la vue affichée par le fragment
 * @property handler un handler pour l'actualisation périodique du shop
 * @property isRefreshing true si le raffraichissement du shop est en cours
 */
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
    ): View {
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

    /**
     * Gère l'affichage de l'onglet d'achat du shop
     */
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
                            ItemCallback {
                            override fun getItemsDescription(itemsDescription: MutableList<ItemDescription>) {
                                val updatedItems = mutableListOf<Triple<Int, ItemDescription, Int>>()
                                for (j in items) {
                                    itemsDescription.find { it.id == j.second.id }?.let { i ->
                                        val newItemDescription = ItemDescription(i.id, i.nom, i.type,i.rarity,i.image,i.desc_fr,i.desc_en,j.second.quantity)
                                        updatedItems.add(Triple(j.first, newItemDescription, j.third))
                                    }
                                }
                                setItemsBuy(updatedItems)
                            }
                        }
                        , requireActivity())
                    }
                    override fun getInventory(items: MutableList<Item>) {}
                    override fun buyItem() {}
                    override fun sellItem() {}
                    override fun erreur() {}

                }
                , requireActivity())
                if (isRefreshing) {
                    handler.postDelayed(this, 10000) // Rafraîchir toutes les 5 secondes
                }
            }
        }, 0)
    }

    /**
     * Gère l'affichage de l'onglet de vente du shop
     */
    fun sellDiplay(){
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.inventoryTitle, VenteFragment())
        transaction.commit()

        viewModel!!.getInventory(object :
            ShopCallback {
            override fun getMarketItems(items: List<Triple<Int, Item, Int>>) {}
            override fun buyItem() {}
            override fun sellItem() {}
            override fun erreur() {}
            override fun getInventory(items: MutableList<Item>) {
                viewModel!!.getItemsDescription(items, object :
                    ItemCallback {
                    override fun getItemsDescription(itemsDescription: MutableList<ItemDescription>) {
                        val updatedItems = items.map { item ->
                            val correspondingItemDescription = itemsDescription.find { it.id == item.id}
                            correspondingItemDescription?.quantity = item.quantity
                            correspondingItemDescription
                        }
                        setItemsSell(updatedItems)
                    }
                }
                , requireActivity())
            }
        }
        , requireActivity())
    }

    /**
     * Définit les offres achetable
     *
     * @param items la liste des offres (id de l'offre, item, prix)
     */
    private fun setItemsBuy(items: List<Triple<Int, ItemDescription?, Int>>){
        val recyclerView: RecyclerView = ret.findViewById(R.id.itemAchat)
        val layoutManagerState = recyclerView.layoutManager?.onSaveInstanceState()
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = ShopAchatAdapter(items, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager?.onRestoreInstanceState(layoutManagerState)
    }

    /**
     * Définit les items vendable par le joueur
     *
     * @param listItemDescription les items du joueurs
     */
    private fun setItemsSell(listItemDescription: List<ItemDescription?>){
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
                    override fun getInventory(items: MutableList<Item>) {}
                    override fun sellItem() {}
                    override fun erreur() {
                        PopUpDisplay.simplePopUp(requireActivity(),
                            getString(R.string.manque_d_argent),
                            getString(R.string.vous_n_avez_pas_suffisament_d_argent_pour_acheter_cet_objet))
                    }
                    override fun buyItem() {
                        buyDiplay()
                        PopUpDisplay.simplePopUp(requireActivity(),
                            getString(R.string.objet_achet),
                            getString(R.string.vous_avez_bien_achet, item?.quantity) +
                                    " ${item?.nom} " +
                                    getString(R.string.pour_le_prix_de, prix.toString()))
                    }
                }
                , requireActivity())
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
                , requireActivity())
            }
        }
    }

    /**
     * Stop le raffraichissement du shop
     */
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