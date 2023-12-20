package com.example.motherload.ui.game.profile

import android.app.UiModeManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherland.MotherLoad
import com.example.motherload.data.callback.ProfilCallback
import com.example.motherload.R
import com.example.motherload.utils.PopUpDisplay
import com.example.motherload.utils.setSafeOnClickListener

class ProfileFragment: Fragment(){

    private var viewModel: ProfileViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[ProfileViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ret = inflater.inflate(R.layout.fragment_profil, container, false)
        val retour = ret.findViewById<ImageView>(R.id.boutonRetour)
        val confirmer = ret.findViewById<Button>(R.id.boutonConfirmer)
        val reset = ret.findViewById<Button>(R.id.boutonReset)
        val theme = ret.findViewById<Switch>(R.id.theme)
        val langue = ret.findViewById<Spinner>(R.id.selecteurLangue)

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
                        PopUpDisplay.simplePopUp(requireActivity(),
                            "Changement de pseudo",
                            "Vous avez changé de pseudo pour $pseudo")
                    }
                    else{
                        PopUpDisplay.simplePopUp(requireActivity(),
                            "problème de changement de pseudo",
                            "Problème de changement de pseudo. Il faut qu'il soit plus long que 3")
                    }
                }
                override fun resetUser() {}
            })
        }
        reset.setSafeOnClickListener {
            PopUpDisplay.cancellablePopUp(requireActivity(),
                "Réinitialisation",
                "êtes vous sûr de vouloir réinitialiser votre compte?"
                ) { confirmed ->
                if (confirmed) {viewModel!!.resetUser(object :
                    ProfilCallback {
                        override fun changerPseudo(pseudo: String) {}
                        override fun resetUser() {
                            PopUpDisplay.simplePopUp(
                                requireActivity(),
                                "Réinitialisation",
                                "Votre compte a été réinitialisé"
                            )
                        }
                    })
                }
            }

        }

        theme.isChecked = resources.configuration.isNightModeActive

        theme.setOnCheckedChangeListener{ buttonView, isChecked ->
            val themeSharedPref = MotherLoad.instance.getSharedPreferences("Theme", Context.MODE_PRIVATE)
            val editor = themeSharedPref.edit()
            if(isChecked){
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                editor.putInt("theme", MODE_NIGHT_YES)
            }
            else {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                editor.putInt("theme", MODE_NIGHT_NO)
            }
            editor.apply()
        }


        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.langues,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            langue.adapter = adapter
        }

        return ret
    }

}