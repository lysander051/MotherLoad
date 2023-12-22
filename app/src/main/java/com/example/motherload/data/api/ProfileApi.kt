package com.example.motherload.data.api

import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherland.MotherLoad
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.data.callback.ProfilCallback
import com.example.motherload.data.callback.ShopCallback
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

object ProfileApi {
    val TAG = "InventoryApi"
    private val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"
    //todo faire le traitement des autres
    fun changerPseudo(session: Long, signature: Long, pseudo: String, callback: ProfilCallback){
        val url = BASE_URL_CREUSER+"changenom.php?session=$session&signature=$signature&nom=$pseudo"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(response.byteInputStream())
                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if (statusNode != null) {
                        Log.d(TAG,"nouveau pseudo $pseudo")
                        val status = statusNode.textContent.trim()
                        if (status == "OK" && pseudo.length > 3) {
                            callback.changerPseudo(pseudo)
                        } else {
                            Log.d(TAG, "Erreur - $status")
                            callback.changerPseudo("")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la lecture de la réponse XML", e)
                }
            },
            { error ->
                Log.d(TAG, "profile error")
                error.printStackTrace()
            }
        )

        MotherLoad.instance.requestQueue?.add(stringRequest)
    }

    fun resetUser(session: Long, signature: Long, callback: ProfilCallback){
        val url = BASE_URL_CREUSER+"reinit_joueur.php?session=$session&signature=$signature"
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
                            Log.d(TAG, "Reset player")
                            callback.resetUser()
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

    fun getArtifact(session: Long, signature: Long, callback: ProfilCallback){
        val url = BASE_URL_CREUSER +"artefacts_list.php?session=$session&signature=$signature"
        Log.d(TAG, "session: $session|signature: $signature")

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    Log.d(TAG, url)
                    val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                    val doc: Document = docBuilder.parse(response.byteInputStream())
                    val statusNode = doc.getElementsByTagName("STATUS").item(0)
                    if (statusNode != null) {
                        val status = statusNode.textContent.trim()
                        if (status == "OK") {
                            Log.d(TAG, "Acces inventaire")
                            var items = mutableListOf<Item>()
                            var listItems = doc.getElementsByTagName("ARTEFACTS").item(0).childNodes
                            for (i in 0 until listItems.length) {
                                val node = listItems.item(i)
                                if (node.nodeType == Node.ELEMENT_NODE) {
                                    val elem = node as Element
                                    val id = elem.getElementsByTagName("ID").item(0).textContent.toInt()
                                    items.add(Item(id.toString(),"1"))
                                }
                            }
                            Log.d(TAG, "inventory:${items.size}")
                            callback.getArtifact(items)
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

    fun getItems(session: Long, signature: Long, items: List<Item>, callback: ProfilCallback){
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
}