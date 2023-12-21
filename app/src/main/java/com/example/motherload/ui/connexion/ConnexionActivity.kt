package com.example.motherload.ui.connexion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherLoad.UI.Connexion.ConnexionViewModel
import com.example.motherLoad.Utils.AppPermission
import com.example.motherload.data.callback.ConnexionCallback
import com.example.motherload.ui.game.MainActivity
import com.example.motherload.R
import com.example.motherload.utils.PopUpDisplay
import com.example.motherload.utils.setSafeOnClickListener

class ConnexionActivity : AppCompatActivity(){

    private var viewModel: ConnexionViewModel? = null
    private lateinit var login: EditText
    private lateinit var psw: EditText
    private var saveLP = false
    private var stayC = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connexion)

        AppPermission.requestLocation(this)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[ConnexionViewModel::class.java]
        val connexion = findViewById<Button>(R.id.boutonConnexion)
        val saveLoginPsw = findViewById<CheckBox>(R.id.saveLoginPsw)
        val stayconnected = findViewById<CheckBox>(R.id.stayConnected)

        //récupération des données enregistré
        login = findViewById(R.id.login)
        psw = findViewById(R.id.password)
        val (loadedLogin, loadedPsw, loadedSaveLP) = loadLoginFromSharedPreferences()
        val loadedStayC = loadStayConnected()
        login.setText(loadedLogin)
        psw.setText(loadedPsw)
        saveLoginPsw.isChecked = loadedSaveLP
        saveLP = loadedSaveLP
        stayconnected.isChecked = loadedStayC
        stayC = loadedStayC

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
            //todo rester connecté en permanence
        }

        saveLoginPsw.setOnCheckedChangeListener { _, isChecked ->
            saveLP = isChecked
        }
    }

    private fun checkConnexion(connected: Boolean) {
        if (connected) {
            val intent = Intent(this, MainActivity::class.java)
            Log.d("connexion", saveLP.toString())
            if(saveLP) {
                //todo save the password crypted with keystore
                saveLoginToSharedPreferences(login.text.toString(), psw.text.toString(), saveLP)
            }
            else {
                saveLoginToSharedPreferences("", "", false)
            }
            saveStayConnected(stayC)
            startActivity(intent)
        } else {
            PopUpDisplay.longToast(this, "mot de passe ou pseudo incorrect")
        }
    }

    private fun saveLoginToSharedPreferences(loginValue: String, pswValue: String, saveLP: Boolean) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("login", loginValue)
            putString("psw", pswValue)
            putBoolean("saveLP", saveLP)
            apply()
        }
    }

    private fun saveStayConnected(stayC: Boolean){
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("stayC", stayC)
            apply()
        }
    }

    private fun loadLoginFromSharedPreferences(): Triple<String, String, Boolean> {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val login = sharedPref.getString("login", "") ?: ""
        val psw = sharedPref.getString("psw", "") ?: ""
        val saveLP = sharedPref.getBoolean("saveLP", false)
        return Triple(login, psw, saveLP)
    }

    private fun loadStayConnected(): Boolean {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
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