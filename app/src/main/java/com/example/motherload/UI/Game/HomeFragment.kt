package com.example.motherland.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.motherLoad.Injection.ViewModelFactory
import com.example.motherload.Data.HomeCallback
import com.example.motherload.R
import com.example.motherload.UI.Game.HomeViewModel
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
    private var lastExecutionTime: Long = 0
    private var center = false
    private var requestingLocationUpdate = false
    private var viewModel: HomeViewModel? = null
    private var timer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance!!)[HomeViewModel::class.java]
        timer = Timer()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    getLocation(location)
                    if (System.currentTimeMillis() - lastExecutionTime >= 15000) {
                        viewModel!!.deplacement(location, object :
                            HomeCallback {
                            override fun deplacement(voisin: MutableMap<String, GeoPoint>) {
                                affichageVoisin(voisin)
                            }
                        }
                        )
                        lastExecutionTime = System.currentTimeMillis();
                    }
                }
            }
        }

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100)
            .build()

        Configuration.getInstance().load(requireActivity().applicationContext,
            activity?.let { PreferenceManager.getDefaultSharedPreferences(it.applicationContext) })
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val ret = inflater.inflate(R.layout.fragment_home, container, false)
        var inventaire = ret.findViewById<ImageView>(R.id.boutonInventaire)
        var shop = ret.findViewById<ImageView>(R.id.boutonShop)
        var creuser = ret.findViewById<ImageView>(R.id.boutonCreuser)
        var profil = ret.findViewById<ImageView>(R.id.boutonProfil)

        profil.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            profil.startAnimation(animation)
        }
        creuser.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            creuser.startAnimation(animation)
        }

        shop.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            shop.startAnimation(animation)
        }

        inventaire.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireActivity().applicationContext, R.anim.animation_icon)
            inventaire.startAnimation(animation)
            activity?.supportFragmentManager?.beginTransaction()
            activity?.supportFragmentManager?.commit {
                replace(R.id.fragmentContainerView, InventoryFragment())
                setReorderingAllowed(true)
                addToBackStack("Inventory")
            }
        }

        map = ret.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        val mapController = map.controller
        mapController.setZoom(19)

        if (ActivityCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // reuqest for permission
        }
        else{
            fusedLocationProviderClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
                if (location != null) {
                    getLocation(location)
                    viewModel!!.deplacement(location, object :
                        HomeCallback {
                        override fun deplacement(voisin: MutableMap<String, GeoPoint>) {
                            affichageVoisin(voisin)
                        }
                    }
                    )
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
            requestingLocationUpdate = true
        }
        return ret
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    fun getLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        playerPosition = GeoPoint(latitude, longitude)
        if (!center) {
            map.controller.setCenter(playerPosition)
            center = true
        }
        val overlayItems = ArrayList<OverlayItem>()
        var joueur = OverlayItem("Moi", "", playerPosition)
        var icone_joueur = requireActivity().resources.getDrawable(R.drawable.player_icon)
        joueur.setMarker(icone_joueur)
        overlayItems.add(joueur)
        joueurOverlay = ItemizedOverlayWithFocus(context, overlayItems, object :
            ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
            override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                return false
            }
            override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                return false
            }
        })
        if(map.overlays.size == 0)
            map.overlays.add(joueurOverlay)
        map.overlays.set(0, joueurOverlay)
    }

    fun affichageVoisin(voisin: MutableMap<String, GeoPoint>){
        map.overlays.clear()
        map.overlays.add(joueurOverlay)
        val overlayItems = ArrayList<OverlayItem>()
        voisin.forEach { (cle, valeur) ->
            overlayItems.add(OverlayItem(cle, "", valeur))
        }
        val mOverlay = ItemizedOverlayWithFocus<OverlayItem>(context, overlayItems, object :
            ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
            override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                return true
            }

            override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                return false
            }
        })
        mOverlay.setFocusItemsOnTap(true)
        map.overlays.add(mOverlay)
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        requestingLocationUpdate = false
        center = false
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        if (!requestingLocationUpdate) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
        }
    }

}