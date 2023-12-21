package com.example.motherload.data.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherland.MotherLoad
import com.example.motherload.data.callback.ConnexionCallback
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

object ConnexionApi {
    val TAG = "ConnexionApi"
    private val BASE_URL_CREUSER = "https://test.vautard.fr/creuse_srv/"
    //todo faire le traitement des autres
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
}