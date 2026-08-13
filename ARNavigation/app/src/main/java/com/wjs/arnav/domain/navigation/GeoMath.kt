package com.wjs.arnav.domain.navigation

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun distanceMeters(
    first: GeoCoordinate,
    second: GeoCoordinate,
): Double {
    val earthRadiusMeters = 6_371_000.0
    val latDelta = Math.toRadians(second.latitude - first.latitude)
    val lngDelta = Math.toRadians(second.longitude - first.longitude)
    val firstLat = Math.toRadians(first.latitude)
    val secondLat = Math.toRadians(second.latitude)

    val haversine = sin(latDelta / 2) * sin(latDelta / 2) +
        cos(firstLat) * cos(secondLat) * sin(lngDelta / 2) * sin(lngDelta / 2)
    val arc = 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
    return earthRadiusMeters * arc
}

fun initialBearingDegrees(
    from: GeoCoordinate,
    to: GeoCoordinate,
): Double {
    val startLat = Math.toRadians(from.latitude)
    val endLat = Math.toRadians(to.latitude)
    val lngDelta = Math.toRadians(to.longitude - from.longitude)

    val y = sin(lngDelta) * cos(endLat)
    val x = cos(startLat) * sin(endLat) - sin(startLat) * cos(endLat) * cos(lngDelta)
    return normalizeBearingDegrees(Math.toDegrees(atan2(y, x)))
}

fun normalizeBearingDegrees(degrees: Double): Double {
    return (degrees % 360.0 + 360.0) % 360.0
}

fun normalizeSignedDegrees(degrees: Double): Double {
    val normalized = normalizeBearingDegrees(degrees)
    return if (normalized > 180.0) normalized - 360.0 else normalized
}
