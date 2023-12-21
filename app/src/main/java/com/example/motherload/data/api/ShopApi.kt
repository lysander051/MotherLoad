package com.example.motherload.data.api

import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherland.MotherLoad
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.data.callback.ShopCallback
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

object ShopApi {
    val TAG = "ShopApi"
    private val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"

    fun getMarketItems(session: Long, signature: Long, callback: ShopCallback) {
        val url = BASE_URL_CREUSER + "market_list.php?session=$session&signature=$signature"
        Log.d(TAG, "session: $session|signature: $signature")

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(response.byteInputStream())
                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if (statusNode != null) {
                        val status = statusNode.textContent.trim()
                        if (status == "OK") {
                            Log.d(TAG, "Acces market")
                            Log.d(TAG, url)
                            var items = mutableListOf<Triple<Int, Item, Int>>()
                            var listItems = doc.getElementsByTagName("OFFERS").item(0).childNodes
                            for (i in 0 until listItems.length) {
                                val node = listItems.item(i)
                                if (node.nodeType == Node.ELEMENT_NODE) {
                                    val elem = node as Element
                                    val offer_id = elem.getElementsByTagName("OFFER_ID").item(0).textContent.toInt()
                                    val item_id = elem.getElementsByTagName("ITEM_ID").item(0).textContent.toInt()
                                    val quantity = elem.getElementsByTagName("QUANTITE").item(0).textContent.toInt()
                                    val price = elem.getElementsByTagName("PRIX").item(0).textContent.toInt()
                                    items.add(Triple(offer_id.toInt(), Item(item_id.toString(),quantity.toString()), price.toInt()))
                                }
                            }
                            callback.getMarketItems(items)
                        } else {
                            Log.d(TAG, "Erreur - $status")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la lecture de la réponse XML", e)
                }
            },
            { error ->
                Log.d(TAG, "connexion error")
                error.printStackTrace()
            }
        )
        MotherLoad.instance.requestQueue?.add(stringRequest)
    }
    fun getItems(session: Long, signature: Long, items: List<Item>, callback: ShopCallback){
        var itemDescription = mutableListOf<ItemDescription>()
        var requestCount = 0
        for (e in items) {
            val url = BASE_URL_CREUSER + "item_detail.php?session=$session&signature=$signature&item_id=${e.id}"

            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    try {
                        val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                        val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                        val doc: Document = docBuilder.parse(response.byteInputStream())
                        val statusNode = doc.getElementsByTagName("STATUS").item(0)
                        if (statusNode != null) {
                            val status = statusNode.textContent.trim()
                            if (status == "OK") {
                                val nom =doc.getElementsByTagName("NOM").item(0).textContent.toString()
                                val type =doc.getElementsByTagName("TYPE").item(0).textContent.toString()
                                val rarity = doc.getElementsByTagName("RARETE").item(0).textContent.toString()
                                val image = "https://test.vautard.fr/creuse_imgs/" + doc.getElementsByTagName("IMAGE").item(0).textContent.toString()
                                val desc_fr = doc.getElementsByTagName("DESC_FR").item(0).textContent.toString()
                                val desc_en = doc.getElementsByTagName("DESC_EN").item(0).textContent.toString()
                                itemDescription.add(ItemDescription(e.id, nom, type, rarity,image,desc_fr,desc_en,e.quantity))
                                requestCount++
                                if (requestCount == items.size) {
                                    callback.getItemsDescription(itemDescription)
                                }
                            } else {
                                Log.d(TAG, "Erreur - $status")
                                if (requestCount == items.size) {
                                    callback.getItemsDescription(itemDescription)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erreur lors de la lecture de la réponse XML", e)
                    }
                },
                { error ->
                    Log.d(TAG, "connexion error")
                    error.printStackTrace()
                }
            )
            MotherLoad.instance.requestQueue?.add(stringRequest)
        }
    }

    fun getInventory(session: Long, signature: Long, callback: ShopCallback){
        val url = BASE_URL_CREUSER +"status_joueur.php?session=$session&signature=$signature"
        Log.d(TAG, "session: $session|signature: $signature")

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(response.byteInputStream())
                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if (statusNode != null) {
                        val status = statusNode.textContent.trim()
                        if (status == "OK") {
                            Log.d(InventoryApi.TAG, "Acces inventaire")
                            var items = mutableListOf<Item>()

                            var listItems = doc.getElementsByTagName("ITEMS").item(0).childNodes
                            for (i in 0 until listItems.length) {
                                val node = listItems.item(i)
                                if (node.nodeType == Node.ELEMENT_NODE) {
                                    val elem = node as Element
                                    val id = elem.getElementsByTagName("ITEM_ID").item(0).textContent.toInt()
                                    val quantity = elem.getElementsByTagName("QUANTITE").item(0).textContent.toInt()
                                    items.add(Item(id.toString(),quantity.toString()))
                                }
                            }
                            callback.getInventory(items)
                        }
                        else {
                            Log.d(InventoryApi.TAG, "Erreur - $status")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(InventoryApi.TAG, "Erreur lors de la lecture de la réponse XML", e)
                }
            },
            { error ->
                Log.d(InventoryApi.TAG, "connexion error")
                error.printStackTrace()
            }
        )
        MotherLoad.instance.requestQueue?.add(stringRequest)
    }
    fun buyItem(session: Long, signature: Long, order_id: Int, callback: ShopCallback){
        val url = BASE_URL_CREUSER +"market_acheter.php?session=$session&signature=$signature&offer_id=$order_id"
        Log.d(TAG, "session: $session|signature: $signature")

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(response.byteInputStream())
                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if (statusNode != null) {
                        val status = statusNode.textContent.trim()
                        if (status == "OK") {
                            Log.d(InventoryApi.TAG, "Acces buy item")
                            callback.buyItem()
                        }
                        else {
                            Log.d(InventoryApi.TAG, "Erreur - $status")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(InventoryApi.TAG, "Erreur lors de la lecture de la réponse XML", e)
                }
            },
            { error ->
                Log.d(InventoryApi.TAG, "connexion error")
                error.printStackTrace()
            }
        )
        MotherLoad.instance.requestQueue?.add(stringRequest)
    }

    fun sellItem(session: Long, signature: Long, quantity: Int, id: String?, prix: Int, callback: ShopCallback) {
        val url = BASE_URL_CREUSER +"market_vendre.php?session=$session&signature=$signature&item_id=$id&quantite=$quantity&prix=$prix"
        Log.d(TAG, "session: $session|signature: $signature")

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(response.byteInputStream())
                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if (statusNode != null) {
                        val status = statusNode.textContent.trim()
                        if (status == "OK") {
                            Log.d(InventoryApi.TAG, "Acces sell item")
                            callback.sellItem()
                        }
                        else {
                            Log.d(InventoryApi.TAG, "Erreur - $status")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(InventoryApi.TAG, "Erreur lors de la lecture de la réponse XML", e)
                }
            },
            { error ->
                Log.d(InventoryApi.TAG, "connexion error")
                error.printStackTrace()
            }
        )
        MotherLoad.instance.requestQueue?.add(stringRequest)
    }
}