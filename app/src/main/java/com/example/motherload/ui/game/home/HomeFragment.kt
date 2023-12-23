package com.example.motherload.ui.game.home

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherLoad.Utils.AppPermission
import com.example.motherload.R
import com.example.motherload.data.Item
import com.example.motherload.data.ItemDescription
import com.example.motherload.data.callback.HomeCallback
import com.example.motherload.data.callback.ItemCallback
import com.example.motherload.ui.game.inventory.InventoryFragment
import com.example.motherload.ui.game.profile.ProfileFragment
import com.example.motherload.ui.game.shop.ShopFragment
import com.example.motherload.utils.PopUpDisplay
import com.example.motherload.utils.setSafeOnClickListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.GroundOverlay2
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.OverlayItem
import java.util.Timer


class HomeFragment : Fragment() {

    private lateinit var map: MapView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var playerPosition: GeoPoint
    private lateinit var holePosition: GeoPoint
    private lateinit var joueurOverlay: ItemizedOverlayWithFocus<OverlayItem>
    private lateinit var creuser: ImageView
    private lateinit var creuserBW: ImageView
    private lateinit var depthField: TextView
    private var creuserAnimationStartTime: Long = 0L
    private var center = true
    private var viewModel: HomeViewModel? = null
    private var timer: Timer? = null
    private var didDig = true
    private var depthHole = false
    private var firstdisp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[HomeViewModel::class.java]
        timer = Timer()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        //initialisation du comportement de chaque mise à jour de la position
        locationCallback = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    getLocation(location)
                    viewModel!!.deplacement(location, object :
                        HomeCallback {
                        override fun deplacement(voisin: MutableMap<String, GeoPoint>) {
                            affichageVoisin(voisin)
                        }
                        override fun creuse(itemId: Int, depht: String, voisin: MutableMap<String, GeoPoint>) {}
                        override fun erreur(erreurId: Int) {}
                    }
                    )
                }
            }
        }

        //mise à jour de la position gps toutes les 5 secondes
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .build()
        Configuration.getInstance().load(requireActivity().applicationContext,
            activity?.let { PreferenceManager.getDefaultSharedPreferences(it.applicationContext) })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val ret = inflater.inflate(R.layout.fragment_home, container, false)
        val inventaire = ret.findViewById<ImageView>(R.id.boutonInventaire)
        val shop = ret.findViewById<ImageView>(R.id.boutonShop)
        val profil = ret.findViewById<ImageView>(R.id.boutonProfil)
        val bcenter = ret.findViewById<ImageView>(R.id.boutonCenter)
        creuserBW = ret.findViewById(R.id.boutonCreuserBW)
        depthField = ret.findViewById(R.id.depth)
        creuser = ret.findViewById(R.id.boutonCreuser)

        //comportement du bouton de centrage de la map sur le joueur
        bcenter.setSafeOnClickListener{
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            bcenter.startAnimation(animation)
            if(center){
                center = false
                bcenter.setImageResource(R.drawable.center_black_icon)
            }
            else{
                center = true
                map.controller.setCenter(playerPosition)
                bcenter.setImageResource(R.drawable.center_blue_icon)
            }
        }

        //bouton pour accéder au profil
        profil.setSafeOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.commit()
            activity?.supportFragmentManager?.commit {
                replace(R.id.fragmentContainerView, ProfileFragment())
                setReorderingAllowed(true)
                addToBackStack("Profile")
            }
        }

        //bouton pour creuser avec un delai de 10sec
        //todo ajouter la profondeur à un sharedpreference
        val handler = Handler()
        creuser.setSafeOnClickListener {
            if (viewModel!!.isButtonClickEnabled.value == true) {
                viewModel!!.disableButtonClick()
                creuserBW.visibility = View.VISIBLE
                val animationDuration: Long = 10000
                val fadeAnimator = ObjectAnimator.ofFloat(creuserBW, "alpha", 1f, 0f)
                fadeAnimator.duration = animationDuration
                creuserAnimationStartTime = System.currentTimeMillis()
                fadeAnimator.start()
                val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
                creuserBW.startAnimation(animation)
                viewModel!!.creuser(playerPosition, object : HomeCallback {
                    override fun deplacement(voisin: MutableMap<String, GeoPoint>) {}
                    override fun creuse(itemId: Int, depth: String, voisin: MutableMap<String, GeoPoint>) {
                        if (itemId != -1) {
                            viewModel!!.getItems(mutableListOf(Item(itemId.toString(),"1")), object :
                                ItemCallback {
                                override fun getItemsDescription(itemDescription: MutableList<ItemDescription>) {
                                    PopUpDisplay.shortToast(requireActivity(), "${itemDescription.get(0).nom} trouvé")
                                }
                            })
                        }
                        depthHole = true
                        didDig = true
                        affichageVoisin(voisin)
                        val text = depth + "M"
                        depthField.text = text
                    }
                    override fun erreur(erreurId: Int) {
                        gestionErreur(erreurId)
                    }
                })

                // Bloque le bouton 10secondes si on a cliqué
                handler.postDelayed({
                    creuser.setImageResource(R.drawable.pickaxe_icon)
                    viewModel!!.enableButtonClick()
                }, 10000)
            }
        }


        shop.setSafeOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.commit()
            activity?.supportFragmentManager?.commit {
                replace(R.id.fragmentContainerView, ShopFragment())
                setReorderingAllowed(true)
                addToBackStack("Shop")
            }
        }

        //pour aller sur le fragment de l'inventaire
        inventaire.setSafeOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.commit()
            activity?.supportFragmentManager?.commit {
                replace(R.id.fragmentContainerView, InventoryFragment())
                setReorderingAllowed(true)
                addToBackStack("Inventory")
            }
        }

        //initialisation du comportement de la map
        mapInitialisation(ret, bcenter)
        //démarrage de la géolocalisation du joueur
        if (ActivityCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            AppPermission.requestLocation(requireActivity())
        }
        else{
            //cette partie permet une initialisation rapide de la position du joueur
            fusedLocationProviderClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
                if (location != null) {
                    getLocation(location)
                    viewModel!!.deplacement(location, object :
                        HomeCallback {
                        override fun deplacement(voisin: MutableMap<String, GeoPoint>) {
                            affichageVoisin(voisin)
                        }
                        override fun creuse(itemId: Int, depht: String, voisin: MutableMap<String, GeoPoint>) {}
                        override fun erreur(erreurId: Int) {}
                    }
                    )
                }
            }
            //on lance la géolocalisation et la mise a jour des voisins
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
        }
        return ret
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun mapInitialisation(ret: View, bcenter: ImageView){
        map = ret.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(false)
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(19.0)
        //désactive le centrage sur le joueur lorsque l'on touche ou bouge la map
        map.setOnTouchListener { v, _ ->
            if (center) {
                center = false
                bcenter.setImageResource(R.drawable.center_black_icon)
            }
            v.performClick()
        }
    }

    private fun gestionErreur(erreurId: Int){
        //TODO gestion de toutes les erreurs
        if (erreurId == 0)
            PopUpDisplay.simplePopUp(requireActivity(),
                getString(R.string.trop_rapide),
                getString(R.string.vous_cliquez_comme_un_fou_il_faut_ralentir))
        if (erreurId == 1)
            PopUpDisplay.simplePopUp(requireActivity(),
                getString(R.string.trop_profond),
                getString(R.string.trop_profond_pour_votre_pioche_il_faut_vous_d_placer_ou_changer_de_pioche))
        if (erreurId == 2)
            PopUpDisplay.simplePopUp(requireActivity(),
                getString(R.string.trop_loin),
                getString(R.string.ce_n_est_pas_pokemongo_il_faut_rester_l_universit_pour_travailler_ehhhh_jouer))
    }

    private fun getLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        playerPosition = GeoPoint(latitude, longitude)
        affichageTrou()
        //centre la caméra sur le joueur s'il faut
        if (center)
            map.controller.setCenter(playerPosition)
        //création de l'icone de joueur
        val overlayItems = ArrayList<OverlayItem>()
        val joueur = OverlayItem("", "", playerPosition)
        val iconeJoueur = requireActivity().resources.getDrawable(R.drawable.player_icon)
        joueur.setMarker(iconeJoueur)
        overlayItems.add(joueur)
        //désactive le clique sur l'icone du joueur
        joueurOverlay = ItemizedOverlayWithFocus(context, overlayItems, object :
            ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
            override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                return false
            }
            override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                return false
            }
        })
        //permet de set la position du joueur et d'éviter la duplication d'icones
        if (map.overlays.size > 1)
            map.overlays[1] = joueurOverlay
        else
            map.overlays.add(0, joueurOverlay)
    }

    private fun affichageTrou() {
        val myGroundOverlay = GroundOverlay2()
        if(didDig && firstdisp) {
            holePosition = playerPosition
            didDig = false
        }
        val centerPoint = GeoPoint(holePosition.latitude, holePosition.longitude)
        val overlayWidth = 0.0003
        val overlayHeight = 0.00015
        val topLeft = GeoPoint(
            centerPoint.latitude + overlayHeight / 2,
            centerPoint.longitude - overlayWidth / 2
        )
        val bottomRight = GeoPoint(
            centerPoint.latitude - overlayHeight / 2,
            centerPoint.longitude + overlayWidth / 2
        )

        myGroundOverlay.setPosition(topLeft, bottomRight)
        val d = BitmapFactory.decodeResource(requireContext().resources, R.drawable.hole)
        myGroundOverlay.setImage(d)

        if (depthHole)
            myGroundOverlay.transparency = 0f
        else
            myGroundOverlay.transparency = 1f

        // Ajouter le trou à l'indice 0 dans la liste des overlays
        if (map.overlays.size > 0)
            map.overlays[0] = myGroundOverlay
        else
            map.overlays.add(0, myGroundOverlay)
}

    private fun affichageVoisin(voisin: MutableMap<String, GeoPoint>) {
        if (map.overlays.size > 2) {
            map.overlays.subList(2, map.overlays.size).clear()
        }
        affichageTrou()
        map.overlays.add(1, joueurOverlay)

        val overlayItems = ArrayList<OverlayItem>()
        voisin.forEach { (cle, valeur) ->
            if (valeur.latitude != playerPosition.latitude ||
                valeur.longitude != playerPosition.longitude){
                overlayItems.add(OverlayItem(cle, "", valeur))
            }
        }

        val mOverlay = ItemizedOverlayWithFocus(context, overlayItems, object :
            ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
            override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                return true
            }

            override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                return false
            }
        })

        mOverlay.setFocusItemsOnTap(true)
        map.overlays.add(2, mOverlay)
    }

    private fun loadingDepthHole(){
        val dephtHoleInfo = viewModel?.getDepthHole()
        holePosition = GeoPoint(dephtHoleInfo?.first?.toDouble()!!, dephtHoleInfo?.second?.toDouble()!!)
        depthField.text = dephtHoleInfo.third.toString()+"M"
        depthHole = true
        affichageTrou()
        firstdisp = true
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        center = false
        firstdisp = false
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            AppPermission.requestLocation(requireActivity())
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
        if (System.currentTimeMillis() - creuserAnimationStartTime < 10000) {
            creuserBW.visibility = View.VISIBLE
            val fadeAnimator = ObjectAnimator.ofFloat(creuserBW, "alpha", 1f, 0f)
            fadeAnimator.duration = 10000
            fadeAnimator.currentPlayTime = System.currentTimeMillis() - creuserAnimationStartTime
            fadeAnimator.start()
        }
        center = true
        loadingDepthHole()
    }
}