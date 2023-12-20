package com.example.motherload.data.api

import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherland.MotherLoad
import com.example.motherload.data.callback.ProfilCallback
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

object ProfileApi {
    val TAG = "InventoryApi"
    private val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"
    fun changerPseudo(session: Long, signature: Long, pseudo: String, callback: ProfilCallback){
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

    fun resetUser(session: Long, signature: Long, callback: ProfilCallback){
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
}