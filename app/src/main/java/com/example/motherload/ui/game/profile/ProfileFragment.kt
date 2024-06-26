package com.example.motherload.ui.game.profile

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motherload.injection.ViewModelFactory
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

/**
 * @property viewModel le ViewModel utilisé par le fragment
 * @property ret la vue affichée par le fragment
 */
class ProfileFragment: Fragment(){

    private var viewModel: ProfileViewModel? = null
    private lateinit var ret: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[ProfileViewModel::class.java]
    }

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
                override fun getInventory(inventory: List<Item>) {}
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
                        override fun getInventory(inventory: List<Item>) {}
                        override fun resetUser() {
                            PopUpDisplay.simplePopUp(
                                requireActivity(),
                                getString(R.string.r_initialisation),
                                getString(R.string.votre_compte_a_t_r_initialis)
                            )
                            setArtifact()
                        }
                    }, requireActivity())
                }
            }

        }

        //Si l'utilisateur ne possède pas une version d'Api >= 30 on récupère son thème dans les sharedPreferences
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            theme.isChecked = resources.configuration.isNightModeActive
        }
        else{
            val sharedPref = MotherLoad.instance.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            theme.isChecked = sharedPref.getInt("theme", MODE_NIGHT_NO) == MODE_NIGHT_YES
        }

        theme.setOnCheckedChangeListener{ _, isChecked ->
            val themeSharedPref = MotherLoad.instance.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val editor = themeSharedPref.edit()
            //On change le thème de l'app et stock le nouveau thème dans les sharedPréférences
            if(isChecked){
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                editor.putInt("theme", MODE_NIGHT_YES)
            }
            else {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                editor.putInt("theme", MODE_NIGHT_NO)
            }
            editor.apply()
            //On redémarre l'activité pour prendre en compte le changement de thème
            requireActivity().finish()
            startActivity(requireActivity().intent)
        }

        //Gestion du sélecteur de langues
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


    /**
     * Défini les artéfact de la liste panini selon leur obtention
     */
    private fun setArtifact() {
        viewModel!!.getArtifact(object :
            ProfilCallback {
                override fun changerPseudo(pseudo: String) {}
                override fun resetUser() {}
                override fun getInventory(inventory: List<Item>) {}
                @RequiresApi(Build.VERSION_CODES.O)
                override fun getArtifact(artifact: List<Item>) {
                    viewModel!!.getInventory(object :
                        ProfilCallback {
                        override fun changerPseudo(pseudo: String) {}
                        override fun resetUser() {}
                        override fun getArtifact(artifact: List<Item>) {}
                        override fun getInventory(inventory: List<Item>) {
                            viewModel!!.getItems(artifact, object :
                                ItemCallback {
                                override fun getItemsDescription(itemDescription: MutableList<ItemDescription>) {
                                    for (e in inventory){
                                        Log.d("coucou", "${e.id} ${e.quantity}")
                                    }
                                    Log.d("coucou", "-----------------------------------")
                                    for (e in artifact){
                                        Log.d("coucou", "${e.id} ${e.quantity}")
                                    }
                                    artifact.forEach { item ->
                                        val correspondingArtefact = inventory.find { it.id == item.id }
                                        item.quantity = correspondingArtefact?.quantity ?: "0"
                                    }

                                    val updatedItems: List<ItemDescription> = itemDescription.map { itemDesc ->
                                        val correspondingInventoryItem = artifact.find { it.id == itemDesc.id }
                                        correspondingInventoryItem?.let { itemDesc.copy(quantity = it.quantity) } ?: itemDesc
                                    }

                                    val recyclerView: RecyclerView = ret.findViewById(R.id.artefactInventory)
                                    val layoutManager = GridLayoutManager(requireActivity(), calculateSpanCount())
                                    recyclerView.layoutManager = layoutManager
                                    val adapter = ProfileAdapter(updatedItems)
                                    recyclerView.adapter = adapter
                                }
                            }, requireActivity())
                        }
                    }, requireActivity())
                }
            }
        , requireActivity())
    }

    /**
     * Définit le nombre de décalages nécessaire selon la taille de l'écran
     */
    private fun calculateSpanCount(): Int {
        val screenWidth = resources.displayMetrics.widthPixels
        val itemWidth = resources.getDimensionPixelSize(R.dimen.column_width)
        val minSpanCount = 1

        return max(minSpanCount.toDouble(), (screenWidth / itemWidth).toDouble()).toInt()
    }
}