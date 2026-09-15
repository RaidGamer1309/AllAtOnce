package com.example.allatonce

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

class SensorMathUnitTest {

    @Test
    fun testVectorMagnitudeCalculation() {
        fun magnitude(x: Float, y: Float, z: Float): Float {
            return sqrt(x * x + y * y + z * z)
        }

        // Standard 1G earth gravity rest on table
        val magGravity = magnitude(0f, 0f, 9.80665f)
        assertEquals(9.80665f, magGravity, 0.001f)

        // Classic 3-4-12 3D Pythagorean triple (3^2 + 4^2 + 12^2 = 9 + 16 + 144 = 169 = 13^2)
        val magTriple = magnitude(3f, 4f, 12f)
        assertEquals(13.0f, magTriple, 0.001f)

        // Zero motion
        val magZero = magnitude(0f, 0f, 0f)
        assertEquals(0.0f, magZero, 0.0001f)
    }

    @Test
    fun testBarometricAltitudeCalculation() {
        // Standard Hypsometric Barometric Formula:
        // altitude = 44330 * (1 - (p / p0)^(1 / 5.255))
        fun calculateAltitude(p: Float, p0: Float = 1013.25f): Float {
            return (44330.0 * (1.0 - (p.toDouble() / p0.toDouble()).pow(1.0 / 5.255))).toFloat()
        }

        // Sea level standard pressure
        val seaLevelAltitude = calculateAltitude(1013.25f)
        assertEquals(0.0f, seaLevelAltitude, 0.05f)

        // High elevation (e.g. 800 hPa ~= 1949 meters)
        val mountainAltitude = calculateAltitude(800.0f)
        assertTrue("Altitude at 800 hPa should be approximately 1949 meters", mountainAltitude in 1940f..1960f)

        // Low elevation / high pressure (e.g. 1025 hPa -> below sea level)
        val belowSeaLevel = calculateAltitude(1025.0f)
        assertTrue("Altitude at 1025 hPa should be below sea level (negative)", belowSeaLevel < 0f)
    }

    @Test
    fun testAudioRmsAndDecibelCalculation() {
        fun calculateDecibels(rms: Double, maxAmplitude: Double = 32768.0): Float {
            return if (rms > 0.0) (20.0 * log10(rms / maxAmplitude)).toFloat() else -90.0f
        }

        // Full scale sine wave / maximum level (RMS = 32768) -> 0 dBFS
        val fullScaleDb = calculateDecibels(32768.0)
        assertEquals(0.0f, fullScaleDb, 0.01f)

        // Half amplitude (RMS = 16384) -> -6.02 dBFS
        val halfScaleDb = calculateDecibels(16384.0)
        assertEquals(-6.0206f, halfScaleDb, 0.01f)

        // Quarter amplitude (RMS = 8192) -> -12.04 dBFS
        val quarterScaleDb = calculateDecibels(8192.0)
        assertEquals(-12.0412f, quarterScaleDb, 0.01f)

        // Silence (RMS = 0) -> clamped noise floor of -90 dBFS
        val silenceDb = calculateDecibels(0.0)
        assertEquals(-90.0f, silenceDb, 0.01f)
    }

    @Test
    fun testThermalZoneTemperatureNormalization() {
        fun normalizeThermalZoneTemp(rawTemp: Float): Float? {
            // Thermal zone temps in sysfs are commonly provided in millidegrees (e.g. 43500)
            val tempC = if (rawTemp > 1000f) rawTemp / 1000f else rawTemp
            // Sanity range check for hardware sensors
            return if (tempC in -40f..150f) tempC else null
        }

        // Typical SoC millidegree reading (42,000 mC = 42.0 C)
        assertEquals(42.0f, normalizeThermalZoneTemp(42000f)!!, 0.01f)

        // Direct Celsius sensor (37.5 C)
        assertEquals(37.5f, normalizeThermalZoneTemp(37.5f)!!, 0.01f)

        // Extreme heat under throttle (85,500 mC = 85.5 C)
        assertEquals(85.5f, normalizeThermalZoneTemp(85500f)!!, 0.01f)

        // Unrealistic corrupt readings should be filtered out
        assertNull(normalizeThermalZoneTemp(99999999f))
        assertNull(normalizeThermalZoneTemp(-500f))
    }

    @Test
    fun testAmbientLightLevelClassification() {
        fun classifyLight(lux: Float): String {
            return when {
                lux < 1f -> "Pitch Black"
                lux < 50f -> "Dim Interior"
                lux < 500f -> "Normal Indoor"
                lux < 2000f -> "Bright Indoor"
                lux < 20000f -> "Daylight / Overcast"
                else -> "Direct Sunlight"
            }
        }

        assertEquals("Pitch Black", classifyLight(0.2f))
        assertEquals("Dim Interior", classifyLight(25f))
        assertEquals("Normal Indoor", classifyLight(350f))
        assertEquals("Bright Indoor", classifyLight(1200f))
        assertEquals("Daylight / Overcast", classifyLight(8000f))
        assertEquals("Direct Sunlight", classifyLight(50000f))
    }

    @Test
    fun testCorrelatedColorTemperatureMcCamyFormula() {
        // McCamy's formula for CCT approximation from CIE chromaticity coordinates (x, y)
        // n = (x - 0.3320) / (0.1858 - y)
        // CCT = 449 * n^3 + 3525 * n^2 + 6823.3 * n + 5520.33
        fun calculateCCT(x: Double, y: Double): Double {
            val n = (x - 0.3320) / (0.1858 - y)
            return 449.0 * n.pow(3) + 3525.0 * n.pow(2) + 6823.3 * n + 5520.33
        }

        // Standard D65 daylight (x ~= 0.3127, y ~= 0.3290) should yield ~6500 K
        val cctD65 = calculateCCT(0.3127, 0.3290)
        assertTrue("D65 daylight CCT should be approximately 6500K, got $cctD65", cctD65 in 6400.0..6600.0)
    }
}
