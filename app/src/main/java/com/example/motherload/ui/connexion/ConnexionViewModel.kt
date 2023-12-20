package com.example.motherLoad.UI.Connexion

import androidx.lifecycle.ViewModel
import com.example.motherLoad.Utils.LoginManager
import com.example.motherload.data.callback.ConnexionCallback
import com.example.motherload.data.Repository
import java.net.URLEncoder

class ConnexionViewModel(var connexionRepo: Repository) : ViewModel() {

    fun getConnected(login: String, password: String, callback: ConnexionCallback) {
        val passwordHash : String = LoginManager.hash(password)
        val encodeLogin = URLEncoder.encode(login,"UTF-8")
        val encodePassword = URLEncoder.encode(passwordHash,"UTF-8")

        connexionRepo.getConnected(encodeLogin, encodePassword, object : ConnexionCallback {
            override fun onConnexion(isConnected: Boolean) {
                callback.onConnexion(isConnected)
            }
        })
    }
}