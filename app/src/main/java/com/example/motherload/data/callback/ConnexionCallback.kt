package com.example.motherload.data.callback

interface ConnexionCallback {
    /**
     * Transmet le résultat de la requête connexion
     *
     * @param isConnected le résultat de la connexion (true si succès)
     */
    fun onConnexion(isConnected: Boolean)
}