# 🎛️ AllAtOnce — Android Hardware Sensor Dashboard

[![Build & Deploy](https://github.com/RaidGamer1309/AllAtOnce/actions/workflows/build-and-deploy.yml/badge.svg)](https://github.com/RaidGamer1309/AllAtOnce/actions/workflows/build-and-deploy.yml)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%E2%80%9335)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> An all-in-one hardware telemetry suite and diagnostic instrument panel for Android smartphones, exposing real-time readouts from **33 dedicated sensor and hardware modules** across a unified, high-density HUD interface.

---

## 📸 Highlights

- **33 Hardware Modules**: Complete visibility into motion, optics, acoustic, environmental, RF/radio, biometrics, and internal silicon thermal zones.
- **Cyberpunk / Avionics HUD Aesthetic**: High-contrast, dark-mode instrument panel with live telemetry waveforms, calibrated unit displays, and color-coded threshold alerts.
- **Zero Hard Hardware Requirements**: Every hardware sensor uses `<uses-feature android:required="false">`. The app installs and runs gracefully on any Android 8.0+ device, automatically tagging absent chips with `NOT_SUPPORTED` badges.
- **Instant Live Telemetry**: Launches immediately into `LIVE` mode to poll hardware sensors without configuration hurdles. Includes global Pause / Wake / Stop controls.
- **Automated CI/CD**: Cloud pipeline on GitHub Actions builds and validates unit tests, runs Android Lint, and publishes downloadable `.apk` artifacts on every push to `main`.
- **Stitch MCP Design System**: High-precision cybernetic UI specification modeled in Stitch (`projects/15133633290371366176`), ensuring uniform optoelectronic styling across all 33 tiles.

---

## 🎨 Design Specification (Stitch MCP)

AllAtOnce uses the **Cybernetic Telemetry Interface** design specification generated via **Stitch MCP**:
- **Aesthetic**: Deep-space avionics HUD with void-black ground (`#0A0A0C`), hairline vector conduits (`#00E5FF`), warning gold (`#FFB300`), and nominal link indicators (`#00E676`).
- **Typography**: Tabular monospaced numerals for zero-jitter telemetry fluctuations, paired with crisp industrial headers.
- **Stitch Project**: `AllAtOnce Hardware Sensor Dashboard` ([`projects/15133633290371366176`](https://stitch.withgoogle.com))
- **Primary Screen Spec**: `AllAtOnce Telemetry Dashboard` (`screens/9f92183aa07a48d19f292b06f7d3e24c`)

---

## 🧭 Hardware Sensor Modules (33 Total)

| Domain | Sensor / Hardware Module | Source Implementation | Capability Description |
|---|---|---|---|
| **Motion** | **Accelerometer** | [`AccelerometerModule.kt`](app/src/main/java/com/example/allatonce/modules/AccelerometerModule.kt) | 3-axis linear acceleration ($m/s^2$) for orientation and physical motion tracking. |
| **Motion** | **Gyroscope** | [`GyroscopeModule.kt`](app/src/main/java/com/example/allatonce/modules/GyroscopeModule.kt) | Angular velocity and rotational rate ($rad/s$) used for OIS and motion gaming. |
| **Motion** | **Gravity Sensor** | [`GravityModule.kt`](app/src/main/java/com/example/allatonce/modules/GravityModule.kt) | Isolated directional gravity vector isolated from dynamic user acceleration. |
| **Motion** | **Step Counter** | [`StepCounterModule.kt`](app/src/main/java/com/example/allatonce/modules/StepCounterModule.kt) | Hardware pedometer step detector and total boot-cycle step accumulation. |
| **Magnetic** | **Magnetometer** | [`MagnetometerModule.kt`](app/src/main/java/com/example/allatonce/modules/MagnetometerModule.kt) | 3-axis geomagnetic field strength ($\mu T$) and compass heading computation. |
| **Magnetic** | **Hall Effect Sensor** | [`HallEffectModule.kt`](app/src/main/java/com/example/allatonce/modules/HallEffectModule.kt) | Magnetic proximity detection used for folio smart covers and docking accessories. |
| **Atmosphere** | **Barometer** | [`BarometerModule.kt`](app/src/main/java/com/example/allatonce/modules/BarometerModule.kt) | Atmospheric pressure ($hPa$) and hypsometric real-time elevation estimation ($m$). |
| **Atmosphere** | **Ambient Light (ALS)** | [`LightModule.kt`](app/src/main/java/com/example/allatonce/modules/LightModule.kt) | Ambient illuminance ($lx$) and environmental lighting classification. |
| **Atmosphere** | **Thermometer / Humidity** | [`TemperatureHumidityModule.kt`](app/src/main/java/com/example/allatonce/modules/TemperatureHumidityModule.kt) | Ambient external temperature ($^\circ C$) and relative atmospheric humidity ($\%$). |
| **Optics** | **CameraX Preview** | [`CameraPreviewModule.kt`](app/src/main/java/com/example/allatonce/modules/CameraPreviewModule.kt) | High-speed lens sensor viewfinder with rear/front camera switching and FPS stats. |
| **Optics** | **Color Temperature (CCT)** | [`ColorTemperatureModule.kt`](app/src/main/java/com/example/allatonce/modules/ColorTemperatureModule.kt) | Multi-spectral ambient light sensor for white balance and TrueTone screen adaptation. |
| **Optics** | **Flicker Sensor** | [`FlickerSensorModule.kt`](app/src/main/java/com/example/allatonce/modules/FlickerSensorModule.kt) | AC electrical lighting frequency detector ($50\text{ Hz} / 60\text{ Hz}$) for camera anti-banding. |
| **Optics** | **Laser / 3D ToF Sensor** | [`ToFSensorModule.kt`](app/src/main/java/com/example/allatonce/modules/ToFSensorModule.kt) | Time-of-Flight depth ranger used for rapid laser autofocus and spatial sensing. |
| **Optics** | **Flashlight / Torch** | [`FlashlightModule.kt`](app/src/main/java/com/example/allatonce/modules/FlashlightModule.kt) | Camera LED flash controller with toggle capability and status feedback. |
| **Digitizer** | **Capacitive Touch** | [`TouchSensorModule.kt`](app/src/main/java/com/example/allatonce/modules/TouchSensorModule.kt) | Multi-touch digitizer grid report (distinct touch points, stylus support, refresh rate). |
| **Haptics** | **Haptic Feedback Engine** | [`HapticsModule.kt`](app/src/main/java/com/example/allatonce/modules/HapticsModule.kt) | ERM / LRA vibration motor waveform tests (clicks, ticks, double-clicks). |
| **Acoustic** | **MEMS Microphone** | [`MicrophoneModule.kt`](app/src/main/java/com/example/allatonce/modules/MicrophoneModule.kt) | Real-time audio stream decibel analysis ($\text{RMS}$ and $\text{Peak dBFS}$ meter). |
| **Acoustic** | **Audio Speaker** | [`SpeakerModule.kt`](app/src/main/java/com/example/allatonce/modules/SpeakerModule.kt) | Synthesizes pure calibrated test tones ($440\text{ Hz}$ standard pitch test). |
| **Radio** | **GPS / GNSS Location** | [`GpsModule.kt`](app/src/main/java/com/example/allatonce/modules/GpsModule.kt) | Satellite geolocation (latitude, longitude, altitude, bearing, horizontal accuracy). |
| **Radio** | **Wi-Fi Scanner** | [`WifiScanModule.kt`](app/src/main/java/com/example/allatonce/modules/WifiScanModule.kt) | 2.4 GHz / 5 GHz / 6 GHz channel discovery, SSID, RSSI signal strength, and BSSIDs. |
| **Radio** | **Bluetooth LE Scanner** | [`BluetoothScanModule.kt`](app/src/main/java/com/example/allatonce/modules/BluetoothScanModule.kt) | Bluetooth Low Energy beacon discovery, peripheral address, and RSSI tracking. |
| **Radio** | **Near Field Communication** | [`NfcModule.kt`](app/src/main/java/com/example/allatonce/modules/NfcModule.kt) | NFC controller state, NDEF tag reader/writer status, and transceiver readiness. |
| **Radio** | **Cellular Telephony** | [`NetworkModule.kt`](app/src/main/java/com/example/allatonce/modules/NetworkModule.kt) | Carrier operator name, network type (5G / LTE / HSPA), SIM state, and active transport. |
| **Biometrics** | **Biometric / Fingerprint** | [`BiometricModule.kt`](app/src/main/java/com/example/allatonce/modules/BiometricModule.kt) | BiometricManager hardware security enrollment and sensor authenticity verification. |
| **System** | **Internal Thermistors** | [`ThermistorModule.kt`](app/src/main/java/com/example/allatonce/modules/ThermistorModule.kt) | Multi-zone SoC, battery, and charging circuitry thermal sensor monitoring (`/sys/class/thermal/`). |
| **System** | **Battery Health** | [`BatteryModule.kt`](app/src/main/java/com/example/allatonce/modules/BatteryModule.kt) | Battery level, charging speed (voltage, current $\mu A$), battery technology, and health. |
| **System** | **Proximity Sensor** | [`ProximityModule.kt`](app/src/main/java/com/example/allatonce/modules/ProximityModule.kt) | Infrared / ultrasonic face detection sensor detecting near vs. far object states. |
| **System** | **Display & Screen** | [`ScreenInfoModule.kt`](app/src/main/java/com/example/allatonce/modules/ScreenInfoModule.kt) | Display resolution, DPI density, refresh rate ($60\text{--}120+\text{ Hz}$), and HDR capabilities. |
| **System** | **Storage & Memory** | [`StorageModule.kt`](app/src/main/java/com/example/allatonce/modules/StorageModule.kt) | Internal flash storage breakdown (total, available, used space) and RAM overview. |
| **System** | **CPU Wake Lock** | [`WakeLockModule.kt`](app/src/main/java/com/example/allatonce/modules/WakeLockModule.kt) | PowerManager wake lock status and background CPU throttle prevention test. |
| **System** | **Clipboard Manager** | [`ClipboardModule.kt`](app/src/main/java/com/example/allatonce/modules/ClipboardModule.kt) | System clipboard inspection, primary clip MIME type, and buffer monitoring. |
| **System** | **Gamepad & Peripherals** | [`GamepadModule.kt`](app/src/main/java/com/example/allatonce/modules/GamepadModule.kt) | Input device enumeration for external joysticks, controllers, and physical keyboards. |
| **System** | **Hardware Summary** | [`HardwareSummaryModule.kt`](app/src/main/java/com/example/allatonce/modules/HardwareSummaryModule.kt) | High-level diagnostic tally of active, dormant, error, and unsupported hardware modules. |

---

## 🔐 Android Permissions & Hardware Policy

AllAtOnce is built with a **universal hardware tolerance policy**:

1. **Non-Blocking `<uses-feature>` Flags**:
   All 18+ hardware features are declared with `android:required="false"`. If your phone lacks a Barometer or NFC, the Google Play Store and package manager will still permit installation.
2. **Runtime Permission Degradation**:
   Sensors requiring runtime consent (`CAMERA`, `RECORD_AUDIO`, `ACCESS_FINE_LOCATION`, `BLUETOOTH_SCAN`) will display a `PERMISSION_DENIED` state in their HUD tile if not granted, without crashing the app or affecting other sensors.
3. **Privacy First**:
   All telemetry is processed entirely in memory on the device. No sensor readings, audio buffers, camera frames, or location coordinates are uploaded or persisted.

---

## 🛠️ Build & Development

### Prerequisites
- **JDK 17** (Temurin recommended)
- **Android SDK** (API level 35 compile target, API level 26 minimum)

### Quick Commands

```bash
# Clone the repository
git clone https://github.com/RaidGamer1309/AllAtOnce.git
cd AllAtOnce

# Run unit tests
./gradlew testDebugUnitTest

# Run Android Lint verification
./gradlew lint

# Build debug APK
./gradlew assembleDebug
```

The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📲 Download Pre-Built APK

Every commit pushed to the `main` branch automatically triggers our GitHub Actions CI pipeline:

1. Navigate to the **[Actions Tab](https://github.com/RaidGamer1309/AllAtOnce/actions)**.
2. Select the latest workflow run.
3. Scroll down to the **Artifacts** section.
4. Download **`debug-apk`**, extract the ZIP, and install the `.apk` directly on your Android phone.

---

## 🏛️ Architecture & Tech Stack

```
AllAtOnce/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/allatonce/
│   │   │   ├── modules/       # 33 individual sensor & hardware modules
│   │   │   ├── state/         # AppState, PanelMode, and ModuleStatus
│   │   │   ├── theme/         # Cyberpunk/HUD color tokens and typography
│   │   │   ├── ui/
│   │   │   │   ├── components/# Reusable HUD panels, readouts, waveforms
│   │   │   │   └── screens/   # InstrumentPanelScreen (consolidated HUD grid)
│   │   │   └── MainActivity.kt
│   │   └── res/               # Vector icons, theming, application manifest
│   └── src/test/              # Comprehensive JUnit test suites
└── .github/workflows/         # Automated CI/CD (Lint, Unit Tests, APK Build)
```

- **Declarative UI**: Built with 100% **Jetpack Compose** and **Material 3**.
- **Camera Pipeline**: Powered by modern **AndroidX CameraX** (`camera-camera2`, `camera-lifecycle`, `camera-view`).
- **Location & Geodesy**: Uses **Google Play Services Location** for high-precision GNSS triangulation.
- **Biometrics**: Leverages AndroidX **BiometricPrompt** for secure hardware authentication.
- **Coroutines & Asynchronous Streams**: Kotlin Coroutines dispatch non-blocking audio buffers and continuous sensor listeners.

---

## 📄 License

This project is licensed under the **Apache License, Version 2.0**. See the [LICENSE](LICENSE) file for details.
