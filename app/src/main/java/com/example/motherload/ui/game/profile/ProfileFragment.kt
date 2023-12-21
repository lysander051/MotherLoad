package com.example.motherload.ui.game.profile

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.os.LocaleListCompat
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
            val themeSharedPref = MotherLoad.instance.getSharedPreferences("Settings", Context.MODE_PRIVATE)
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
            requireActivity().finish()
            startActivity(requireActivity().intent)
        }

        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.langues,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            langue.adapter = adapter
            val sharedPref = MotherLoad.instance.getSharedPreferences("Settings",Context.MODE_PRIVATE)
            langue.setSelection(sharedPref.getInt("posLangue",0))
        }

        class LanguageSelector() : Activity(), AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                var lang = "fr-FR"
                var pos = 0
                when(position){
                    1 -> {lang = "en-US"
                        pos = 1}
                    2 -> {lang = "ko-KR"
                        pos = 2}
                    else -> {}
                }
                val appLocales : LocaleListCompat = LocaleListCompat.forLanguageTags(lang)
                val sharedPref = MotherLoad.instance.getSharedPreferences("Settings", Context.MODE_PRIVATE)
                val sha = sharedPref.getInt("pos",0)
                Log.d("LANGUE","shared = $sha et pos = $pos")
                if (sharedPref.getInt("pos",0) != pos){
                    val editor = sharedPref.edit()
                    editor.putString("langue",lang)
                    editor.putInt("posLangue",pos)
                    editor.apply()
                    AppCompatDelegate.setApplicationLocales(appLocales)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }


        }

        langue.onItemSelectedListener = LanguageSelector()


        return ret
    }

}