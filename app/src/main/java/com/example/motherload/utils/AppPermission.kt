package com.example.motherLoad.Utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat


class AppPermission {
    companion object {
        fun requestLocation(context: Context){
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val explanationMessage = "Nous avons besoin de votre permission pour accéder à votre localisation précise afin que vous puissiez jouer."
                AlertDialog.Builder(context)
                    .setTitle("Permission Requise")
                    .setMessage(explanationMessage)
                    .setPositiveButton("OK") { _, _ ->
                        ActivityCompat.requestPermissions(
                            context as Activity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            1
                        )
                    }
                    .show()
            }
            else if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                val explanationMessage = "Merci d'activer la géolocalisation précise dans vos paramètres pour profiter de notre jeu"
                AlertDialog.Builder(context)
                    .setTitle("Permission gps")
                    .setMessage(explanationMessage)
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }
    }
}