package com.example.motherload.data.api

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherLoad.Utils.LoginManager
import com.example.motherland.MotherLoad
import com.example.motherload.data.Item
import com.example.motherload.data.callback.ConnexionCallback
import com.example.motherload.data.callback.ShopCallback
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @property TAG le tag utiliser pour les logs
 * @property BASE_URL_CREUSER l'url de base utilisée par les webservices
 */
object ShopApi {
    private const val TAG = "ShopApi"
    private const val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"

    /**
     * Récupère les offres du magasin
     *
     * @param session la session de la connexion utilisateur
     * @param signature la signature de la connexion utilisateur
     * @param callback getMarketItems avec la liste des items mis en vente
     * @param activity l'activité courante
     */
    fun getMarketItems(session: Long, signature: Long, callback: ShopCallback, activity: Activity) {
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
                            val items = mutableListOf<Triple<Int, Item, Int>>()
                            val listItems = doc.getElementsByTagName("OFFERS").item(0).childNodes
                            for (i in 0 until listItems.length) {
                                val node = listItems.item(i)
                                if (node.nodeType == Node.ELEMENT_NODE) {
                                    val elem = node as Element
                                    val offerId = elem.getElementsByTagName("OFFER_ID").item(0).textContent.toInt()
                                    val itemId = elem.getElementsByTagName("ITEM_ID").item(0).textContent.toInt()
                                    val quantity = elem.getElementsByTagName("QUANTITE").item(0).textContent.toInt()
                                    val price = elem.getElementsByTagName("PRIX").item(0).textContent.toInt()
                                    items.add(Triple(offerId, Item(itemId.toString(),quantity.toString()), price))
                                }
                            }
                            callback.getMarketItems(items)
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


    /**
     * Récupère l'inventaire du joueur
     *
     * @param session la session de la connexion utilisateur
     * @param signature la signature de la connexion utilisateur
     * @param callback getInventory avec la liste des objets de l'inventaire
     * @param activity l'activité courante
     */
    fun getInventory(session: Long, signature: Long, callback: ShopCallback, activity: Activity){
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
                            Log.d(TAG, "Acces inventaire")
                            val items = mutableListOf<Item>()

                            val listItems = doc.getElementsByTagName("ITEMS").item(0).childNodes
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

    /**
     * Effectue l'achat d'une offre du magasin
     *
     * @param session la session de la connexion utilisateur
     * @param signature la signature de la connexion utilisateur
     * @param callback buyItem si l'achat c'est effectué avec succés
     * et erreur sinon
     * @param activity l'activité courante
     */
    fun buyItem(session: Long, signature: Long, orderId: Int, callback: ShopCallback, activity: Activity){
        val url = BASE_URL_CREUSER +"market_acheter.php?session=$session&signature=$signature&offer_id=$orderId"
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
                            Log.d(TAG, "Acces buy item")
                            callback.buyItem()
                        }
                        else if (status == "KO - NO MONEY"){
                            callback.erreur()
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

    /**
     * Effectue la mise en vente d'un objet de l'utilisateur
     *
     * @param session la session de la connexion utilisateur
     * @param signature la signature de la connexion utilisateur
     * @param quantity la quantité mise en vente
     * @param id l'id de l'objet mis en vente
     * @param prix le prix de l'offre
     * @param callback sellItem
     * @param activity l'activité courante
     */
    fun sellItem(session: Long, signature: Long, quantity: Int, id: String?, prix: Int, callback: ShopCallback, activity: Activity) {
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
                            Log.d(TAG, "Acces sell item")
                            callback.sellItem()
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
}