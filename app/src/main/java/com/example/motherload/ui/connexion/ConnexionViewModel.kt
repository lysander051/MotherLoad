package com.example.motherload.ui.connexion

import androidx.lifecycle.ViewModel
import com.example.motherLoad.Utils.LoginManager
import com.example.motherload.data.callback.ConnexionCallback
import com.example.motherload.data.Repository
import java.net.URLEncoder

class ConnexionViewModel(private var connexionRepo: Repository) : ViewModel() {

    /**
     * Envoie les informations de connexion au repo afin de commencer la connexion
     *
     * @param login le login de l'utilisateur
     * @param password le mot de passe de l'utilisateur
     * @param callback le callback de la requête
     */
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