package com.wjs.arnav.domain.navigation

import com.wjs.arnav.core.common.TimeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArrivalTrackerTest {
    private val fakeTimeProvider = FakeTimeProvider()
    private val tracker = ArrivalTracker(
        timeProvider = fakeTimeProvider,
        policy = ArrivalThresholdPolicy(
            arrivalRadiusMeters = 10.0,
            dwellTimeMillis = 3_000L,
        ),
    )

    @Test
    fun `inside radius starts dwell timer without arriving immediately`() {
        fakeTimeProvider.nowMillis = 1_000L

        val state = tracker.update(
            previousState = ArrivalTrackingState(),
            distanceMeters = 8.0,
        )

        assertEquals(1_000L, state.enteredRadiusAtMillis)
        assertFalse(state.hasArrived)
    }

    @Test
    fun `staying inside radius long enough marks arrival`() {
        val initialState = ArrivalTrackingState(enteredRadiusAtMillis = 1_000L)
        fakeTimeProvider.nowMillis = 4_100L

        val state = tracker.update(
            previousState = initialState,
            distanceMeters = 6.0,
        )

        assertTrue(state.hasArrived)
        assertEquals(1_000L, state.enteredRadiusAtMillis)
    }

    @Test
    fun `leaving radius resets dwell tracking`() {
        val previousState = ArrivalTrackingState(
            enteredRadiusAtMillis = 1_000L,
            hasArrived = false,
        )
        fakeTimeProvider.nowMillis = 2_000L

        val state = tracker.update(
            previousState = previousState,
            distanceMeters = 14.0,
        )

        assertEquals(null, state.enteredRadiusAtMillis)
        assertFalse(state.hasArrived)
    }
}

private class FakeTimeProvider(
    var nowMillis: Long = 0L,
) : TimeProvider {
    override fun currentTimeMillis(): Long = nowMillis
}
