package com.wjs.arnav.feature.ar

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.wjs.arnav.R
import com.wjs.arnav.core.logging.AppLogger

/**
 * Minimal lifecycle-aware ARCore session owner for the stage-00 spike.
 */
class ArCoreSessionLifecycleHelper(
    private val activity: Activity,
    private val onSessionStatusChange: (String) -> Unit,
) : DefaultLifecycleObserver {
    companion object {
        private const val TAG = "ArCore-SessionLifecycle"
    }

    var installRequested = false
        private set

    var session: Session? = null
        private set

    private fun tryCreateSession(): Session? {
        return try {
            when (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    installRequested = true
                    AppLogger.debug(TAG, "ARCore install requested")
                    onSessionStatusChange(activity.getString(R.string.ar_spike_session_install_requested))
                    null
                }

                ArCoreApk.InstallStatus.INSTALLED -> {
                    installRequested = false
                    Session(activity).also { createdSession ->
                        AppLogger.debug(TAG, "ARCore session created")
                        onSessionStatusChange(activity.getString(R.string.ar_spike_session_ready))
                        configureSession(createdSession)
                    }
                }
            }
        } catch (error: Exception) {
            onSessionStatusChange(
                activity.getString(
                    R.string.ar_spike_session_error,
                    error.localizedMessage ?: error.javaClass.simpleName,
                ),
            )
            null
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        val currentSession = session ?: tryCreateSession() ?: return
        try {
            configureSession(currentSession)
            currentSession.resume()
            session = currentSession
            AppLogger.debug(TAG, "ARCore session resumed")
        } catch (error: CameraNotAvailableException) {
            AppLogger.error(TAG, "ARCore session resume failed", error)
            onSessionStatusChange(
                activity.getString(
                    R.string.ar_spike_session_error,
                    error.localizedMessage ?: error.javaClass.simpleName,
                ),
            )
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        AppLogger.debug(TAG, "ARCore session paused")
        session?.pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        AppLogger.debug(TAG, "ARCore session destroyed")
        session?.close()
        session = null
    }

    private fun configureSession(session: Session) {
        val config = Config(session).apply {
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            focusMode = Config.FocusMode.AUTO
            updateMode = Config.UpdateMode.BLOCKING
            depthMode = Config.DepthMode.DISABLED
            lightEstimationMode = Config.LightEstimationMode.DISABLED
        }
        session.configure(config)
    }
}
