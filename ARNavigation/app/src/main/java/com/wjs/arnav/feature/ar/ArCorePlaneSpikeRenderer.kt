package com.wjs.arnav.feature.ar

import android.app.Activity
import android.opengl.GLSurfaceView
import android.opengl.Matrix
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
import com.wjs.arnav.core.logging.AppLogger
import kotlin.math.sqrt
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
        private const val ArrowLiftMeters = 0.05f
        private const val ArrowScale = 1.0f
        private const val ArrowTiltDegrees = 0f
        private const val ReanchorViewportMargin = 0.92f
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
    private var anchoredArrowYawDegrees: Float? = null
    private var hasSetTextureNames = false
    private var viewportWidth = 1
    private var viewportHeight = 1
    private var relativeBearingDegrees: Float? = null
    private var arrowColor = floatArrayOf(0.14f, 0.74f, 0.37f, 1.0f)

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)
    private val anchorWorldPoint = FloatArray(4)
    private val anchorViewPoint = FloatArray(4)
    private val anchorClipPoint = FloatArray(4)

    fun attachSurfaceView(surfaceView: GLSurfaceView) {
        if (sampleRender == null) {
            AppLogger.debug(TAG, "GLSurfaceView attached")
            sampleRender = SampleRender(surfaceView, this, activity.assets)
        }
    }

    fun updateNavigationGuidance(
        relativeBearingDegrees: Float?,
        indicatorColor: FloatArray,
    ) {
        this.relativeBearingDegrees = relativeBearingDegrees
        this.arrowColor = indicatorColor
    }

    override fun onResume(owner: LifecycleOwner) {
        AppLogger.debug(TAG, "Renderer resumed")
        displayRotationHelper.onResume()
        hasSetTextureNames = false
    }

    override fun onPause(owner: LifecycleOwner) {
        AppLogger.debug(TAG, "Renderer paused")
        displayRotationHelper.onPause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        AppLogger.debug(TAG, "Renderer destroyed")
        arrowAnchor?.detach()
        arrowAnchor = null
        anchoredArrowYawDegrees = null
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
            anchoredArrowYawDegrees = null
        }

        if (arrowAnchor == null) {
            tryPlaceArrowAnchor(frame, camera)
        } else if (shouldReanchor(camera) || !isAnchorVisibleInViewport()) {
            val preservedYawDegrees = anchoredArrowYawDegrees
            arrowAnchor?.detach()
            arrowAnchor = null
            anchoredArrowYawDegrees = null
            tryPlaceArrowAnchor(frame, camera, preservedYawDegrees)
        }

        val currentAnchor = arrowAnchor
        if (currentAnchor?.trackingState == TrackingState.TRACKING) {
            buildFloorParallelModelMatrix(
                anchor = currentAnchor,
                yawDegrees = anchoredArrowYawDegrees ?: targetYawDegrees(),
            )
            Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)
            currentArrowShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
            currentArrowShader.setVec4("u_Color", arrowColor)
            render.draw(currentArrowMesh, currentArrowShader)
            onArrowStatusChange(activity.getString(R.string.ar_spike_arrow_placed))
        } else if (hasTrackedHorizontalPlane) {
            onArrowStatusChange(activity.getString(R.string.ar_spike_arrow_pending))
        } else {
            onArrowStatusChange(activity.getString(R.string.ar_spike_arrow_pending))
        }
    }

    private fun shouldReanchor(camera: Camera): Boolean {
        val currentAnchor = arrowAnchor ?: return false
        val anchorPose = currentAnchor.pose
        val cameraPose = camera.pose
        val dx = anchorPose.tx() - cameraPose.tx()
        val dy = anchorPose.ty() - cameraPose.ty()
        val dz = anchorPose.tz() - cameraPose.tz()
        val distanceMeters = sqrt(dx * dx + dy * dy + dz * dz)
        return distanceMeters > 2.5f || distanceMeters < 0.4f
    }

    private fun buildFloorParallelModelMatrix(
        anchor: Anchor,
        yawDegrees: Float,
    ) {
        val anchorPose = anchor.pose
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(
            modelMatrix,
            0,
            anchorPose.tx(),
            anchorPose.ty() + ArrowLiftMeters,
            anchorPose.tz(),
        )
        Matrix.rotateM(modelMatrix, 0, yawDegrees, 0f, 1f, 0f)
        Matrix.rotateM(modelMatrix, 0, ArrowTiltDegrees, 1f, 0f, 0f)
        Matrix.scaleM(modelMatrix, 0, ArrowScale, ArrowScale, ArrowScale)
    }

    private fun isAnchorVisibleInViewport(): Boolean {
        val anchorPose = arrowAnchor?.pose ?: return false
        anchorWorldPoint[0] = anchorPose.tx()
        anchorWorldPoint[1] = anchorPose.ty() + ArrowLiftMeters
        anchorWorldPoint[2] = anchorPose.tz()
        anchorWorldPoint[3] = 1f

        Matrix.multiplyMV(anchorViewPoint, 0, viewMatrix, 0, anchorWorldPoint, 0)
        Matrix.multiplyMV(anchorClipPoint, 0, projectionMatrix, 0, anchorViewPoint, 0)

        val clipW = anchorClipPoint[3]
        if (clipW <= 0f) {
            return false
        }

        val ndcX = anchorClipPoint[0] / clipW
        val ndcY = anchorClipPoint[1] / clipW
        val ndcZ = anchorClipPoint[2] / clipW
        return ndcX in -ReanchorViewportMargin..ReanchorViewportMargin &&
            ndcY in -ReanchorViewportMargin..ReanchorViewportMargin &&
            ndcZ in -1f..1f
    }

    private fun tryPlaceArrowAnchor(
        frame: Frame,
        camera: Camera,
        yawDegrees: Float? = null,
    ) {
        val centerX = viewportWidth / 2f
        val floorProbeY = viewportHeight * 0.78f
        val hit = frame.hitTest(centerX, floorProbeY).firstOrNull { hitResult ->
            val trackable = hitResult.trackable
            trackable is Plane &&
                trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                trackable.isPoseInPolygon(hitResult.hitPose) &&
                PlaneRenderer.calculateDistanceToPlane(hitResult.hitPose, camera.pose) > 0
        }
        if (hit != null) {
            arrowAnchor = hit.createAnchor()
            anchoredArrowYawDegrees = yawDegrees ?: targetYawDegrees()
            onArrowStatusChange(activity.getString(R.string.ar_spike_arrow_placed))
        }
    }

    private fun targetYawDegrees(): Float {
        return 180f - (relativeBearingDegrees ?: 0f)
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
            // Chevron 1 outer/inner rails.
            -0.26f, 0f, 0.22f,
            -0.11f, 0f, 0.15f,
            0f, 0f, 0.03f,
            0.11f, 0f, 0.15f,
            0.26f, 0f, 0.22f,

            // Chevron 2.
            -0.21f, 0f, -0.02f,
            -0.09f, 0f, -0.08f,
            0f, 0f, -0.18f,
            0.09f, 0f, -0.08f,
            0.21f, 0f, -0.02f,

            // Chevron 3.
            -0.16f, 0f, -0.24f,
            -0.07f, 0f, -0.29f,
            0f, 0f, -0.37f,
            0.07f, 0f, -0.29f,
            0.16f, 0f, -0.24f,
        )
        val indices = intArrayOf(
            0, 1, 2,
            2, 1, 0,
            2, 3, 4,
            4, 3, 2,
            5, 6, 7,
            7, 6, 5,
            7, 8, 9,
            9, 8, 7,
            10, 11, 12,
            12, 11, 10,
            12, 13, 14,
            14, 13, 12,
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
