package com.fm.beebo.ui.osm

// Removed deprecated PreferenceManager import
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
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
import androidx.navigation.NavController
import com.fm.beebo.R
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
import androidx.core.graphics.scale
import androidx.core.graphics.drawable.toDrawable

data class Waypoint(
    val id: Int,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double
)

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
                    IconButton(onClick = {navController.popBackStack()}) {
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
    val selectedBranchOffice by settingsViewModel.selectedBranchOffice.collectAsState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var lastLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    val waypoints = remember {
        listOf(
            Waypoint(
                id = BranchOffice.Zentralbibliothek.id,
                title = BranchOffice.Zentralbibliothek.displayName,
                description = "Zentralbibo im Kulturpalast",
                latitude = 51.050666,
                longitude = 13.738295
            ),
            Waypoint(
                id = BranchOffice.Blasewitz.id,
                title = BranchOffice.Blasewitz.displayName,
                description = "",
                latitude = 51.051027,
                longitude = 13.807854
            ),
            Waypoint(
                id = BranchOffice.Buehlau.id,
                title = BranchOffice.Buehlau.displayName,
                description = "",
                latitude = 51.061745,
                longitude = 13.84873
            ),
            Waypoint(
                id = BranchOffice.Cossebaude.id,
                title = BranchOffice.Cossebaude.displayName,
                description = "",
                latitude = 51.08838376693984,
                longitude = 13.630048042411289
            ),
            Waypoint(
                id = BranchOffice.Cotta.id,
                title = BranchOffice.Cotta.displayName,
                description = "",
                latitude = 51.05752830676419,
                longitude = 13.6860007709652
            ),
            Waypoint(
                id = BranchOffice.Gorbitz.id,
                title = BranchOffice.Gorbitz.displayName,
                description = "",
                latitude = 51.04584224985092,
                longitude = 13.669299482607935
            ),
            Waypoint(
                id = BranchOffice.Gruna.id,
                title = BranchOffice.Gruna.displayName,
                description = "",
                latitude = 51.03418525200302,
                longitude = 13.78361795318534
            ),
            Waypoint(
                id = BranchOffice.Johannstadt.id,
                title = BranchOffice.Johannstadt.displayName,
                description = "",
                latitude = 51.049157283542556,
                longitude = 13.76885322522458
            ),
            Waypoint(
                id = BranchOffice.Klotzsche.id,
                title = BranchOffice.Klotzsche.displayName,
                description = "",
                latitude = 51.11939784488856,
                longitude = 13.769891149084087
            ),
            Waypoint(
                id = BranchOffice.Langebrueck.id,
                title = BranchOffice.Langebrueck.displayName,
                description = "",
                latitude = 51.12724800127986,
                longitude = 13.843881553437907
            ),
            Waypoint(
                id = BranchOffice.Laubegast.id,
                title = BranchOffice.Laubegast.displayName,
                description = "",
                latitude = 51.02500620146082,
                longitude = 13.839257737292995
            ),
            Waypoint(
                id = BranchOffice.LeubnitzNeuostra.id,
                title = BranchOffice.LeubnitzNeuostra.displayName,
                description = "",
                latitude = 51.02165602307906,
                longitude = 13.764028928309257
            ),
            Waypoint(
                id = BranchOffice.Neustadt.id,
                title = BranchOffice.Neustadt.displayName,
                description = "",
                latitude = 51.06747146177359,
                longitude = 13.747596329492392
            ),
            Waypoint(
                id = BranchOffice.Pieschen.id,
                title = BranchOffice.Pieschen.displayName,
                description = "",
                latitude = 51.07799264408835,
                longitude = 13.720705561235228
            ),
            Waypoint(
                id = BranchOffice.Plauen.id,
                title = BranchOffice.Plauen.displayName,
                description = "",
                latitude = 51.03469458786578,
                longitude = 13.70551168163574
            ),
            Waypoint(
                id = BranchOffice.Prohlis.id,
                title = BranchOffice.Prohlis.displayName,
                description = "",
                latitude = 51.006203894415236,
                longitude = 13.798450667932558
            ),
            Waypoint(
                id = BranchOffice.Strehlen.id,
                title = BranchOffice.Strehlen.displayName,
                description = "",
                latitude = 51.01971306378044,
                longitude = 13.780271698657312
            ),
            Waypoint(
                id = BranchOffice.Suedvorstadt.id,
                title = BranchOffice.Suedvorstadt.displayName,
                description = "",
                latitude = 51.03399912872732,
                longitude = 13.721460388888648
            ),
            Waypoint(
                id = BranchOffice.Weissig.id,
                title = BranchOffice.Weissig.displayName,
                description = "",
                latitude = 51.06231646773942,
                longitude = 13.884955722572409
            ),
            Waypoint(
                id = BranchOffice.Weixdorf.id,
                title = BranchOffice.Weixdorf.displayName,
                description = "",
                latitude = 51.14212409847163,
                longitude = 13.795246022787616
            ),
        )
    }

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


    // Function to add waypoint markers
    fun addWaypoints(mapView: MapView) {
        waypoints.forEach { waypoint ->
            val marker = object : Marker(mapView) {
                override fun onLongPress(event: MotionEvent?, mapView: MapView?): Boolean {
                    settingsViewModel.setBranchOffice(BranchOffice.getById(waypoint.id))
                    Toast.makeText(mapView?.context, "Zweigstelle ausgewählt: ${waypoint.title}", Toast.LENGTH_SHORT).show()

                    return true
                }
            }

            marker.apply {
                position = GeoPoint(waypoint.latitude, waypoint.longitude)
                title = waypoint.title
                snippet = waypoint.description
                setAnchor(ANCHOR_CENTER, ANCHOR_BOTTOM)

                setOnMarkerClickListener { m, mv ->
                    showInfoWindow()
                    mv.controller.animateTo(position)
                    true
                }

                marker.icon = getScaledDrawable(mapView.context, R.drawable.marker, 28, 36) // Size in pixels

            }

            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }






    // Function to update location overlay
    fun updateLocationOverlay(mapView: MapView) {
        if (locationPermissionGranted) {
            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
            locationOverlay.enableMyLocation()
            locationOverlay.enableFollowLocation()
            mapView.overlays.add(locationOverlay)
        }
    }

    // Center map on location when permission is granted and location is available
    LaunchedEffect(lastLocation, locationPermissionGranted) {
        if (locationPermissionGranted && lastLocation != null) {
            mapViewRef.value?.controller?.animateTo(lastLocation)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                val mapView = MapView(context)
                Configuration.getInstance()
                    .load(context, context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
                Configuration.getInstance().userAgentValue = context.packageName

                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.setMultiTouchControls(true)
                mapView.controller.setZoom(15.0)

                // Set initial center (Dresden city center as default)
                mapView.controller.setCenter(GeoPoint(51.0504, 13.7373))

                // Add waypoints immediately
                addWaypoints(mapView)

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
                            mapViewRef.value?.controller?.animateTo(geoPoint)
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