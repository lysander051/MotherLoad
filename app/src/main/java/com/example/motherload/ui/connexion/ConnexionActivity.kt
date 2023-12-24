package com.example.motherload.ui.connexion

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.motherload.injection.ViewModelFactory
import com.example.motherLoad.Utils.AppPermission
import com.example.motherLoad.Utils.LoginManager
import com.example.motherland.MotherLoad
import com.example.motherload.data.callback.ConnexionCallback
import com.example.motherload.ui.game.MainActivity
import com.example.motherload.R
import com.example.motherload.utils.PopUpDisplay
import com.example.motherload.utils.setSafeOnClickListener

/**
 * @property viewModel le ViewModel utilisé par l'activité
 * @property login le champs texte du login de l'utilisateur
 * @property psw le champs texte du mot de passe de l'utilisateur
 * @property saveLP true si l'utilisateur souhaite sauvegarder ses informations
 * @property stayC true si l'utilisateur souhaite rester connecté
 * @property SHAREDPREF le sharedPreference utilisé par l'activité pour stocker diverses valeurs
 */
class ConnexionActivity : AppCompatActivity(){

    private var viewModel: ConnexionViewModel? = null
    private lateinit var login: EditText
    private lateinit var psw: EditText
    private var saveLP = false
    private var stayC = false
    private val SHAREDPREF : String = "Connexion"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connexion)

        //On demande la permission d'utiliser la localisation de l'utilisateur
        AppPermission.requestLocation(this)
        //On récupère le ViewModel dont on se servira plus tard
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[ConnexionViewModel::class.java]
        //On récupère les éléments de la vue avec lesquels on va pouvoir intéragir
        val connexion = findViewById<Button>(R.id.boutonConnexion)
        val saveLoginPsw = findViewById<CheckBox>(R.id.saveLoginPsw)
        val stayconnected = findViewById<CheckBox>(R.id.stayConnected)

        //récupération des données enregistré
        login = findViewById(R.id.login)
        psw = findViewById(R.id.password)
        //On charge les login et psw si on les connait déjà
        val (loadedLogin, loadedPsw, loadedSaveLP) = loadLoginFromSharedPreferences()
        val loadedStayC = loadStayConnected()
        login.setText(loadedLogin)
        psw.setText(loadedPsw)
        //On coche les cases selon les précédent choix de l'utilisateur (s'ils existent)
        saveLoginPsw.isChecked = loadedSaveLP
        saveLP = loadedSaveLP
        stayconnected.isChecked = loadedStayC
        stayC = loadedStayC

        //Le bouton connexion effectue la requête de connexion
        connexion.setSafeOnClickListener {
            viewModel!!.getConnected(findViewById<EditText>(R.id.login).text.toString(), findViewById<EditText>(R.id.password).text.toString(), object :
                ConnexionCallback {
                override fun onConnexion(isConnected: Boolean) {
                    checkConnexion(isConnected)
                }
            })
        }
        stayconnected.setOnCheckedChangeListener { _, isChecked ->
            stayC = isChecked
        }

        saveLoginPsw.setOnCheckedChangeListener { _, isChecked ->
            saveLP = isChecked
        }
        //Si rester connecté était déjà coché alors on se connecte automatiquement
        if (stayC){
            viewModel!!.getConnected(findViewById<EditText>(R.id.login).text.toString(), findViewById<EditText>(R.id.password).text.toString(), object :
                ConnexionCallback {
                override fun onConnexion(isConnected: Boolean) {
                    checkConnexion(isConnected)
                }
            })
        }
    }

    /**
     * Vérifie le résultat de la requête de la connexion de l'utilisateur
     *
     * @param connected le résultat de la requête (true si l'utilisateur à réussi à se connecter)
     */
    private fun checkConnexion(connected: Boolean) {
        if (connected) {
            val intent = Intent(this, MainActivity::class.java)
            //Si l'utilisateur veut sauvegarder ses informations ou resté tout le temps connecté
            if(saveLP || stayC) {
                //On chiffre son mot de passe pour le stocker dans les sharedPreferences
                val chiffrePassword = LoginManager.savePassword(psw.text.toString())
                saveLoginToSharedPreferences(login.text.toString(), chiffrePassword, saveLP)
            }
            else {
                //Sinon on ne sauvegarde rien / on enlève ce que l'on avait jusqu'alors
                saveLoginToSharedPreferences("", "", false)
            }
            saveStayConnected(stayC)
            //On termine l'activité et on lance l'activité principale
            finish()
            startActivity(intent)
        } else {
            //On informe l'utilisateur du problème
            PopUpDisplay.longToast(this, getString(R.string.mot_de_passe_ou_pseudo_incorrect))
        }
    }

    /**
     * Sauvegarde des informations de l'utilisateur
     *
     * @param loginValue le login de l'utilisateur
     * @param pswValue le mot de passe crypté de l'utilisateur
     * @param saveLP le choix de sauvegarder les informations
     */
    private fun saveLoginToSharedPreferences(loginValue: String, pswValue: String, saveLP: Boolean) {
        val sharedPref = MotherLoad.instance.getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("login", loginValue)
            putString("psw", pswValue)
            putBoolean("saveLP", saveLP)
            apply()
        }
    }

    /**
     * Sauvegarde le choix de l'utilisateur de rester connecté
     *
     * @param stayC le choix de l'utilisateur de rester connecté
     */
    private fun saveStayConnected(stayC: Boolean){
        val sharedPref = MotherLoad.instance.getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("stayC", stayC)
            apply()
        }
    }

    /**
     * Récupère le login, mot de passe et le choix de sauvegarder les informations de l'utilisateur
     *
     * @return le login, le mot de passe et le choix de l'utilisateur de sauvegarder ses informations
     */
    private fun loadLoginFromSharedPreferences(): Triple<String, String, Boolean> {
        val sharedPref = MotherLoad.instance.getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE)
        val login = sharedPref.getString("login", "") ?: ""
        val psw = LoginManager.getDecryptedPassword()
        val saveLP = sharedPref.getBoolean("saveLP", false)
        return Triple(login, psw, saveLP)
    }

    /**
     * Récupère le choix de l'utilisateur de rester connecté
     */
    private fun loadStayConnected(): Boolean {
        val sharedPref = MotherLoad.instance.getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE)
        return sharedPref.getBoolean("stayC", false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("login", login.text.toString())
        outState.putString("psw", psw.text.toString())
        outState.putBoolean("saveLP", saveLP)
        outState.putBoolean("stayC", stayC)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        login.setText(savedInstanceState.getString("login", ""))
        psw.setText(savedInstanceState.getString("psw", ""))
        saveLP = savedInstanceState.getBoolean("saveLP", false)
        stayC = savedInstanceState.getBoolean("stayC", false)
    }
}