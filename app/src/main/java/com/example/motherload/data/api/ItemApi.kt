package com.example.motherload.data.api

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherLoad.Utils.LoginManager
import com.example.motherland.MotherLoad
import com.example.motherload.Model.AppDatabase
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.data.callback.ConnexionCallback
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.data.callback.ItemCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

object ItemApi {
    val TAG = "ItemApi"
    private val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"
    @RequiresApi(Build.VERSION_CODES.O)
    fun getItems(
        session: Long,
        signature: Long,
        items: List<Item>,
        context: Context,
        callback: ItemCallback,
        activity: Activity
    ) {
        var itemDescription = mutableListOf<ItemDescription>()
        var requestCount = 0

        // Create a Room database instance
        val database = AppDatabase.getDatabase(context)
        val itemDescriptionDao = database.itemDescriptionDao()

        for (e in items) {
            val url = BASE_URL_CREUSER + "item_detail.php?session=$session&signature=$signature&item_id=${e.id}"

            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            val docBF: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                            val docBuilder: DocumentBuilder = docBF.newDocumentBuilder()
                            val doc: Document = docBuilder.parse(response.byteInputStream())
                            val statusNode = doc.getElementsByTagName("STATUS").item(0)
                            if (statusNode != null) {
                                val status = statusNode.textContent.trim()
                                if (status == "OK") {
                                    val nom = doc.getElementsByTagName("NOM")
                                        .item(0).textContent.toString()
                                    val type = doc.getElementsByTagName("TYPE")
                                        .item(0).textContent.toString()
                                    val rarity = doc.getElementsByTagName("RARETE")
                                        .item(0).textContent.toString()
                                    val image =
                                        "https://test.vautard.fr/creuse_imgs/" + doc.getElementsByTagName(
                                            "IMAGE"
                                        ).item(0).textContent.toString()
                                    val desc_fr = doc.getElementsByTagName("DESC_FR")
                                        .item(0).textContent.toString()
                                    val desc_en = doc.getElementsByTagName("DESC_EN")
                                        .item(0).textContent.toString()
                                    val itemDesc = ItemDescription(
                                        e.id,
                                        nom,
                                        type,
                                        rarity,
                                        image,
                                        desc_fr,
                                        desc_en,
                                        e.quantity
                                    )
                                    itemDescriptionDao.insertItem(itemDesc)
                                    itemDescription.add(itemDesc)

                                    requestCount++
                                    if (requestCount == items.size) {
                                        // Callback with the list of ItemDescription
                                        launch(Dispatchers.Main) {
                                            callback.getItemsDescription(itemDescription)
                                        }
                                    }
                                }
                                else if (status == "KO - SESSION INVALID" || status == "KO - SESSION EXPIRED"){
                                    ConnexionApi.connectAgain(object : ConnexionCallback {
                                        override fun onConnexion(isConnected: Boolean) {
                                            LoginManager.checkReconnexion(activity, isConnected)
                                        }
                                    })
                                }
                                else {
                                    Log.d(TAG, "Erreur - $status")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Erreur lors de la lecture de la réponse XML", e)
                        }
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