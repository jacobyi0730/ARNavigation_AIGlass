package com.wjs.arnav.domain.navigation

import com.wjs.arnav.core.common.TimeProvider

data class ArrivalTrackingState(
    val enteredRadiusAtMillis: Long? = null,
    val hasArrived: Boolean = false,
) {
    val isTrackingInsideRadius: Boolean
        get() = enteredRadiusAtMillis != null
}

/**
 * Tracks whether the user has stayed inside the arrival radius long enough to
 * confirm arrival without reacting to a single noisy GPS sample.
 */
class ArrivalTracker(
    private val timeProvider: TimeProvider,
    private val policy: ArrivalThresholdPolicy = ArrivalThresholdPolicy(),
) {
    fun update(
        previousState: ArrivalTrackingState,
        distanceMeters: Double,
        sampleTimeMillis: Long = timeProvider.currentTimeMillis(),
    ): ArrivalTrackingState {
        if (distanceMeters > policy.arrivalRadiusMeters) {
            return ArrivalTrackingState()
        }

        val enteredAt = previousState.enteredRadiusAtMillis ?: sampleTimeMillis
        val dwellSatisfied = sampleTimeMillis - enteredAt >= policy.dwellTimeMillis
        return ArrivalTrackingState(
            enteredRadiusAtMillis = enteredAt,
            hasArrived = previousState.hasArrived || dwellSatisfied,
        )
    }
}
