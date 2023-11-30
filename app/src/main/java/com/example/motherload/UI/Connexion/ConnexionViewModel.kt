package com.example.motherLoad.UI.Connexion

import androidx.lifecycle.ViewModel
import com.example.motherLoad.Data.ConnexionRepo
import com.example.motherLoad.Utils.SHA256Hasher
import com.example.motherload.Data.ConnexionCallback
import java.net.URLEncoder

class ConnexionViewModel(var connexionRepo: ConnexionRepo) : ViewModel() {

    fun getConnected(login: String, password: String, callback: ConnexionCallback) {
        val hasher = SHA256Hasher()
        val passwordHash : String = hasher.hash(password)
        val encodeLogin = URLEncoder.encode(login,"UTF-8")
        val encodePassword = URLEncoder.encode(passwordHash,"UTF-8")

        connexionRepo.getConnected(encodeLogin, encodePassword, object : ConnexionCallback {
            override fun onConnexion(isConnected: Boolean) {
                callback.onConnexion(isConnected)
            }
        })
    }
}