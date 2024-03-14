package com.adityaa0108.mapboxapp


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point


import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent

import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource






class MainActivity : AppCompatActivity(), OnMapReadyCallback,PermissionsListener,
    MapboxMap.OnMapClickListener {

    private lateinit var mapView: MapView
    private var mapboxMap: MapboxMap? = null
    private lateinit var spinnerMapType: Spinner
    private var permissionManager: PermissionsManager? = null
    private var locationComponent: LocationComponent? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        //Initialize the spinner
        spinnerMapType = findViewById(R.id.spinnerMapType)

        val adapter = ArrayAdapter.createFromResource(
            this, R.array.map_types, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMapType.adapter = adapter

        //Set up map
        mapView.getMapAsync(this@MainActivity)
        // Handle spinner item selection
        spinnerMapType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>, selectedItemView: View,
                position: Int, id: Long
            ) {
                updateMapStyle(position)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing here
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(map: MapboxMap) {
        this.mapboxMap = map
        // Customize map settings and add functionalities here

        // Set the initial map style based on the selected item in the spinner
        updateMapStyle(spinnerMapType.selectedItemPosition)

    }


    @SuppressLint("MissingPermission")
    private fun updateMapStyle(position: Int) {
        when (position) {

            0 -> setStyle(Style.MAPBOX_STREETS)
            1 -> setStyle(Style.SATELLITE)
            2 -> setStyle(Style.OUTDOORS)
            3 -> setStyle(Style.SATELLITE_STREETS)
            4 -> setStyle(Style.TRAFFIC_DAY)
            5 -> setStyle(Style.TRAFFIC_NIGHT)

            // Add more cases for other map types if needed
        }

    }

    @SuppressLint("MissingPermission")
    private fun setStyle(styleUrl: String) {
        mapboxMap?.setStyle(styleUrl) {
            // Callback triggered when the map style is loaded
            enableLocationComponent(mapboxMap!!.style.toString())
            addDestinationIcon(mapboxMap!!.style!!)
            mapboxMap!!.addOnMapClickListener(this)
        }
    }

    private fun addDestinationIcon(loadedMapStyle: Style?) {
           loadedMapStyle!!.addImage("destination-icon-id", BitmapFactory.decodeResource(this.resources,
               com.mapbox.mapboxsdk.R.drawable.mapbox_marker_icon_default))
           val geoJsonSource = GeoJsonSource("destination-source-id")
           loadedMapStyle.addSource(geoJsonSource)
           val destinationSymbolLayer = SymbolLayer("destination-symbol-layer-id","destination-source-id")
           destinationSymbolLayer.withProperties(PropertyFactory.iconImage("destination-icon-id"),
               PropertyFactory.iconAllowOverlap(true),PropertyFactory.iconIgnorePlacement(true))

        loadedMapStyle.addLayer(destinationSymbolLayer)
    }

    override fun onMapClick(point: LatLng): Boolean {
          val destinationPoint = Point.fromLngLat(point.longitude,point.latitude)
        val originPoint = Point.fromLngLat(locationComponent!!.lastKnownLocation!!.longitude,locationComponent!!.lastKnownLocation!!.latitude)
        val source= mapboxMap!!.style!!.getSourceAs<GeoJsonSource>("destination-source-id")
        source?.setGeoJson(Feature.fromGeometry(destinationPoint))

        return true
    }


    private fun enableLocationComponent(styleUrl: String) {
        // Activate the LocationComponent to show user location
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = mapboxMap!!.locationComponent
            locationComponent!!.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, mapboxMap!!.style!!).build()
            )

            // Enable to make the LocationComponent visible
            locationComponent!!.isLocationComponentEnabled = true

            // Set the LocationComponent camera mode to follow the user's location
            locationComponent!!.cameraMode = CameraMode.TRACKING
        } else {
            permissionManager = PermissionsManager(this)
            permissionManager!!.requestLocationPermissions(this)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this,R.string.user_location_permission_explanation,Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if(granted){
            enableLocationComponent(mapboxMap!!.style.toString())
        }
        else{
            Toast.makeText(this,R.string.user_location_permission_explanation,Toast.LENGTH_SHORT).show()
            finish()
        }
    }


        override fun onStart() {
            super.onStart()
            mapView.onStart()
        }

        override fun onResume() {
            super.onResume()
            mapView.onResume()
        }

        override fun onPause() {
            super.onPause()
            mapView.onPause()
        }

        override fun onStop() {
            super.onStop()
            mapView.onStop()
        }

        override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
            super.onSaveInstanceState(outState, outPersistentState)
            mapView.onSaveInstanceState(outState)
        }

        override fun onDestroy() {
            super.onDestroy()
            mapView.onDestroy()
        }

        override fun onLowMemory() {
            super.onLowMemory()
            mapView.onLowMemory()
        }




}




