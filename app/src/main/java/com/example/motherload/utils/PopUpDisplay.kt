package com.example.motherload.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.example.motherload.R

class PopUpDisplay {
    companion object{
        /**
         * Affiche une pop-up à l'écran
         *
         * @param context le context courant
         * @param title titre de la pop-up
         * @param text contenu de la pop-up
         */
        fun simplePopUp(context:Context, title: String, text: String){
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                }.show()
        }

        /**
         * Créer une pop-up avec des boutons "ok" et "annuler"
         *
         * @param context le context courant
         * @param title le titre de la pop-up
         * @param text le contenu de la pop-up
         * @param onConfirmed fonction de réaction au clic sur "ok" ou "annuler"
         */
        fun cancellablePopUp(context: Context, title: String, text: String, onConfirmed: (Boolean) -> Unit){
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle(title)
            alertDialogBuilder.setMessage(text)

            alertDialogBuilder.setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                onConfirmed(true)
            }

            alertDialogBuilder.setNegativeButton(context.getString(R.string.annuler)) { _, _ ->
                onConfirmed(false)
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        /**
         * Affiche un Toast court à l'écran
         *
         * @param context le context courant
         * @param text le contenu du Toast
         */
        fun shortToast(context: Context, text: String){
            Toast.makeText(
                context,
                text,
                Toast.LENGTH_SHORT
            ).show()
        }

        /**
         * Affiche un Toast long à l'écran
         *
         * @param context le context courant
         * @param text le contenu du Toast
         */
        fun longToast(context: Context, text: String){
            Toast.makeText(
                context,
                text,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}