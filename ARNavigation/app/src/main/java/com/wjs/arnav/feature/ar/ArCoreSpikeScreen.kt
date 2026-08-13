package com.wjs.arnav.feature.ar

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.wjs.arnav.R

/**
 * Stage-00 prototype screen: shows a live rear-camera feed with a simple overlay so
 * the user can visually confirm the AR screen shell before the full ARCore renderer lands.
 */
@Composable
fun ArCoreSpikeScreen(
    onOpenMap: () -> Unit,
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
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission.value = granted
    }
    val sessionHelper = remember(activity) {
        activity?.let {
            ArCoreSessionLifecycleHelper(it) { status ->
                arCoreSessionStatus = status
            }
        }
    }
    val renderer = remember(activity, sessionHelper) {
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

        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
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
                Button(onClick = onOpenMap) {
                    Text(text = stringResource(R.string.ar_spike_open_map))
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
