package com.example.motherload.UI.Game

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherload.Data.ConnexionCallback
import com.example.motherload.Data.ProfilCallback
import com.example.motherload.R
import com.example.motherload.Utils.setSafeOnClickListener

class ProfileFragment: Fragment(){

    private var viewModel: ProfileViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[ProfileViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val ret = inflater.inflate(R.layout.fragment_profil, container, false)
        val retour = ret.findViewById<ImageView>(R.id.boutonRetour)
        val confirmer = ret.findViewById<Button>(R.id.boutonConfirmer)

        retour.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            retour.startAnimation(animation)
            activity?.supportFragmentManager?.popBackStack()
        }
        confirmer.setSafeOnClickListener {
            viewModel!!.changerPseudo(ret.findViewById<EditText>(R.id.nouveauPseudo).text.toString(), object :
                ProfilCallback {
                override fun changerPseudo(pseudo: String) {
                    ret.findViewById<EditText>(R.id.nouveauPseudo).setText(pseudo)
                    if (pseudo != ""){
                        val explanationMessage = "Vous avez changé de pseudo pour $pseudo"
                        AlertDialog.Builder(context)
                            .setTitle("Changement de pseudo")
                            .setMessage(explanationMessage)
                            .setPositiveButton("OK") { _, _ ->
                            }.show()
                    }
                    else{
                        val explanationMessage = "Problème de changement de pseudo. Il faut qu'il soit plus long que 3"
                        AlertDialog.Builder(context)
                            .setTitle("problème de changement de pseudo")
                            .setMessage(explanationMessage)
                            .setPositiveButton("OK") { _, _ ->
                            }.show()
                    }
                }
            })
        }


        return ret
    }
}