package com.wjs.arnav.core.logging

import org.junit.Assert.assertEquals
import org.junit.Test

class LogSanitizerTest {
    @Test
    fun `coordinate summary redacts coordinates when precise logging is disabled`() {
        val summary = LogSanitizer.coordinateSummary(
            label = "currentLocation",
            latitude = 37.5665,
            longitude = 126.9780,
            allowPrecise = false,
        )

        assertEquals("currentLocation([REDACTED_COORDINATES])", summary)
    }
}
