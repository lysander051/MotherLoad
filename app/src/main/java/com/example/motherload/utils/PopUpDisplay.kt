package com.example.motherload.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.example.motherload.R

class PopUpDisplay {
    companion object{
        fun simplePopUp(context:Context, title: String, text: String){
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                }.show()
        }

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

        fun shortToast(context: Context, text: String){
            Toast.makeText(
                context,
                text,
                Toast.LENGTH_SHORT
            ).show()
        }
        fun longToast(context: Context, text: String){
            Toast.makeText(
                context,
                text,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}