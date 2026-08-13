package com.wjs.arnav.core.logging

import android.util.Log
import com.wjs.arnav.BuildConfig

object AppLogger {
    fun debug(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}

object LogSanitizer {
    private const val RedactedCoordinates = "[REDACTED_COORDINATES]"

    fun coordinateSummary(
        label: String,
        latitude: Double,
        longitude: Double,
        allowPrecise: Boolean,
    ): String {
        return if (allowPrecise) {
            "$label(lat=$latitude, lng=$longitude)"
        } else {
            "$label($RedactedCoordinates)"
        }
    }
}
