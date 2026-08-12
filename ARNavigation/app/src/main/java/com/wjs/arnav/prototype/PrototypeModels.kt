package com.wjs.arnav.prototype

import com.google.android.gms.maps.model.LatLng

enum class MapEditMode {
    NONE,
    DESTINATION,
    WAYPOINT,
}

data class PrototypeMapState(
    val destination: LatLng? = null,
    val waypoints: List<LatLng> = emptyList(),
    val editMode: MapEditMode = MapEditMode.NONE,
)

fun PrototypeMapState.removeWaypoint(index: Int): PrototypeMapState {
    if (index !in waypoints.indices) {
        return this
    }
    return copy(
        waypoints = waypoints.filterIndexed { currentIndex, _ ->
            currentIndex != index
        },
    )
}

fun PrototypeMapState.onMapTap(
    tappedPoint: LatLng,
    minimumSpacingMeters: Double = 15.0,
    maxWaypoints: Int = 5,
): PrototypeMapState {
    return when (editMode) {
        MapEditMode.NONE -> this
        MapEditMode.DESTINATION -> copy(
            destination = tappedPoint,
            editMode = MapEditMode.NONE,
        )

        MapEditMode.WAYPOINT -> {
            if (waypoints.size >= maxWaypoints) {
                this
            } else if (isDuplicatePoint(tappedPoint, minimumSpacingMeters)) {
                this
            } else {
                copy(waypoints = waypoints + tappedPoint)
            }
        }
    }
}

fun PrototypeMapState.isDuplicatePoint(
    tappedPoint: LatLng,
    minimumSpacingMeters: Double,
): Boolean {
    val allPoints = buildList {
        destination?.let(::add)
        addAll(waypoints)
    }

    return allPoints.any { existingPoint ->
        distanceMeters(existingPoint, tappedPoint) < minimumSpacingMeters
    }
}

private fun distanceMeters(
    first: LatLng,
    second: LatLng,
): Double {
    val earthRadiusMeters = 6_371_000.0
    val latDelta = Math.toRadians(second.latitude - first.latitude)
    val lngDelta = Math.toRadians(second.longitude - first.longitude)
    val firstLat = Math.toRadians(first.latitude)
    val secondLat = Math.toRadians(second.latitude)

    val haversine = kotlin.math.sin(latDelta / 2) * kotlin.math.sin(latDelta / 2) +
        kotlin.math.cos(firstLat) * kotlin.math.cos(secondLat) *
        kotlin.math.sin(lngDelta / 2) * kotlin.math.sin(lngDelta / 2)
    val arc = 2 * kotlin.math.atan2(kotlin.math.sqrt(haversine), kotlin.math.sqrt(1 - haversine))
    return earthRadiusMeters * arc
}
