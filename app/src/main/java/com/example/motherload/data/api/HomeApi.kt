package com.example.motherload.data.api

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherLoad.Utils.LoginManager
import com.example.motherland.MotherLoad
import com.example.motherload.data.callback.ConnexionCallback
import com.example.motherload.data.callback.HomeCallback
import org.osmdroid.util.GeoPoint
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

object HomeApi {
    private const val TAG = "HomeApi"
    private const val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"

    fun deplacement(session: Long, signature: Long, latitude:Double, longitude:Double, callback: HomeCallback, activity: Activity){
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
                                    val geoPoint = GeoPoint(lat.toDouble(), lon.toDouble())
                                    Log.d(TAG, "Nom: $nom|latitude: $lat |longitude: $lon|")
                                    voisin[nom] = geoPoint
                                    callback.deplacement(voisin)
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
            },
            { error ->
                Log.d(TAG, "connexion error")
                error.printStackTrace()
            }
        )
        MotherLoad.instance.requestQueue?.add(stringRequest)
    }


    fun creuser(session: Long, signature: Long, latitude: Double, longitude: Double, callback: HomeCallback, activity: Activity) {
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
                                    val geoPoint = GeoPoint(lat.toDouble(), lon.toDouble())
                                    Log.d(
                                        TAG,
                                        "Nom: $nom|latitude: $lat|longitude: $lon|"
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
            },
            { error ->
                Log.d(TAG, "connexion error")
                error.printStackTrace()
            }
        )
        MotherLoad.instance.requestQueue?.add(stringRequest)
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