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
import com.example.motherload.data.callback.ProfilCallback
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @property TAG le tag utiliser pour les logs
 * @property BASE_URL_CREUSER l'url de base utilisée par les webservices
 */
object ProfileApi {
    private const val TAG = "ProfileApi"
    private const val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"

    /**
     * Effectue le changement de pseudonyme
     *
     * @param session la session de la connexion utilisateur
     * @param signature la signature de la connexion utilisateur
     * @param pseudo le nouveau pseudonyme de l'utilisateur
     * @param callback changerPseudo avec pseudo si le pseudo est valide et avec une chaîne vide sinon
     * @param activity l'activité courante
     */
    fun changerPseudo(session: Long, signature: Long, pseudo: String, callback: ProfilCallback, activity: Activity){
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

    /**
     * Effectue le reset du compte utilisateur
     *
     * @param session la session de la connexion utilisateur
     * @param signature la signature de la connexion utilisateur
     * @param callback resetUser
     * @param activity l'activité courante
     */
    fun resetUser(session: Long, signature: Long, callback: ProfilCallback, activity: Activity){
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
     * Récupère la liste des artéfacts
     *
     * @param session la session de la connexion utilisateur
     * @param signature la signature de la connexion utilisateur
     * @param callback getArtifact avec la liste des artefacts
     * @param activity l'activité courante
     */
    fun getArtifact(session: Long, signature: Long, callback: ProfilCallback, activity: Activity){
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
                            val items = mutableListOf<Item>()
                            val listItems = doc.getElementsByTagName("ARTEFACTS").item(0).childNodes
                            for (i in 0 until listItems.length) {
                                val node = listItems.item(i)
                                if (node.nodeType == Node.ELEMENT_NODE) {
                                    val elem = node as Element
                                    val id = elem.getElementsByTagName("ID").item(0).textContent.toInt()
                                    items.add(Item(id.toString(),"0"))
                                }
                            }
                            Log.d(TAG, "inventory:${items.size}")
                            callback.getArtifact(items)
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
    fun getInventory(session: Long, signature: Long, callback: ProfilCallback, activity: Activity){
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
}