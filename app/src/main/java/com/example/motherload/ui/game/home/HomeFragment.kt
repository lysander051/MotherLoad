package com.example.motherload.ui.game.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherLoad.Utils.AppPermission
import com.example.motherload.data.HomeCallback
import com.example.motherload.R
import com.example.motherload.ui.game.inventory.InventoryFragment
import com.example.motherload.ui.game.profile.ProfileFragment
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
    private lateinit var joueurOverlay: ItemizedOverlayWithFocus<OverlayItem>
    private var center = true
    private var viewModel: HomeViewModel? = null
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[HomeViewModel::class.java]
        timer = Timer()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        //initialisation du comportement de chaque mise à jour de la position
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    getLocation(location)
                    viewModel!!.deplacement(location, object :
                        HomeCallback {
                        override fun deplacement(voisin: MutableMap<String, GeoPoint>) {
                            affichageVoisin(voisin)
                        }
                        override fun creuse(itemId: Int) {}
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val ret = inflater.inflate(R.layout.fragment_home, container, false)
        val inventaire = ret.findViewById<ImageView>(R.id.boutonInventaire)
        val shop = ret.findViewById<ImageView>(R.id.boutonShop)
        val creuser = ret.findViewById<ImageView>(R.id.boutonCreuser)
        val profil = ret.findViewById<ImageView>(R.id.boutonProfil)
        val bcenter = ret.findViewById<ImageView>(R.id.boutonCenter)

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
        val handler = Handler()
        creuser.setSafeOnClickListener {
            if (viewModel!!.isButtonClickEnabled.value == true) {
                viewModel!!.disableButtonClick()
                creuser.setImageResource(R.drawable.pickaxe_icon_bw)
                val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
                creuser.startAnimation(animation)

                // Bloque le bouton 10secondes si on a cliqué
                handler.postDelayed({
                    viewModel!!.creuser(playerPosition, object : HomeCallback {
                        override fun deplacement(voisin: MutableMap<String, GeoPoint>) {}
                        override fun creuse(itemId: Int) {
                            if (itemId != -1) {
                                PopUpDisplay.shortToast(requireActivity(), "$itemId trouvé")
                            }
                        }
                        override fun erreur(erreurId: Int) {
                            gestionErreur(erreurId)
                        }
                    })
                    creuser.setImageResource(R.drawable.pickaxe_icon)
                    viewModel!!.enableButtonClick()
                }, 10000)
            }
        }

        shop.setSafeOnClickListener {
            //todo le shop
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
                        override fun creuse(itemId: Int) {}
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
    fun mapInitialisation(ret: View, bcenter: ImageView){
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
            false
        }
    }

    fun gestionErreur(erreurId: Int){
        //TODO gestion de toutes les erreurs
        if (erreurId == 0)
            PopUpDisplay.simplePopUp(requireActivity(),
                "Trop Rapide",
                "Vous cliquez comme un fou, il faut ralentir.")
        if (erreurId == 1)
            PopUpDisplay.simplePopUp(requireActivity(),
                "Trop profond",
                "Trop profond pour votre pioche. Il faut vous déplacer ou changer de pioche.")
        if (erreurId == 2)
            PopUpDisplay.simplePopUp(requireActivity(),
                "Trop loin",
                "Ce n'est pas pokemonGO, il faut rester à l'université pour travailler... Ehhhh jouer.")
    }

    fun getLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        playerPosition = GeoPoint(latitude, longitude)
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
        if(map.overlays.size == 0)
            map.overlays.add(joueurOverlay)
        map.overlays[0] = joueurOverlay
    }

    fun affichageVoisin(voisin: MutableMap<String, GeoPoint>){
        //clear la liste des joueurs
        map.overlays.clear()
        //ajoute le joueur en position 0 dans la liste
        map.overlays.add(joueurOverlay)
        val overlayItems = ArrayList<OverlayItem>()
        //cherche dans les voisins notre position pour éviter la duplication d'icone
        //TODO voir pourquoi ça bug
        voisin.forEach { (cle, valeur) ->
            if(String.format("%.3f", valeur.latitude).toDouble() != String.format("%.3f", playerPosition.latitude).toDouble() || String.format("%.3f", valeur.longitude).toDouble() != String.format("%.3f", playerPosition.longitude).toDouble())
                overlayItems.add(OverlayItem(cle, "", valeur))
        }
        //active le clique sur les autres joueurs pour voir leurs pseudo
        val mOverlay = ItemizedOverlayWithFocus(context, overlayItems, object :
            ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
            override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                return true
            }

            override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                return false
            }
        })
        //ajout des joueurs à la map
        mOverlay.setFocusItemsOnTap(true)
        map.overlays.add(mOverlay)
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        center = false
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

        center = true
    }
}