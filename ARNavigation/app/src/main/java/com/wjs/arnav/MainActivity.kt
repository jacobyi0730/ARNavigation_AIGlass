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
import com.wjs.arnav.core.ui.theme.ARNavigationTheme
import com.wjs.arnav.feature.ar.ArCoreSpikeScreen
import com.wjs.arnav.feature.map.MapEditMode
import com.wjs.arnav.feature.map.PrototypeMapRoute
import com.wjs.arnav.feature.map.PrototypeMapState

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

        PrototypeNavHost(
            navController = navController,
            mapState = mapState,
            onMapStateChange = { mapState = it },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrototypeNavHost(
    navController: NavHostController,
    mapState: PrototypeMapState,
    onMapStateChange: (PrototypeMapState) -> Unit,
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
                )
            }

            composable(AppDestination.MAP.name) {
                PrototypeMapRoute(
                    state = mapState,
                    onNavigateToAr = {
                        navController.popBackStack()
                    },
                    onStateChange = onMapStateChange,
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

@Preview(showBackground = true)
@Composable
private fun ARNavigationPreview() {
    ARNavigationApp()
}
