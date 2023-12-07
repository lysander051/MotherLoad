package com.example.motherload.UI.Connexion

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherLoad.UI.Connexion.ConnexionViewModel
import com.example.motherLoad.Utils.AppPermission
import com.example.motherland.MotherLoad
import com.example.motherload.Data.ConnexionCallback
import com.example.motherload.UI.Game.MainActivity
import com.example.motherload.R
import com.example.motherload.Utils.SafeClickListener
import com.example.motherload.Utils.setSafeOnClickListener

class ConnexionActivity : AppCompatActivity(){

    private var viewModel: ConnexionViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connexion)

        AppPermission.requestLocation(this)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[ConnexionViewModel::class.java]
        var connexion = findViewById<Button>(R.id.boutonConnexion)
        connexion.setSafeOnClickListener {
            viewModel!!.getConnected(findViewById<EditText>(R.id.login).text.toString(), findViewById<EditText>(R.id.password).text.toString(), object :
            ConnexionCallback {
                override fun onConnexion(isConnected: Boolean) {
                    checkConnexion(isConnected)
                }
            })
        }
    }

    fun checkConnexion(connected:Boolean){
        if(connected){
            var intent = Intent(this, MainActivity::class.java)
            val sharedPreferences: SharedPreferences = MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
            intent = intent.putExtra("SessionId", sharedPreferences.getLong("SessionId",-1))
            intent = intent.putExtra("Signature", sharedPreferences.getLong("Signature",-1))
            startActivity(intent)
        }
        else{
            Toast.makeText(
                this,
                "Mauvais login/ mot de passe",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}