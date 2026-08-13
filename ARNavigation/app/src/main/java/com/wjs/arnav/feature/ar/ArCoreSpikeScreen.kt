package com.wjs.arnav.feature.ar

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.opengl.GLSurfaceView
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.wjs.arnav.domain.navigation.ArrivalTracker
import com.wjs.arnav.domain.navigation.ArrivalTrackingState
import com.wjs.arnav.domain.navigation.GeoCoordinate
import com.wjs.arnav.domain.navigation.NavigationPhase
import com.wjs.arnav.domain.navigation.NavigationSessionState
import com.wjs.arnav.domain.navigation.NavigationTargetType
import com.wjs.arnav.domain.navigation.distanceMeters
import com.wjs.arnav.domain.navigation.initialBearingDegrees
import com.wjs.arnav.domain.navigation.normalizeSignedDegrees
import com.wjs.arnav.R
import com.wjs.arnav.core.common.SystemTimeProvider

/**
 * Stage-00 prototype screen: shows a live rear-camera feed with a simple overlay so
 * the user can visually confirm the AR screen shell before the full ARCore renderer lands.
 */
@Composable
fun ArCoreSpikeScreen(
    onOpenMap: () -> Unit,
    navigationSession: NavigationSessionState,
    onNavigationSessionChange: (NavigationSessionState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val glSurfaceViewState = remember { mutableStateOf<GLSurfaceView?>(null) }
    val glSurfaceView = glSurfaceViewState.value
    val availability = remember(context) {
        context.initialArCoreAvailabilityLabel()
    }
    val hasCameraPermission = rememberCameraPermissionState(context)
    var arCoreSessionStatus by rememberSaveable {
        mutableStateOf(context.getString(R.string.ar_spike_session_waiting_permission))
    }
    var installRequested by rememberSaveable { mutableStateOf(false) }
    var trackingStatus by rememberSaveable {
        mutableStateOf(context.getString(R.string.ar_spike_tracking_waiting))
    }
    var planeStatus by rememberSaveable {
        mutableStateOf(context.getString(R.string.ar_spike_plane_searching))
    }
    var arrowStatus by rememberSaveable {
        mutableStateOf(context.getString(R.string.ar_spike_arrow_pending))
    }
    var isDebugPanelVisible by rememberSaveable { mutableStateOf(false) }
    val hasLocationPermission = rememberLocationPermissionState(context)
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var headingDegrees by remember { mutableStateOf<Float?>(null) }
    var arrivalTrackingState by remember { mutableStateOf(ArrivalTrackingState()) }
    val arrivalTracker = remember { ArrivalTracker(SystemTimeProvider) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission.value = granted
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasLocationPermission.value = granted
    }
    val sessionHelper: ArCoreSessionLifecycleHelper? = remember(activity) {
        activity?.let {
            ArCoreSessionLifecycleHelper(it) { status ->
                arCoreSessionStatus = status
            }
        }
    }
    val renderer: ArCorePlaneSpikeRenderer? = remember(activity, sessionHelper) {
        if (activity != null && sessionHelper != null) {
            ArCorePlaneSpikeRenderer(
                activity = activity,
                sessionHelper = sessionHelper,
                onTrackingStatusChange = { trackingStatus = it },
                onPlaneStatusChange = { planeStatus = it },
                onArrowStatusChange = { arrowStatus = it },
            )
        } else {
            null
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission.value) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    LaunchedEffect(navigationSession.isNavigating, hasLocationPermission.value) {
        if (navigationSession.isNavigating && !hasLocationPermission.value) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun updateArCoreSessionStatus() {
        arCoreSessionStatus = context.runArCoreSessionProbe(
            installRequested = installRequested,
            onInstallRequestedChange = { installRequested = it },
        )
    }

    LaunchedEffect(hasCameraPermission.value) {
        if (hasCameraPermission.value) {
            updateArCoreSessionStatus()
        } else {
            arCoreSessionStatus = context.getString(R.string.ar_spike_session_waiting_permission)
        }
    }

    DisposableEffect(context, hasLocationPermission.value, navigationSession.isNavigating) {
        if (!hasLocationPermission.value || !navigationSession.isNavigating) {
            onDispose { }
        } else {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000L)
                .setMinUpdateIntervalMillis(500L)
                .build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    currentLocation = result.lastLocation
                }
            }
            @Suppress("MissingPermission")
            client.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                }
            }
            @Suppress("MissingPermission")
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            onDispose {
                client.removeLocationUpdates(callback)
            }
        }
    }

    DisposableEffect(context, navigationSession.isNavigating) {
        if (!navigationSession.isNavigating) {
            onDispose { }
        } else {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            if (sensor == null) {
                onDispose { }
            } else {
                val listener = object : SensorEventListener {
                    private val rotationMatrix = FloatArray(9)
                    private val orientationAngles = FloatArray(3)

                    override fun onSensorChanged(event: SensorEvent) {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        headingDegrees = ((Math.toDegrees(orientationAngles[0].toDouble()) + 360.0) % 360.0).toFloat()
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                }
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
                onDispose {
                    sensorManager.unregisterListener(listener)
                }
            }
        }
    }

    val currentTarget = navigationSession.currentTarget()
    val targetLabel = remember(currentTarget, context) {
        when (currentTarget?.type) {
            NavigationTargetType.WAYPOINT -> context.getString(
                R.string.ar_navigation_target_waypoint,
                currentTarget.order ?: 0,
            )
            NavigationTargetType.DESTINATION -> context.getString(R.string.ar_navigation_target_destination)
            null -> context.getString(R.string.ar_navigation_not_started)
        }
    }
    val distanceToTargetMeters = remember(currentLocation, currentTarget) {
        if (currentLocation != null && currentTarget != null) {
            distanceMeters(
                GeoCoordinate(currentLocation!!.latitude, currentLocation!!.longitude),
                currentTarget.coordinate,
            )
        } else {
            null
        }
    }
    val relativeBearingDegrees = remember(currentLocation, currentTarget, headingDegrees) {
        if (currentLocation != null && currentTarget != null && headingDegrees != null) {
            normalizeSignedDegrees(
                initialBearingDegrees(
                    from = GeoCoordinate(currentLocation!!.latitude, currentLocation!!.longitude),
                    to = currentTarget.coordinate,
                ) - headingDegrees!!.toDouble(),
            ).toFloat()
        } else {
            null
        }
    }

    LaunchedEffect(relativeBearingDegrees, currentTarget?.type, renderer) {
        val indicatorColor = if (currentTarget?.type == NavigationTargetType.WAYPOINT) {
            floatArrayOf(0.19f, 0.51f, 0.98f, 1.0f)
        } else {
            floatArrayOf(0.14f, 0.74f, 0.37f, 1.0f)
        }
        renderer?.updateNavigationGuidance(relativeBearingDegrees, indicatorColor)
    }

    LaunchedEffect(distanceToTargetMeters, navigationSession.currentTargetIndex, navigationSession.phase) {
        if (!navigationSession.isNavigating || distanceToTargetMeters == null) {
            arrivalTrackingState = ArrivalTrackingState()
            return@LaunchedEffect
        }
        val updatedState = arrivalTracker.update(
            previousState = arrivalTrackingState,
            distanceMeters = distanceToTargetMeters,
        )
        arrivalTrackingState = updatedState
        if (updatedState.hasArrived) {
            onNavigationSessionChange(navigationSession.advanceToNextTarget())
            arrivalTrackingState = ArrivalTrackingState()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCameraPermission.value) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    GLSurfaceView(viewContext).apply {
                        glSurfaceViewState.value = this
                        renderer?.attachSurfaceView(this)
                    }
                },
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = {
                    isDebugPanelVisible = !isDebugPanelVisible
                },
            ) {
                Text(
                    text = if (isDebugPanelVisible) {
                        stringResource(R.string.ar_debug_hide)
                    } else {
                        stringResource(R.string.ar_debug_show)
                    },
                )
            }

            if (isDebugPanelVisible) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.92f),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.ar_spike_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = if (hasCameraPermission.value) {
                                stringResource(R.string.ar_spike_renderer_camera)
                            } else {
                                stringResource(R.string.ar_spike_permission_needed)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = stringResource(R.string.ar_spike_availability_prefix, availability),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = stringResource(R.string.ar_spike_session_status_prefix, arCoreSessionStatus),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = stringResource(R.string.ar_spike_tracking_status_prefix, trackingStatus),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = stringResource(R.string.ar_spike_plane_status_prefix, planeStatus),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = stringResource(R.string.ar_spike_arrow_status_prefix, arrowStatus),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (navigationSession.isNavigating) {
                            Text(
                                text = stringResource(R.string.ar_navigation_target_prefix, targetLabel),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = stringResource(
                                    R.string.ar_navigation_distance_prefix,
                                    distanceToTargetMeters?.toDistanceLabel()
                                        ?: context.getString(R.string.ar_navigation_distance_unknown),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = stringResource(
                                    R.string.ar_navigation_heading_prefix,
                                    relativeBearingDegrees?.toInt()?.toString()
                                        ?: context.getString(R.string.ar_navigation_distance_unknown),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (!hasCameraPermission.value) {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                            ) {
                                Text(text = stringResource(R.string.ar_spike_request_camera))
                            }
                        }
                        Button(
                            onClick = ::updateArCoreSessionStatus,
                            enabled = hasCameraPermission.value,
                        ) {
                            Text(text = stringResource(R.string.ar_spike_retry_arcore))
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = onOpenMap) {
                Text(text = stringResource(R.string.ar_spike_open_map))
            }
            if (navigationSession.isNavigating || navigationSession.phase == NavigationPhase.ARRIVED) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        onNavigationSessionChange(navigationSession.reset())
                        arrivalTrackingState = ArrivalTrackingState()
                    },
                ) {
                    Text(text = stringResource(R.string.ar_navigation_end))
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, sessionHelper, renderer) {
        if (sessionHelper != null) {
            lifecycleOwner.lifecycle.addObserver(sessionHelper)
        }
        if (renderer != null) {
            lifecycleOwner.lifecycle.addObserver(renderer)
        }
        onDispose {
            if (renderer != null) {
                lifecycleOwner.lifecycle.removeObserver(renderer)
            }
            if (sessionHelper != null) {
                lifecycleOwner.lifecycle.removeObserver(sessionHelper)
            }
        }
    }

    DisposableEffect(lifecycleOwner, glSurfaceView) {
        val surfaceView = glSurfaceView ?: return@DisposableEffect onDispose {}
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                surfaceView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                surfaceView.onPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            surfaceView.onResume()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            surfaceView.onPause()
        }
    }
}

