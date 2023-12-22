package com.example.motherload.data.api

import AppDatabase
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherland.MotherLoad
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.HomeCallback
import com.example.motherload.data.callback.InventoryCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

object HomeApi {
    val TAG = "HomeApi"
    private val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"

    //todo faire le traitement des autres
    fun deplacement(session: Long, signature: Long, latitude:Double, longitude:Double, callback: HomeCallback){
        val url = BASE_URL_CREUSER+"deplace.php?session=$session&signature=$signature&lon=$longitude&lat=$latitude"
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
                            Log.d(TAG, "Acces au voisin")
                            val voisin: MutableMap<String, GeoPoint> = mutableMapOf()
                            val listVoisin = doc.getElementsByTagName("VOISINS").item(0).childNodes
                            for (i in 0 until listVoisin.length) {
                                val node = listVoisin.item(i)
                                if (node.nodeType == Node.ELEMENT_NODE) {
                                    val elem = node as Element
                                    val nom = elem.getElementsByTagName("NOM").item(0).textContent
                                    val lat = elem.getElementsByTagName("LATITUDE").item(0).textContent
                                    val lon = elem.getElementsByTagName("LONGITUDE").item(0).textContent
                                    var geoPoint = GeoPoint(lat.toDouble(), lon.toDouble())
                                    Log.d(TAG, "Nom" + nom + "|latitude: "+ lat + "|longitude: "+ lon + "|")
                                    voisin[nom] = geoPoint
                                    callback.deplacement(voisin)
                                }
                            }
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


    fun creuser(session: Long, signature: Long, latitude: Double, longitude: Double, callback: HomeCallback) {
        Log.d(TAG, "session: $session|signature: $signature")
        val url = BASE_URL_CREUSER+"creuse.php?session=$session&signature=$signature&lon=$longitude&lat=$latitude"
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
                            Log.d(TAG, "Creuse")
                            var itemId: String
                            val depth = doc.getElementsByTagName("DEPTH").item(0).textContent.toString()
                            try {
                                itemId = doc.getElementsByTagName("ITEM_ID").item(0).textContent
                            }
                            catch (e: NullPointerException){
                                itemId = "-1"
                            }
                            val voisin: MutableMap<String, GeoPoint> = mutableMapOf()
                            val listVoisin = doc.getElementsByTagName("VOISINS").item(0).childNodes
                            for (i in 0 until listVoisin.length) {
                                val node = listVoisin.item(i)
                                if (node.nodeType == Node.ELEMENT_NODE) {
                                    val elem = node as Element
                                    val nom = elem.getElementsByTagName("NOM").item(0).textContent
                                    val lat =
                                        elem.getElementsByTagName("LATITUDE").item(0).textContent
                                    val lon =
                                        elem.getElementsByTagName("LONGITUDE").item(0).textContent
                                    var geoPoint = GeoPoint(lat.toDouble(), lon.toDouble())
                                    Log.d(
                                        TAG,
                                        "Nom" + nom + "|latitude: " + lat + "|longitude: " + lon + "|"
                                    )
                                    voisin[nom] = geoPoint
                                }
                            }
                            val sharedPreferences: SharedPreferences = MotherLoad.instance.getSharedPreferences("HoleDisplay", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putInt("Depth", depth.toInt())
                            editor.putFloat("Latitude", latitude.toFloat())
                            editor.putFloat("Longitude", longitude.toFloat())
                            editor.apply()
                            Log.d(TAG, "itemId: $itemId | depth: $depth")
                            callback.creuse(itemId.toInt(), depth, voisin)
                        }
                        else if (status.subSequence(0, 14) == "KO  - TOO FAST"){
                            Log.d(TAG, "Trop rapide")
                            callback.erreur(0)
                        }
                        else if (status == "KO  - BAD PICKAXE"){
                            Log.d(TAG, "Trop profond pour cette pioche")
                            callback.erreur(1)
                        }
                        else if(status == "KO - OUT OF BOUNDS"){
                            Log.d(TAG,"En dehors de l'université")
                            callback.erreur(2)
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

    fun getItems(session: Long, signature: Long, items: List<Item>, context: Context, callback: HomeCallback){
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

    fun getDepthHole(): Triple<Float, Float, Int> {
        val sharedPreferences = MotherLoad.instance.getSharedPreferences("HoleDisplay", Context.MODE_PRIVATE)
        val latitude = sharedPreferences.getFloat("Latitude", 0f)
        val longitude = sharedPreferences.getFloat("Longitude", 0f)
        val depth = sharedPreferences.getInt("Depth", 0)
        Log.d(TAG, "latitude: $latitude, longitude: $longitude, depth: $depth, ")
        return Triple(latitude, longitude, depth)
    }
}