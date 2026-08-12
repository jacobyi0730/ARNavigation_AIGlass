package com.wjs.arnav.spike

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val availability = remember(context) {
        context.initialArCoreAvailabilityLabel()
    }
    val hasCameraPermission = rememberCameraPermissionState(context)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission.value = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission.value) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (hasCameraPermission.value) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PreviewView(viewContext).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                update = { previewView ->
                    bindRearCameraPreview(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        previewView = previewView,
                    )
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
                if (!hasCameraPermission.value) {
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                    ) {
                        Text(text = stringResource(R.string.ar_spike_request_camera))
                    }
                }
                Button(onClick = onOpenMap) {
                    Text(text = stringResource(R.string.ar_spike_open_map))
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ProcessCameraProvider.getInstance(context).get().unbindAll()
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

private fun bindRearCameraPreview(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
) {
    val cameraProvider = ProcessCameraProvider.getInstance(context).get()
    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }

    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        CameraSelector.DEFAULT_BACK_CAMERA,
        preview,
    )
}

private fun Context.initialArCoreAvailabilityLabel(): String {
    return ArCoreApk.getInstance().checkAvailability(this).toDisplayLabel()
}

private fun ArCoreApk.Availability.toDisplayLabel(): String {
    return name
}
