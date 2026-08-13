package com.wjs.arnav.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Central dispatcher holder so coroutine-based domain or feature logic can be tested
 * without hardcoding Android/global dispatchers.
 */
data class AppCoroutineDispatchers(
    val main: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val io: CoroutineDispatcher,
)

object DefaultAppCoroutineDispatchers {
    fun create(): AppCoroutineDispatchers {
        return AppCoroutineDispatchers(
            main = Dispatchers.Main,
            default = Dispatchers.Default,
            io = Dispatchers.IO,
        )
    }
}
