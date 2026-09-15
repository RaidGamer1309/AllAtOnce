package com.example.allatonce

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.InputDevice
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.allatonce.state.AppState
import com.example.allatonce.theme.AllAtOnceTheme
import com.example.allatonce.theme.PanelBackground
import com.example.allatonce.ui.screens.InstrumentPanelScreen

import java.util.Locale

class MainActivity : ComponentActivity() {

    private val appState = AppState()
    private var lastGamepadEvent = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AllAtOnceTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    color = PanelBackground
                ) {
                    InstrumentPanelScreen(
                        appState = appState,
                        lastGamepadEvent = lastGamepadEvent.value
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume hardware if not explicitly stopped by user
        if (!appState.wasExplicitlyStopped) {
            appState.wake()
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop all systems when app loses focus — manifesto rule #6
        appState.pauseForBackground()
    }

    // Capture gamepad key events
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && (event.source and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            lastGamepadEvent.value = "Key: ${KeyEvent.keyCodeToString(keyCode)}"
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Capture gamepad motion events (joysticks)
    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        if (event != null &&
            (event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
            event.action == MotionEvent.ACTION_MOVE
        ) {
            val x = event.getAxisValue(MotionEvent.AXIS_X)
            val y = event.getAxisValue(MotionEvent.AXIS_Y)
            lastGamepadEvent.value = "Stick: X=${String.format(Locale.US, "%.2f", x)} Y=${String.format(Locale.US, "%.2f", y)}"
            return true
        }
        return super.onGenericMotionEvent(event)
    }
}
