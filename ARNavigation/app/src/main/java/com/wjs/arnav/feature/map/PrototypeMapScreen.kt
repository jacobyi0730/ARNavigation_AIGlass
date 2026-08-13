package com.wjs.arnav.feature.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.wjs.arnav.BuildConfig
import com.wjs.arnav.R

private val SeoulCityHall = LatLng(37.5663, 126.9779)

@Composable
fun PrototypeMapRoute(
    state: PrototypeMapState,
    onNavigateToAr: () -> Unit,
    onStateChange: (PrototypeMapState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(SeoulCityHall, 15f)
    }

    LaunchedEffect(state.destination, state.waypoints.size) {
        val focusPoint = state.destination ?: state.waypoints.lastOrNull() ?: SeoulCityHall
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLng(focusPoint),
            durationMs = 600,
        )
    }

    PrototypeMapScreen(
        state = state,
        cameraPositionState = cameraPositionState,
        onNavigateToAr = onNavigateToAr,
        onStateChange = onStateChange,
        modifier = modifier,
    )
}

@Composable
private fun PrototypeMapScreen(
    state: PrototypeMapState,
    cameraPositionState: CameraPositionState,
    onNavigateToAr: () -> Unit,
    onStateChange: (PrototypeMapState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val hasLocationPermission = rememberLocationPermissionState(context)
    val destinationMarkerTitle = stringResource(R.string.map_marker_destination_title)
    val destinationMarkerSnippet = stringResource(R.string.map_marker_destination_snippet)
    val waypointMarkerSnippet = stringResource(R.string.map_marker_waypoint_snippet)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasLocationPermission.value = granted
    }
    val mapUiSettings = remember(hasLocationPermission.value) {
        MapUiSettings(
            myLocationButtonEnabled = false,
            compassEnabled = true,
            zoomControlsEnabled = false,
        )
    }
    val mapProperties = remember(hasLocationPermission.value) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission.value,
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings,
            properties = mapProperties,
            onMapClick = { tappedPoint ->
                onStateChange(state.onMapTap(tappedPoint))
            },
        ) {
            state.destination?.let { destination ->
                Marker(
                    state = MarkerState(destination),
                    title = destinationMarkerTitle,
                    snippet = destinationMarkerSnippet,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                )
            }

            state.waypoints.forEachIndexed { index, waypoint ->
                val waypointMarkerTitle = context.getString(R.string.map_marker_waypoint_title, index + 1)
                Marker(
                    state = MarkerState(waypoint),
                    title = waypointMarkerTitle,
                    snippet = waypointMarkerSnippet,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    onClick = {
                        onStateChange(state.removeWaypoint(index))
                        true
                    },
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!BuildConfig.HAS_MAPS_API_KEY) {
                StatusCard(
                    text = stringResource(R.string.map_missing_api_key),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                )
            }
            if (!hasLocationPermission.value) {
                StatusCard(
                    text = stringResource(R.string.map_permission_needed),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                )
            }

            StatusCard(
                text = when (state.editMode) {
                    MapEditMode.NONE -> stringResource(R.string.map_mode_browse)
                    MapEditMode.DESTINATION -> stringResource(R.string.map_mode_destination)
                    MapEditMode.WAYPOINT -> stringResource(R.string.map_mode_waypoint)
                },
            )
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.map_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(
                        R.string.map_destination_label,
                        state.destination?.toShortLabel()
                            ?: stringResource(R.string.map_destination_not_set),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.map_waypoints_label, state.waypoints.size),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            onStateChange(state.copy(editMode = MapEditMode.DESTINATION))
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.map_button_destination))
                    }
                    Button(
                        onClick = {
                            val nextMode = if (state.editMode == MapEditMode.WAYPOINT) {
                                MapEditMode.NONE
                            } else {
                                MapEditMode.WAYPOINT
                            }
                            onStateChange(state.copy(editMode = nextMode))
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = if (state.editMode == MapEditMode.WAYPOINT) {
                                stringResource(R.string.map_button_done)
                            } else {
                                stringResource(R.string.map_button_waypoint)
                            },
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            if (hasLocationPermission.value) {
                                cameraPositionState.moveToCurrentLocation(context)
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.map_recenter))
                    }
                    OutlinedButton(
                        onClick = {
                            onStateChange(PrototypeMapState())
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.map_button_reset))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (!hasLocationPermission.value) {
                        OutlinedButton(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(text = stringResource(R.string.map_request_location))
                        }
                    }
                    Button(
                        onClick = onNavigateToAr,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = stringResource(R.string.map_button_back_to_ar))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = containerColor,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun LatLng.toShortLabel(): String {
    return String.format("%.5f, %.5f", latitude, longitude)
}

@Composable
private fun rememberLocationPermissionState(
    context: Context,
): androidx.compose.runtime.MutableState<Boolean> {
    return remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
}

private fun CameraPositionState.moveToCurrentLocation(context: Context) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    @Suppress("MissingPermission")
    client.lastLocation.addOnSuccessListener { location ->
        location ?: return@addOnSuccessListener
        move(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude, location.longitude),
                17f,
            ),
        )
    }
}
