package com.example.allatonce.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class PanelMode {
    DORMANT,
    LIVE,
    STOPPED
}

enum class ModuleStatus {
    DORMANT,
    LIVE,
    PERMISSION_DENIED,
    NOT_SUPPORTED,
    ERROR,
    STOPPED
}

/**
 * Global app state — single source of truth for panel mode.
 * Defaults to LIVE so all hardware wakes immediately upon launch.
 */
class AppState {
    var panelMode by mutableStateOf(PanelMode.LIVE)
        private set

    var wasExplicitlyStopped by mutableStateOf(false)
        private set

    fun wake() {
        wasExplicitlyStopped = false
        panelMode = PanelMode.LIVE
    }

    fun stop() {
        wasExplicitlyStopped = true
        panelMode = PanelMode.STOPPED
    }

    fun pauseForBackground() {
        panelMode = PanelMode.STOPPED
    }
}
