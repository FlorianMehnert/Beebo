package com.fm.beebo.ui.osm

// Removed deprecated PreferenceManager import
import android.Manifest
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.navigation.NavController
import com.fm.beebo.R
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.ui.components.AppBottomNavigation
import com.fm.beebo.ui.search.BranchOffice
import com.fm.beebo.viewmodels.SettingsViewModel
import com.fm.beebo.viewmodels.UserViewModel
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.IconOverlay.ANCHOR_BOTTOM
import org.osmdroid.views.overlay.IconOverlay.ANCHOR_CENTER
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.core.graphics.toColorInt
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

data class Waypoint(
    val id: Int,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

class WaypointMarker(
    mapView: MapView,
    private val waypoint: Waypoint,
    private val settingsViewModel: SettingsViewModel
) : Marker(mapView) {
    override fun onLongPress(event: MotionEvent?, mapView: MapView?): Boolean {
        val handledByMarker = super.onLongPress(event, mapView)
        if (handledByMarker) {
            settingsViewModel.setBranchOffice(BranchOffice.getById(waypoint.id))
            Toast.makeText(
                mapView?.context,
                "Zweigstelle ausgewählt: ${waypoint.title}",
                Toast.LENGTH_SHORT
            ).show()
        }
        return handledByMarker
    }

}

fun loadWaypointsFromJson(context: Context): List<Waypoint> {
    val inputStream = context.assets.open("dresden_waypoints.json")
    val reader = InputStreamReader(inputStream)
    val type = object : TypeToken<List<Waypoint>>() {}.type
    return Gson().fromJson(reader, type)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    settingsViewModel: SettingsViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Übersichtskarte") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (true) {
                AppBottomNavigation(
                    navController = navController,
                    userViewModel = userViewModel,
                    currentRoute = "map"
                )
            }
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OsmMapView(settingsViewModel = settingsViewModel) // Pass the view model
        }
    }
}

@Composable
fun RequestLocationPermission(onGranted: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            onGranted()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}

@Composable
fun OsmMapView(settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var lastLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val settingsDataStore = SettingsDataStore(LocalContext.current)
    val centerOnMarker =
        settingsDataStore.enableAnimateToMarkerFlow.collectAsState(initial = false).value
    val selectedBranchOffice by settingsViewModel.selectedBranchOffice.collectAsState()

    val waypoints = loadWaypointsFromJson(context)

    RequestLocationPermission {
        locationPermissionGranted = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                lastLocation = GeoPoint(it.latitude, it.longitude)
            }
        }
    }

    fun getScaledDrawable(context: Context, resId: Int, width: Int, height: Int): Drawable {
        val original = ContextCompat.getDrawable(context, resId) ?: return Color.RED.toDrawable()
        val bitmap = (original as BitmapDrawable).bitmap
        val scaledBitmap = bitmap.scale(width, height)
        return scaledBitmap.toDrawable(context.resources)
    }

    fun increaseSaturation(color: Int, factor: Float = 1.0f): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        // hsv[1] is saturation: clamp it to [0, 1]
        hsv[1] = (hsv[1] * factor).coerceIn(0f, 1f)

        return Color.HSVToColor(Color.alpha(color), hsv)
    }


    fun addWaypoints(
        mapView: MapView,
        centerOnMarker: () -> Boolean,
        selectedBranchOffice: BranchOffice?
    ) {
        for (waypoint in waypoints) {
            val marker = WaypointMarker(mapView, waypoint, settingsViewModel)

            marker.apply {
                position = GeoPoint(waypoint.latitude, waypoint.longitude)
                title = waypoint.title
                snippet = waypoint.description
                setAnchor(ANCHOR_CENTER, ANCHOR_BOTTOM)

                setOnMarkerClickListener { m, mv ->
                    showInfoWindow()
                    if (centerOnMarker()) {
                        mv.controller.animateTo(
                            position,
                            mapView.zoomLevelDouble.toFloat().toDouble(), 100
                        )
                    }
                    true
                }

                // Change icon based on whether this is the selected branch office
                icon = getScaledDrawable(mapView.context, R.drawable.marker, 28, 36).apply {
                    if (selectedBranchOffice != null && waypoint.id == selectedBranchOffice.id) {
                        setTint("#c1121f".toColorInt())
                    } else {
                        setTint("#003049".toColorInt())
                    }
                }
            }

            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }




    fun updateLocationOverlay(mapView: MapView) {
        if (locationPermissionGranted) {
            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
            locationOverlay.enableMyLocation()
            locationOverlay.enableFollowLocation()
            mapView.overlays.add(locationOverlay)
        }
    }

    LaunchedEffect(centerOnMarker, selectedBranchOffice) {
        mapViewRef.value?.let { mapView ->
            mapView.overlays.removeAll { it is WaypointMarker }
            addWaypoints(mapView, { centerOnMarker }, selectedBranchOffice)
            mapView.invalidate()
        }
    }

    // Center map on location when permission is granted and location is available
    LaunchedEffect(lastLocation, locationPermissionGranted) {
        if (locationPermissionGranted && lastLocation != null) {
            mapViewRef.value?.controller?.setCenter(lastLocation)
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                val mapView = MapView(context)
                Configuration.getInstance()
                    .load(
                        context,
                        context.getSharedPreferences(
                            "osmdroid",
                            Context.MODE_PRIVATE
                        )
                    )
                Configuration.getInstance().userAgentValue = context.packageName

                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.setMultiTouchControls(true)
                mapView.controller.setZoom(15.0)

                // Set initial center (Dresden city center as default)
                mapView.controller.setCenter(GeoPoint(51.0504, 13.7373))

                // Add waypoints immediately
                addWaypoints(mapView, { centerOnMarker }, selectedBranchOffice)

                // Add location overlay if permission is already granted
                if (locationPermissionGranted) {
                    updateLocationOverlay(mapView)
                }

                mapViewRef.value = mapView
                mapView
            },
            update = { mapView ->
                // Update location overlay when permission status changes
                if (locationPermissionGranted) {
                    // Remove existing location overlay first
                    mapView.overlays.removeAll { it is MyLocationNewOverlay }
                    updateLocationOverlay(mapView)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        FloatingActionButton(
            onClick = {
                if (locationPermissionGranted) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val geoPoint = GeoPoint(it.latitude, it.longitude)
                            mapViewRef.value?.controller?.setCenter(geoPoint)
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My Location")
        }
    }
}