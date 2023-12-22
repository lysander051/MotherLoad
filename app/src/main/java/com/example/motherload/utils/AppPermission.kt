package com.example.motherLoad.Utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.example.motherload.R


class AppPermission {
    companion object {
        fun requestLocation(context: Context){
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val explanationMessage =
                    context.getString(R.string.nous_avons_besoin_de_votre_permission_pour_acc_der_votre_localisation_pr_cise_afin_que_vous_puissiez_jouer)
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.permission_requise))
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
                val explanationMessage =
                    context.getString(R.string.merci_d_activer_la_g_olocalisation_pr_cise_dans_vos_param_tres_pour_profiter_de_notre_jeu)
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.permission_gps))
                    .setMessage(explanationMessage)
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }
    }
}