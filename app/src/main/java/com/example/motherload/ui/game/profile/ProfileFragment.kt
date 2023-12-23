package com.example.motherload.ui.game.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherland.MotherLoad
import com.example.motherload.R
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.data.callback.ItemCallback
import com.example.motherload.data.callback.ProfilCallback
import com.example.motherload.ui.connexion.ConnexionActivity
import com.example.motherload.utils.LanguageSelector
import com.example.motherload.utils.PopUpDisplay
import com.example.motherload.utils.setSafeOnClickListener
import java.lang.Double.max

class ProfileFragment: Fragment(){

    private var viewModel: ProfileViewModel? = null
    private lateinit var ret: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[ProfileViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ret = inflater.inflate(R.layout.fragment_profil, container, false)
        val retour = ret.findViewById<ImageView>(R.id.boutonRetour)
        val confirmer = ret.findViewById<Button>(R.id.boutonConfirmer)
        val reset = ret.findViewById<Button>(R.id.boutonReset)
        val theme = ret.findViewById<Switch>(R.id.theme)
        val langue = ret.findViewById<Spinner>(R.id.selecteurLangue)
        val deconnexion = ret.findViewById<ImageView>(R.id.deconnexion)

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
                            getString(R.string.changement_de_pseudo),
                            getString(R.string.vous_avez_chang_de_pseudo_pour, pseudo))
                    }
                    else{
                        PopUpDisplay.simplePopUp(requireActivity(),
                            getString(R.string.probl_me_de_changement_de_pseudo),
                            getString(R.string.probl_me_de_changement_de_pseudo_il_faut_qu_il_soit_plus_long_que_3))
                    }
                }
                override fun resetUser() {}
                override fun getArtifact(inventory: List<Item>) {}
            }, requireActivity())
        }
        reset.setSafeOnClickListener {
            PopUpDisplay.cancellablePopUp(requireActivity(),
                getString(R.string.r_initialisation),
                getString(R.string.tes_vous_s_r_de_vouloir_r_initialiser_votre_compte)
                ) { confirmed ->
                if (confirmed) {viewModel!!.resetUser(object :
                    ProfilCallback {
                        override fun changerPseudo(pseudo: String) {}
                        override fun getArtifact(inventory: List<Item>) {}
                        override fun resetUser() {
                            PopUpDisplay.simplePopUp(
                                requireActivity(),
                                getString(R.string.r_initialisation),
                                getString(R.string.votre_compte_a_t_r_initialis)
                            )
                        }
                    }, requireActivity())
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
            val defaultLanguage = LanguageSelector.getPosSelecteurLangue()
            langue.setSelection(sharedPref.getInt("posLangue",defaultLanguage))
        }

        langue.onItemSelectedListener = LanguageSelector(requireActivity())
        setArtifact()

        deconnexion.setSafeOnClickListener {
            PopUpDisplay.cancellablePopUp(requireActivity(),
                getString(R.string.d_connexion),
                getString(R.string.voulez_vous_vraiment_vous_d_connecter)
            ) { confirmed ->
                if (confirmed) {
                    val sharedPref = MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putBoolean("stayC", false)
                    editor.putBoolean("saveLP", false)
                    editor.putString("psw", "")
                    editor.putString("iv", "")
                    editor.apply()
                    requireActivity().finish()
                    requireActivity().startActivity(Intent(requireActivity(), ConnexionActivity::class.java))
                }
            }
        }

        return ret
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setArtifact() {
        viewModel!!.getArtifact(object :
            ProfilCallback {
            override fun changerPseudo(pseudo: String) {}
            override fun resetUser() {}
            @RequiresApi(Build.VERSION_CODES.O)
            override fun getArtifact(inventory: List<Item>) {
                viewModel!!.getItems(inventory, object :
                    ItemCallback {
                    override fun getItemsDescription(itemDescription: MutableList<ItemDescription>) {
                        val updatedItems : MutableList<ItemDescription> = inventory.map { item ->
                            var correspondingItemDescription = itemDescription.find { it.id == item.id}
                            correspondingItemDescription?.quantity = item.quantity
                            correspondingItemDescription
                        } as MutableList<ItemDescription>
                        for(i in updatedItems.indices){
                            if(updatedItems.get(i)!!.quantity.toInt() <= 0 ){
                                updatedItems.removeAt(i)
                            }
                        }
                        val recyclerView: RecyclerView = ret.findViewById(R.id.artefactInventory)
                        val layoutManager = GridLayoutManager(requireActivity(), calculateSpanCount())
                        recyclerView.layoutManager = layoutManager
                        val adapter = ProfileAdapter(updatedItems)
                        recyclerView.adapter = adapter
                    }
                }
                , requireActivity())
            }
            }
        , requireActivity())
    }

    private fun calculateSpanCount(): Int {
        val screenWidth = resources.displayMetrics.widthPixels
        val itemWidth = resources.getDimensionPixelSize(R.dimen.column_width)
        val minSpanCount = 1

        return max(minSpanCount.toDouble(), (screenWidth / itemWidth).toDouble()).toInt()
    }
}