@Composable
private fun rememberCameraPermissionState(
    context: Context,
): androidx.compose.runtime.MutableState<Boolean> {
    return remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
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

private fun Context.initialArCoreAvailabilityLabel(): String {
    return ArCoreApk.getInstance().checkAvailability(this).toDisplayLabel()
}

private fun Context.runArCoreSessionProbe(
    installRequested: Boolean,
    onInstallRequestedChange: (Boolean) -> Unit,
): String {
    val activity = findActivity()
        ?: return getString(R.string.ar_spike_session_missing_activity)

    return try {
        when (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
            ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                onInstallRequestedChange(true)
                getString(R.string.ar_spike_session_install_requested)
            }

            ArCoreApk.InstallStatus.INSTALLED -> {
                onInstallRequestedChange(false)
                val session = Session(this)
                session.close()
                getString(R.string.ar_spike_session_ready)
            }
        }
    } catch (_: UnavailableArcoreNotInstalledException) {
        getString(R.string.ar_spike_session_not_installed)
    } catch (_: UnavailableUserDeclinedInstallationException) {
        getString(R.string.ar_spike_session_install_declined)
    } catch (_: UnavailableApkTooOldException) {
        getString(R.string.ar_spike_session_apk_too_old)
    } catch (_: UnavailableSdkTooOldException) {
        getString(R.string.ar_spike_session_sdk_too_old)
    } catch (_: UnavailableDeviceNotCompatibleException) {
        getString(R.string.ar_spike_session_not_supported)
    } catch (error: SecurityException) {
        getString(R.string.ar_spike_session_security_error, error.localizedMessage ?: error.javaClass.simpleName)
    } catch (error: Exception) {
        getString(R.string.ar_spike_session_error, error.localizedMessage ?: error.javaClass.simpleName)
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private fun ArCoreApk.Availability.toDisplayLabel(): String {
    return name
}

private fun Double.toDistanceLabel(): String {
    return if (this >= 1000.0) {
        String.format("%.2f km", this / 1000.0)
    } else {
        String.format("%.0f m", this)
    }
}
