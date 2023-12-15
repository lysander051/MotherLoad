package com.example.motherload.Data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherland.MotherLoad
import org.osmdroid.util.GeoPoint
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class Repository {

    private val TAG: String = "Repo"
    private var session: Long = -1
    private var signature: Long = -1
    private val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"

    fun getConnected(login: String, password: String, callback: ConnexionCallback){
        val url = BASE_URL_CREUSER+"connexion.php?login=$login&passwd=$password"
        var connected = false

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
                            Log.d(TAG, "Connexion avec succès")
                            val session: Long =
                                doc.getElementsByTagName("SESSION").item(0).textContent.toLong()
                            val signature: Long = doc.getElementsByTagName("SIGNATURE")
                                .item(0).textContent.toLong()
                            val sharedPreferences: SharedPreferences = MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            Log.d(TAG, "session: $session|signature: $signature")
                            editor.putLong("SessionId", session)
                            editor.putLong("Signature", signature)
                            editor.apply()
                            callback.onConnexion(true)
                        } else if (status == "KO - WRONG CREDENTIALS") {
                            Log.d(TAG, "Mauvais Login/passwd")
                            Log.d(TAG, "login - $login & password - $password")
                            callback.onConnexion(false)
                        } else {
                            Log.d(TAG, "Erreur - $status")
                            callback.onConnexion(false)
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

    fun deplacement(latitude:Double, longitude:Double, callback: HomeCallback){
        getSessionSignature()
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


    fun creuser(latitude: Double, longitude: Double, callback: HomeCallback) {
        getSessionSignature()
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
                            try {
                                itemId = doc.getElementsByTagName("ITEM_ID").item(0).textContent
                            }
                            catch (e: NullPointerException){
                                itemId = "-1"
                            }
                            Log.d(TAG,"itemId: "+itemId)
                            callback.creuse(itemId.toInt())
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

    fun getStatus(callback: InventoryCallback){
        getSessionSignature()
        val url = BASE_URL_CREUSER+"status_joueur.php?session=$session&signature=$signature"
        var connected = false
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
                                    items.add(Item(id,quantity))
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

    private fun getSessionSignature(){
        val sharedPreferences = MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
        session = sharedPreferences.getLong("SessionId", -1)
        signature = sharedPreferences.getLong("Signature", -1)
    }
}