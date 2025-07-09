package com.fm.beebo.ui.osm

// Removed deprecated PreferenceManager import
import android.Manifest
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.fm.beebo.ui.components.AppBottomNavigation
import com.fm.beebo.viewmodels.UserViewModel
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

data class Waypoint(
    val id: String,
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
            OsmMapView()
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
fun OsmMapView() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var lastLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // Sample waypoints - replace with your actual waypoints
    val waypoints = remember {
        listOf(
            Waypoint(
                id = "1",
                title = "Zentralbibliothek",
                description = "Zentralbibo im Kulturpalast",
                latitude = 51.050666,
                longitude = 13.738295
            ),
            Waypoint(
                id = "2",
                title = "Bibliothek Blasewitz",
                description = "",
                latitude = 51.051027,
                longitude = 13.807854
            ),
            Waypoint(
                id = "3",
                title = "Bibliothek Bühlau",
                description = "",
                latitude = 51.061745,
                longitude = 13.84873
            ),
            Waypoint(
                id = "4",
                title = "Bibliothek Cossebaude",
                description = "",
                latitude = 51.08838376693984,
                longitude = 13.630048042411289
            ),
            Waypoint(
                id = "5",
                title = "Bibliothek Cotta",
                description = "",
                latitude = 51.05752830676419,
                longitude = 13.6860007709652
            ),
            Waypoint(
                id = "6",
                title = "Bibliothek Gorbitz",
                description = "",
                latitude = 51.04584224985092,
                longitude = 13.669299482607935
            ),
            Waypoint(
                id = "7",
                title = "Bibliothek Gruna",
                description = "",
                latitude = 51.03418525200302,
                longitude = 13.78361795318534
            ),
            Waypoint(
                id = "8",
                title = "Bibliothek Johannstadt",
                description = "",
                latitude = 51.049157283542556,
                longitude = 13.76885322522458
            ),
            Waypoint(
                id = "9",
                title = "Bibliothek Klotzsche",
                description = "",
                latitude = 51.11939784488856,
                longitude = 13.769891149084087
            ),
            Waypoint(
                id = "10",
                title = "Bibliothek Langebrück",
                description = "",
                latitude = 51.12724800127986,
                longitude = 13.843881553437907
            ),
            Waypoint(
                id = "11",
                title = "Bibliothek Laubegast",
                description = "",
                latitude = 51.02500620146082,
                longitude = 13.839257737292995
            ),
            Waypoint(
                id = "12",
                title = "Bibliothek Leubnitz-Neuostra",
                description = "",
                latitude = 51.02165602307906,
                longitude = 13.764028928309257
            ),
            Waypoint(
                id = "13",
                title = "Bibliothek Neustadt",
                description = "",
                latitude = 51.06747146177359,
                longitude = 13.747596329492392
            ),
            Waypoint(
                id = "14",
                title = "Bibliothek Pieschen",
                description = "",
                latitude = 51.07799264408835,
                longitude = 13.720705561235228
            ),
            Waypoint(
                id = "15",
                title = "Bibliothek Plauen",
                description = "",
                latitude = 51.03469458786578,
                longitude = 13.70551168163574
            ),
            Waypoint(
                id = "16",
                title = "Bibliothek Prohlis",
                description = "",
                latitude = 51.006203894415236,
                longitude = 13.798450667932558
            ),
            Waypoint(
                id = "17",
                title = "Bibliothek Strehlen",
                description = "",
                latitude = 51.01971306378044,
                longitude = 13.780271698657312
            ),
            Waypoint(
                id = "18",
                title = "Bibliothek Südvorstadt",
                description = "",
                latitude = 51.03399912872732,
                longitude = 13.721460388888648
            ),
            Waypoint(
                id = "19",
                title = "Bibliothek Weißig",
                description = "",
                latitude = 51.06231646773942,
                longitude = 13.884955722572409
            ),
            Waypoint(
                id = "20",
                title = "Bibliothek Weixdorf",
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

    // Function to add waypoint markers
    fun addWaypoints(mapView: MapView) {
        waypoints.forEach { waypoint ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(waypoint.latitude, waypoint.longitude)
                title = waypoint.title
                snippet = waypoint.description
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
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