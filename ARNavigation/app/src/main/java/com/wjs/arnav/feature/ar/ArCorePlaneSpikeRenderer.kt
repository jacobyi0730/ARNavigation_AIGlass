package com.wjs.arnav.feature.ar

import android.app.Activity
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.Anchor
import com.google.ar.core.Camera
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper
import com.google.ar.core.examples.java.common.samplerender.IndexBuffer
import com.google.ar.core.examples.java.common.samplerender.Mesh
import com.google.ar.core.examples.java.common.samplerender.SampleRender
import com.google.ar.core.examples.java.common.samplerender.Shader
import com.google.ar.core.examples.java.common.samplerender.VertexBuffer
import com.google.ar.core.examples.java.common.samplerender.arcore.BackgroundRenderer
import com.google.ar.core.examples.java.common.samplerender.arcore.PlaneRenderer
import com.wjs.arnav.R
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ArCorePlaneSpikeRenderer(
    private val activity: Activity,
    private val sessionHelper: ArCoreSessionLifecycleHelper,
    private val onTrackingStatusChange: (String) -> Unit,
    private val onPlaneStatusChange: (String) -> Unit,
    private val onArrowStatusChange: (String) -> Unit,
) : SampleRender.Renderer, DefaultLifecycleObserver {
    companion object {
        private const val TAG = "ArCore-ArStatus"
    }

    private val session: Session?
        get() = sessionHelper.session

    private val displayRotationHelper = DisplayRotationHelper(activity)
    private var sampleRender: SampleRender? = null
    private var backgroundRenderer: BackgroundRenderer? = null
    private var planeRenderer: PlaneRenderer? = null
    private var arrowMesh: Mesh? = null
    private var arrowShader: Shader? = null
    private var arrowAnchor: Anchor? = null
    private var hasSetTextureNames = false
    private var viewportWidth = 1
    private var viewportHeight = 1

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)
    private val rotatedModelMatrix = FloatArray(16)

    fun attachSurfaceView(surfaceView: GLSurfaceView) {
        if (sampleRender == null) {
            Log.d(TAG, "GLSurfaceView attached")
            sampleRender = SampleRender(surfaceView, this, activity.assets)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        Log.d(TAG, "Renderer resumed")
        displayRotationHelper.onResume()
        hasSetTextureNames = false
    }

    override fun onPause(owner: LifecycleOwner) {
        Log.d(TAG, "Renderer paused")
        displayRotationHelper.onPause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "Renderer destroyed")
        arrowAnchor?.detach()
        arrowAnchor = null
    }

    override fun onSurfaceCreated(render: SampleRender) {
        backgroundRenderer = BackgroundRenderer(render).also {
            it.setUseDepthVisualization(render, false)
        }
        planeRenderer = PlaneRenderer(render)
        arrowMesh = createArrowMesh(render)
        arrowShader = Shader.createFromAssets(
            render,
            "shaders/arrow_spike.vert",
            "shaders/arrow_spike.frag",
            null,
        )
    }

    override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        displayRotationHelper.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(render: SampleRender) {
        val currentSession = session ?: return
        val currentBackgroundRenderer = backgroundRenderer ?: return
        val currentPlaneRenderer = planeRenderer ?: return
        val currentArrowMesh = arrowMesh ?: return
        val currentArrowShader = arrowShader ?: return

        if (!hasSetTextureNames) {
            currentSession.setCameraTextureNames(intArrayOf(currentBackgroundRenderer.cameraColorTexture.textureId))
            hasSetTextureNames = true
        }

        displayRotationHelper.updateSessionIfNeeded(currentSession)

        val frame = try {
            currentSession.update()
        } catch (error: CameraNotAvailableException) {
            onTrackingStatusChange(
                activity.getString(
                    R.string.ar_spike_session_error,
                    error.localizedMessage ?: error.javaClass.simpleName,
                ),
            )
            return
        }

        val camera = frame.camera
        currentBackgroundRenderer.updateDisplayGeometry(frame)
        if (frame.timestamp != 0L) {
            currentBackgroundRenderer.drawBackground(render)
        }

        updateTrackingStatus(camera)

        if (camera.trackingState != TrackingState.TRACKING) {
            onPlaneStatusChange(activity.getString(R.string.ar_spike_plane_searching))
            onArrowStatusChange(activity.getString(R.string.ar_spike_arrow_lost))
            return
        }

        camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100f)
        camera.getViewMatrix(viewMatrix, 0)

        val trackedPlanes = currentSession.getAllTrackables(Plane::class.java)
        currentPlaneRenderer.drawPlanes(render, trackedPlanes, camera.displayOrientedPose, projectionMatrix)

        val hasTrackedHorizontalPlane = trackedPlanes.any { plane ->
            plane.trackingState == TrackingState.TRACKING &&
                plane.subsumedBy == null &&
                plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING
        }
        onPlaneStatusChange(
            activity.getString(
                if (hasTrackedHorizontalPlane) {
                    R.string.ar_spike_plane_detected
                } else {
                    R.string.ar_spike_plane_searching
                },
            ),
        )

        if (arrowAnchor?.trackingState == TrackingState.STOPPED) {
            arrowAnchor?.detach()
            arrowAnchor = null
        }

        if (arrowAnchor == null) {
            tryPlaceArrowAnchor(frame, camera)
        }

        val currentAnchor = arrowAnchor
        if (currentAnchor?.trackingState == TrackingState.TRACKING) {
            Matrix.setIdentityM(rotationMatrix, 0)
            Matrix.rotateM(rotationMatrix, 0, 180f, 0f, 1f, 0f)
            currentAnchor.pose.toMatrix(modelMatrix, 0)
            Matrix.multiplyMM(rotatedModelMatrix, 0, modelMatrix, 0, rotationMatrix, 0)
            Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, rotatedModelMatrix, 0)
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)
            currentArrowShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
            currentArrowShader.setVec4("u_Color", floatArrayOf(0.14f, 0.74f, 0.37f, 1.0f))
            render.draw(currentArrowMesh, currentArrowShader)
            onArrowStatusChange(activity.getString(R.string.ar_spike_arrow_placed))
        } else if (hasTrackedHorizontalPlane) {
            onArrowStatusChange(activity.getString(R.string.ar_spike_arrow_pending))
        } else {
            onArrowStatusChange(activity.getString(R.string.ar_spike_arrow_pending))
        }
    }

    private fun tryPlaceArrowAnchor(frame: Frame, camera: Camera) {
        val centerX = viewportWidth / 2f
        val floorProbeY = viewportHeight * 0.72f
        val hit = frame.hitTest(centerX, floorProbeY).firstOrNull { hitResult ->
            val trackable = hitResult.trackable
            trackable is Plane &&
                trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                trackable.isPoseInPolygon(hitResult.hitPose) &&
                PlaneRenderer.calculateDistanceToPlane(hitResult.hitPose, camera.pose) > 0
        }
        if (hit != null) {
            arrowAnchor = hit.createAnchor()
            onArrowStatusChange(activity.getString(R.string.ar_spike_arrow_placed))
        }
    }

    private fun updateTrackingStatus(camera: Camera) {
        val status = when (camera.trackingState) {
            TrackingState.TRACKING -> activity.getString(R.string.ar_spike_tracking_active)
            TrackingState.PAUSED -> {
                if (camera.trackingFailureReason == TrackingFailureReason.NONE) {
                    activity.getString(R.string.ar_spike_tracking_searching)
                } else {
                    activity.getString(R.string.ar_spike_tracking_lost)
                }
            }

            TrackingState.STOPPED -> activity.getString(R.string.ar_spike_tracking_lost)
        }
        onTrackingStatusChange(status)
    }

    private fun createArrowMesh(render: SampleRender): Mesh {
        val positions = floatArrayOf(
            0f, 0f, -0.35f,
            0.18f, 0f, -0.05f,
            0.08f, 0f, -0.05f,
            0.08f, 0f, 0.35f,
            -0.08f, 0f, 0.35f,
            -0.08f, 0f, -0.05f,
            -0.18f, 0f, -0.05f,
        )
        val indices = intArrayOf(
            0, 1, 2,
            0, 2, 5,
            0, 5, 6,
            5, 2, 3,
            5, 3, 4,
        )
        val positionBuffer = ByteBuffer.allocateDirect(positions.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(positions)
                rewind()
            }
        val indexBuffer = ByteBuffer.allocateDirect(indices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .apply {
                put(indices)
                rewind()
            }

        val vertexBuffer = VertexBuffer(render, 3, positionBuffer)
        val meshIndexBuffer = IndexBuffer(render, indexBuffer)
        return Mesh(
            render,
            Mesh.PrimitiveMode.TRIANGLES,
            meshIndexBuffer,
            arrayOf(vertexBuffer),
        )
    }
}
