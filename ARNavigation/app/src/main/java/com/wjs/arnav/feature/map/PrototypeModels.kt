package com.wjs.arnav.feature.map

import com.google.android.gms.maps.model.LatLng
import com.wjs.arnav.domain.navigation.GeoCoordinate
import com.wjs.arnav.domain.navigation.distanceMeters

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
        distanceMeters(existingPoint.toGeoCoordinate(), tappedPoint.toGeoCoordinate()) < minimumSpacingMeters
    }
}

fun LatLng.toGeoCoordinate(): GeoCoordinate {
    return GeoCoordinate(
        latitude = latitude,
        longitude = longitude,
    )
}
