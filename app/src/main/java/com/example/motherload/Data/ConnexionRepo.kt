package com.example.motherLoad.Data


import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.example.motherland.MotherLoad
import com.example.motherload.Data.ConnexionCallback
import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


class ConnexionRepo {
    val TAG: String = "ConnexionRepo"


    fun getConnected(login: String, password: String, callback: ConnexionCallback): Boolean {
        val BASE_URL = "https://test.vautard.fr/creuse_srv/connexion.php"
        val url = BASE_URL+"?login=$login&passwd=$password"
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
        return connected
    }
}
