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
import com.example.motherload.data.callback.InventoryCallback
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @property TAG le tag utiliser pour les logs
 * @property BASE_URL_CREUSER l'url de base utilisée par les webservices
 */
object InventoryApi {
    private const val TAG = "InventoryApi"
    private const val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"

    /**
     * Récupère la position, l'argent, la pioche et les items de l'utilisateur
     *
     * @param session la session de la connexion utilisateur
     * @param signature la signature de la connexion utilisateur
     * @param callback getStatus avec la pioche, l'argent et les objets du joueur
     * @param activity l'activité courante
     */
    fun getStatus(session: Long, signature: Long, callback: InventoryCallback, activity: Activity){
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
                            val pickaxe = doc.getElementsByTagName("PICKAXE").item(0).textContent.toInt()
                            val money = doc.getElementsByTagName("MONEY").item(0).textContent.toInt()
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
                            Log.d(TAG, "pickaxe:$pickaxe / money:$money / inventory:${items.size}")
                            callback.getStatus(pickaxe,money,items)
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
     * Effectue l'amélioration de la pioche
     *
     * @param session la session de la connexion utilisateur
     * @param signature la signature de la connexion utilisateur
     * @param pickaxeLevel le niveau de la pioche souhaitée
     * @param callback upgradePickaxe en cas de succès
     * et erreur avec le numéro de l'erreur en cas d'échec
     * @param activity l'activité courante
     */
    fun upgradePickaxe(session: Long, signature: Long, pickaxeLevel: Int, callback: InventoryCallback, activity: Activity){
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
     * Récupère les différentes recettes de fabrication des pioches
     *
     * @param session la session de connexion utilisateur
     * @param signature la signature de connexion utilisateur
     * @param callback recipePickaxe avec la liste des recettes
     * @param activity l'activité courante
     */
    fun recipePickaxe(session: Long, signature: Long, callback: InventoryCallback, activity: Activity){
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
                            val recipeList = mutableMapOf<String,List<Item>>()
                            var listItem : MutableList<Item>
                            val listPickaxe = doc.getElementsByTagName("UPGRADES").item(0).childNodes
                            Log.d(TAG, "taille liste pickaxe: ${listPickaxe.length}")
                            for (i in 0 until listPickaxe.length) {
                                val pickaxeNode = listPickaxe.item(i) as Element
                                val pickaxeId = pickaxeNode.getElementsByTagName("PICKAXE_ID").item(0).textContent.trim()
                                val itemsNode = pickaxeNode.getElementsByTagName("ITEMS").item(0)
                                val itemList = itemsNode.childNodes
                                listItem = mutableListOf()
                                for (j in 0 until itemList.length) {
                                    if (itemList.item(j) is Element) {
                                        val itemNode = itemList.item(j) as Element
                                        val itemId = itemNode.getElementsByTagName("ITEM_ID").item(0).textContent.trim()
                                        val quantity = itemNode.getElementsByTagName("QUANTITY").item(0).textContent.trim()
                                        listItem.add(Item(itemId,quantity))
                                    }
                                }
                                Log.d(TAG, "$pickaxeId, size item ${listItem.size}")
                                recipeList[pickaxeId] = listItem
                            }
                            callback.recipePickaxe(recipeList)
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