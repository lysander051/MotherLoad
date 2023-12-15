package com.example.motherload.Data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherland.MotherLoad
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class InventoryRepo {
    val TAG: String = "InventoryRepo"
    private val _inventory = MutableLiveData<List<Item>>()
    val inventory: LiveData<List<Item>> get() = _inventory
    private val itemsMap = ArrayList<Item>()


    fun updateInventory(session: Long, signature: Long) {
        val BASE_URL = "https://test.vautard.fr/creuse_srv/status_joueur.php"
        val url = BASE_URL + "?session=$session&signature=$signature"

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
                            val items = doc.getElementsByTagName("ITEMS").item(0).childNodes
                            val money =
                                doc.getElementsByTagName("MONEY").item(0).textContent.toLong()
                            for (i in 0 until items.length) {
                                val node = items.item(i)
                                if (node.nodeType == Node.ELEMENT_NODE) {
                                    val item = node as Element
                                    val id = item.getElementsByTagName("ITEM_ID")
                                        .item(0).textContent.toInt()
                                    val quantity = item.getElementsByTagName("QUANTITE")
                                        .item(0).textContent.toInt()
                                    itemsMap.add(Item(id, quantity))
                                    Log.d(TAG, "id = $id et quantity = $quantity \n")
                                }
                            }
                            _inventory.value = itemsMap
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