package com.wjs.arnav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.LatLng
import com.wjs.arnav.domain.navigation.GeoCoordinate
import com.wjs.arnav.domain.navigation.NavigationPhase
import com.wjs.arnav.domain.navigation.NavigationSessionState
import com.wjs.arnav.core.ui.theme.ARNavigationTheme
import com.wjs.arnav.feature.ar.ArCoreSpikeScreen
import com.wjs.arnav.feature.map.MapEditMode
import com.wjs.arnav.feature.map.PrototypeMapRoute
import com.wjs.arnav.feature.map.PrototypeMapState
import com.wjs.arnav.feature.map.toGeoCoordinate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ARNavigationApp()
        }
    }
}

private enum class AppDestination {
    AR,
    MAP,
}

@Composable
private fun ARNavigationApp() {
    ARNavigationTheme {
        val navController = rememberNavController()
        var mapState by rememberSaveable(stateSaver = PrototypeMapStateSaver) {
            mutableStateOf(PrototypeMapState())
        }
        var navigationSession by rememberSaveable(stateSaver = NavigationSessionStateSaver) {
            mutableStateOf(NavigationSessionState())
        }

        PrototypeNavHost(
            navController = navController,
            mapState = mapState,
            navigationSession = navigationSession,
            onMapStateChange = { mapState = it },
            onNavigationSessionChange = { navigationSession = it },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrototypeNavHost(
    navController: NavHostController,
    mapState: PrototypeMapState,
    navigationSession: NavigationSessionState,
    onMapStateChange: (PrototypeMapState) -> Unit,
    onNavigationSessionChange: (NavigationSessionState) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.AR.name,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppDestination.AR.name) {
                ArCoreSpikeScreen(
                    onOpenMap = {
                        navController.navigate(AppDestination.MAP.name)
                    },
                    navigationSession = navigationSession,
                    onNavigationSessionChange = onNavigationSessionChange,
                )
            }

            composable(AppDestination.MAP.name) {
                PrototypeMapRoute(
                    state = mapState,
                    navigationSession = navigationSession,
                    onNavigateToAr = {
                        navController.popBackStack()
                    },
                    onStateChange = onMapStateChange,
                    onStartNavigation = startNavigation@{ startCoordinate ->
                        val destination = mapState.destination?.toGeoCoordinate()
                            ?: return@startNavigation
                        onNavigationSessionChange(
                            NavigationSessionState.create(
                                startCoordinate = startCoordinate,
                                waypointCoordinates = mapState.waypoints.map { it.toGeoCoordinate() },
                                destinationCoordinate = destination,
                            ),
                        )
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}

private val PrototypeMapStateSaver = androidx.compose.runtime.saveable.Saver<PrototypeMapState, Any>(
    save = { state ->
        listOf(
            state.editMode.name,
            state.destination?.latitude,
            state.destination?.longitude,
            state.waypoints.flatMap { listOf(it.latitude, it.longitude) },
        )
    },
    restore = { restored ->
        val values = restored as List<*>
        val editMode = MapEditMode.valueOf(values[0] as String)
        val destinationLat = values[1] as Double?
        val destinationLng = values[2] as Double?
        val destination = if (destinationLat != null && destinationLng != null) {
            LatLng(destinationLat, destinationLng)
        } else {
            null
        }
        val waypointRaw = values[3] as List<*>
        val waypoints = waypointRaw.chunked(2).map { pair ->
            LatLng(pair[0] as Double, pair[1] as Double)
        }
        PrototypeMapState(
            destination = destination,
            waypoints = waypoints,
            editMode = editMode,
        )
    },
)

private val NavigationSessionStateSaver = androidx.compose.runtime.saveable.Saver<NavigationSessionState, Any>(
    save = { state ->
        listOf(
            state.phase.name,
            state.startCoordinate?.latitude,
            state.startCoordinate?.longitude,
            state.currentTargetIndex,
            state.targets.flatMap { target ->
                listOf(
                    target.coordinate.latitude,
                    target.coordinate.longitude,
                    target.type.name,
                    target.order,
                )
            },
        )
    },
    restore = { restored ->
        val values = restored as List<*>
        val phase = NavigationPhase.valueOf(values[0] as String)
        val startLatitude = values[1] as Double?
        val startLongitude = values[2] as Double?
        val startCoordinate = if (startLatitude != null && startLongitude != null) {
            GeoCoordinate(startLatitude, startLongitude)
        } else {
            null
        }
        val currentTargetIndex = values[3] as Int
        val targetRaw = values[4] as List<*>
        val targets = targetRaw.chunked(4).map { item ->
            com.wjs.arnav.domain.navigation.NavigationTarget(
                coordinate = GeoCoordinate(
                    latitude = item[0] as Double,
                    longitude = item[1] as Double,
                ),
                type = com.wjs.arnav.domain.navigation.NavigationTargetType.valueOf(item[2] as String),
                order = item[3] as Int?,
            )
        }
        NavigationSessionState(
            phase = phase,
            startCoordinate = startCoordinate,
            targets = targets,
            currentTargetIndex = currentTargetIndex,
        )
    },
)

@Preview(showBackground = true)
@Composable
private fun ARNavigationPreview() {
    ARNavigationApp()
}
