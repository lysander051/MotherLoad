package com.example.motherload.data

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

    fun getItems(items: List<Item>, callback: InventoryCallback){
        getSessionSignature()
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

    fun upgradePickaxe(pickaxeLevel: Int, callback: InventoryCallback){
        getSessionSignature()
        val url = BASE_URL_CREUSER+"maj_pioche.php?session=$session&signature=$signature&pickaxe_id=$pickaxeLevel"
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

    fun recipePickaxe(callback: InventoryCallback){
        getSessionSignature()
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

    fun changerPseudo(pseudo: String, callback: ProfilCallback){
        getSessionSignature()
        val BASE_URL = "https://test.vautard.fr/creuse_srv/changenom.php"
        val url = BASE_URL+"?session=$session&signature=$signature&nom=$pseudo"

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

    fun resetUser(callback: ProfilCallback){
        getSessionSignature()
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

    private fun getSessionSignature(){
        val sharedPreferences = MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
        session = sharedPreferences.getLong("SessionId", -1)
        signature = sharedPreferences.getLong("Signature", -1)
    }
}