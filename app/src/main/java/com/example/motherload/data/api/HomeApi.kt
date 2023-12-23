package com.example.motherload.data.api

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherLoad.Utils.LoginManager
import com.example.motherland.MotherLoad
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.data.Repository
import com.example.motherload.data.callback.HomeCallback
import com.example.motherload.data.callback.InventoryCallback
import com.example.motherload.ui.game.MainActivity
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
    @RequiresApi(Build.VERSION_CODES.O)
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
                        else if (status == "KO - SESSION INVALID" || status == "KO - SESSION EXPIRED"){
                            connectAgain()
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
    fun getDepthHole(): Triple<Float, Float, Int> {
        val sharedPreferences = MotherLoad.instance.getSharedPreferences("HoleDisplay", Context.MODE_PRIVATE)
        val latitude = sharedPreferences.getFloat("Latitude", 0f)
        val longitude = sharedPreferences.getFloat("Longitude", 0f)
        val depth = sharedPreferences.getInt("Depth", 0)
        Log.d(TAG, "latitude: $latitude, longitude: $longitude, depth: $depth, ")
        return Triple(latitude, longitude, depth)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun connectAgain(){
        Log.d("coucou", "try to connect")
        val sharedPref = MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
        val login = sharedPref.getString("login", "") ?: ""
        val psw = LoginManager.hash(LoginManager.getDecryptedPassword())
        val keepConnected = sharedPref.getBoolean("stayC", false)
        if (keepConnected) {
            for (i in 0..5) {
                val url = BASE_URL_CREUSER + "connexion.php?login=$login&passwd=$psw"

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
                                    Log.d("coucou", "Connexion avec succès")
                                    val session: Long =
                                        doc.getElementsByTagName("SESSION")
                                            .item(0).textContent.toLong()
                                    val signature: Long = doc.getElementsByTagName("SIGNATURE")
                                        .item(0).textContent.toLong()
                                    val sharedPreferences: SharedPreferences =
                                        MotherLoad.instance.getSharedPreferences(
                                            "Connexion",
                                            Context.MODE_PRIVATE
                                        )
                                    val editor = sharedPreferences.edit()
                                    Log.d(TAG, "session: $session|signature: $signature")
                                    editor.putLong("SessionId", session)
                                    editor.putLong("Signature", signature)
                                    editor.apply()
                                    return@StringRequest
                                } else if (status == "KO - WRONG CREDENTIALS") {
                                    Log.d(TAG, "Mauvais Login/passwd")
                                    Log.d(TAG, "login - $login & password - $psw")
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
        Log.d("coucou", "pas connecté")
        val intent = Intent(MotherLoad.instance, ConnexionApi::class.java)
        MotherLoad.instance.startActivity(intent)
    }

}