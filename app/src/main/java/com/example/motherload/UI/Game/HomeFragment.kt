package com.example.motherland.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.motherload.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import android.location.LocationListener
import androidx.core.app.ActivityCompat
import com.example.motherload.UI.Connexion.ConnexionActivity
import com.example.motherload.UI.Game.MainActivity
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.OverlayItem


class HomeFragment : Fragment() {

    private lateinit var map: MapView
    private lateinit var playerPosition: GeoPoint
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = requireActivity().applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(context, ConnexionActivity::class.java))
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0, 1.0f, locationListener
        )
        Configuration.getInstance().load(requireActivity().applicationContext,
            activity?.let { PreferenceManager.getDefaultSharedPreferences(it.applicationContext) })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val ret = inflater.inflate(R.layout.fragment_home, container, false)
        map = ret.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(true)
        val mapController = map.controller
        mapController.setZoom(19.5)
        return ret
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private val locationListener: LocationListener =
        LocationListener { location -> // Obtenez la latitude et la longitude de la localisation mise à jour
            val latitude = location.latitude
            val longitude = location.longitude
            map.overlays.clear()
            playerPosition = GeoPoint(latitude,longitude)
            map.controller.setCenter(playerPosition)
            val overlayItems = ArrayList<OverlayItem>()
            overlayItems.add(OverlayItem("Ma Position", "", playerPosition))
            val mOverlay = ItemizedOverlayWithFocus<OverlayItem>(context, overlayItems, object :
                ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean { return false }
                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean { return false } })
            map.overlays.add(mOverlay)
        }


    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

}