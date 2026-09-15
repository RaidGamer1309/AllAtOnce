package com.example.allatonce

import com.example.allatonce.state.AppState
import com.example.allatonce.state.ModuleStatus
import com.example.allatonce.state.PanelMode
import org.junit.Assert.*
import org.junit.Test

class StateManagementUnitTest {

    @Test
    fun testInitialAppState() {
        val appState = AppState()
        assertEquals("Initial panel mode must default to LIVE", PanelMode.LIVE, appState.panelMode)
        assertFalse("wasExplicitlyStopped must be false initially", appState.wasExplicitlyStopped)
    }

    @Test
    fun testExplicitStop() {
        val appState = AppState()
        appState.stop()
        assertEquals("Panel mode must transition to STOPPED", PanelMode.STOPPED, appState.panelMode)
        assertTrue("wasExplicitlyStopped must be true after stop()", appState.wasExplicitlyStopped)
    }

    @Test
    fun testWakeRestoresLiveMode() {
        val appState = AppState()
        appState.stop()
        assertTrue(appState.wasExplicitlyStopped)

        appState.wake()
        assertEquals("Panel mode must return to LIVE", PanelMode.LIVE, appState.panelMode)
        assertFalse("wasExplicitlyStopped must be reset to false", appState.wasExplicitlyStopped)
    }

    @Test
    fun testPauseForBackgroundDoesNotSetExplicitlyStopped() {
        val appState = AppState()
        appState.pauseForBackground()
        assertEquals("Panel mode must be STOPPED in background", PanelMode.STOPPED, appState.panelMode)
        assertFalse("Background pause must NOT mark wasExplicitlyStopped as true", appState.wasExplicitlyStopped)
    }

    @Test
    fun testModuleStatusEnumCompleteness() {
        val expectedStatuses = setOf(
            "DORMANT",
            "LIVE",
            "PERMISSION_DENIED",
            "NOT_SUPPORTED",
            "ERROR",
            "STOPPED"
        )
        val actualStatuses = ModuleStatus.values().map { it.name }.toSet()
        assertEquals("ModuleStatus enum must contain all 6 lifecycle states", expectedStatuses, actualStatuses)
    }

    @Test
    fun testPanelModeEnumCompleteness() {
        val expectedModes = setOf("DORMANT", "LIVE", "STOPPED")
        val actualModes = PanelMode.values().map { it.name }.toSet()
        assertEquals("PanelMode enum must contain exactly 3 operational modes", expectedModes, actualModes)
    }
}
