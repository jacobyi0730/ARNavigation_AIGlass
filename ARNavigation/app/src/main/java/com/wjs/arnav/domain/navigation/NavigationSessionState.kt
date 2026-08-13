package com.wjs.arnav.domain.navigation

enum class NavigationPhase {
    IDLE,
    NAVIGATING,
    ARRIVED,
}

enum class NavigationTargetType {
    WAYPOINT,
    DESTINATION,
}

data class NavigationTarget(
    val coordinate: GeoCoordinate,
    val type: NavigationTargetType,
    val order: Int? = null,
)

data class NavigationSessionState(
    val phase: NavigationPhase = NavigationPhase.IDLE,
    val startCoordinate: GeoCoordinate? = null,
    val targets: List<NavigationTarget> = emptyList(),
    val currentTargetIndex: Int = 0,
) {
    val isNavigating: Boolean
        get() = phase == NavigationPhase.NAVIGATING

    fun currentTarget(): NavigationTarget? {
        return targets.getOrNull(currentTargetIndex)
    }

    fun advanceToNextTarget(): NavigationSessionState {
        val nextIndex = currentTargetIndex + 1
        return if (nextIndex >= targets.size) {
            copy(
                phase = NavigationPhase.ARRIVED,
                currentTargetIndex = targets.lastIndex.coerceAtLeast(0),
            )
        } else {
            copy(currentTargetIndex = nextIndex)
        }
    }

    fun reset(): NavigationSessionState = NavigationSessionState()

    companion object {
        fun create(
            startCoordinate: GeoCoordinate,
            waypointCoordinates: List<GeoCoordinate>,
            destinationCoordinate: GeoCoordinate,
        ): NavigationSessionState {
            val targets = buildList {
                waypointCoordinates.forEachIndexed { index, coordinate ->
                    add(
                        NavigationTarget(
                            coordinate = coordinate,
                            type = NavigationTargetType.WAYPOINT,
                            order = index + 1,
                        ),
                    )
                }
                add(
                    NavigationTarget(
                        coordinate = destinationCoordinate,
                        type = NavigationTargetType.DESTINATION,
                    ),
                )
            }
            return NavigationSessionState(
                phase = NavigationPhase.NAVIGATING,
                startCoordinate = startCoordinate,
                targets = targets,
                currentTargetIndex = 0,
            )
        }
    }
}
