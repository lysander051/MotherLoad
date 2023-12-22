package com.example.motherload.data.api

import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherland.MotherLoad
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.data.callback.InventoryCallback
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

object InventoryApi {
    val TAG = "InventoryApi"
    private val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"
    //todo faire le traitement des autres
    fun getStatus(session: Long, signature: Long, callback: InventoryCallback){
        val url = BASE_URL_CREUSER+"status_joueur.php?session=$session&signature=$signature"
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
                            Log.d(TAG, "Acces inventaire")
                            var pickaxe = doc.getElementsByTagName("PICKAXE").item(0).textContent.toInt()
                            var money = doc.getElementsByTagName("MONEY").item(0).textContent.toInt()
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
                            Log.d(TAG, "pickaxe:$pickaxe / money:$money / inventory:${items.size}")
                            callback.getStatus(pickaxe,money,items)
                        }
                        else {
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

    fun getItems(session: Long, signature: Long, items: List<Item>, callback: InventoryCallback){
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
                                    callback.getItems(itemDescription)
                                }
                            } else {
                                Log.d(TAG, "Erreur - $status")
                                if (requestCount == items.size) {
                                    callback.getItems(itemDescription)
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

    fun upgradePickaxe(session: Long, signature: Long, pickaxeLevel: Int, callback: InventoryCallback){
        val url = BASE_URL_CREUSER+"maj_pioche.php?session=$session&signature=$signature&pickaxe_id=${pickaxeLevel}"
        Log.d(TAG, url)

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
                            Log.d(TAG, "Amelioration pioche")
                            callback.upgradePickaxe()
                        }
                        else if(status == "KO - NO ITEMS"){
                            callback.erreur(0)
                        }
                        else if(status == "KO - UNKNOWN ID"){
                            callback.erreur(1)
                        }
                        else {
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

    fun recipePickaxe(session: Long, signature: Long, callback: InventoryCallback){
        val url = BASE_URL_CREUSER+"recettes_pioches.php?session=$session&signature=$signature"
        Log.d(TAG, url)

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
                            Log.d(TAG, "Liste recette de pioche")
                            var recipeList = mutableMapOf<String,List<Item>>()
                            var listItem = mutableListOf<Item>()
                            val listPickaxe = doc.getElementsByTagName("UPGRADES").item(0).childNodes
                            Log.d(TAG, "taille liste pickaxe: ${listPickaxe.length}")
                            for (i in 0 until listPickaxe.length) {
                                val pickaxeNode = listPickaxe.item(i) as Element
                                val pickaxeId = pickaxeNode.getElementsByTagName("PICKAXE_ID").item(0).textContent.trim()
                                val itemsNode = pickaxeNode.getElementsByTagName("ITEMS").item(0)
                                val itemList = itemsNode.childNodes
                                listItem = mutableListOf<Item>()
                                for (j in 0 until itemList.length) {
                                    if (itemList.item(j) is Element) {
                                        val itemNode = itemList.item(j) as Element
                                        val itemId = itemNode.getElementsByTagName("ITEM_ID").item(0).textContent.trim()
                                        val quantity = itemNode.getElementsByTagName("QUANTITY").item(0).textContent.trim()
                                        listItem.add(Item(itemId,quantity))
                                    }
                                }
                                Log.d(TAG, "$pickaxeId, size item ${listItem.size}")
                                recipeList.put(pickaxeId,listItem)
                            }
                            callback.recipePickaxe(recipeList)
                        }
                        else {
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
}