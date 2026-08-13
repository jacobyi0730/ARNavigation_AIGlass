package com.wjs.arnav.domain.navigation

data class ArrivalThresholdPolicy(
    val arrivalRadiusMeters: Double = 10.0,
    val dwellTimeMillis: Long = 3_000L,
) {
    init {
        require(arrivalRadiusMeters > 0.0) { "arrivalRadiusMeters must be positive" }
        require(dwellTimeMillis > 0L) { "dwellTimeMillis must be positive" }
    }
}
