package com.example.motherload.Data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherland.MotherLoad
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class ProfileRepo {
    private val TAG: String = "ProfileRepo"
    private var session: Long = -1
    private var signature: Long = -1
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
    private fun getSessionSignature(){
        val sharedPreferences = MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
        session = sharedPreferences.getLong("SessionId", -1)
        signature = sharedPreferences.getLong("Signature", -1)
    }
}