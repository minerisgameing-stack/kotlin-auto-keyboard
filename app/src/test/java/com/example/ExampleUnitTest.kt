package com.example

import com.example.data.AutomationSettings
import com.example.data.MessageEntity
import com.example.data.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSettingsSecondsCalculation() {
        val settings = AutomationSettings(
            minTime = 10,
            maxTime = 30,
            timeUnit = TimeUnit.SECONDS
        )

        assertEquals(10L, settings.minSeconds)
        assertEquals(30L, settings.maxSeconds)

        // Run multiple random picks and ensure every pick falls strictly within bounds
        repeat(50) {
            val randomDelay = settings.calculateRandomDelaySeconds()
            assertTrue("Delay $randomDelay must be >= 10", randomDelay >= 10L)
            assertTrue("Delay $randomDelay must be <= 30", randomDelay <= 30L)
        }
    }

    @Test
    fun testSettingsMinutesCalculation() {
        val settings = AutomationSettings(
            minTime = 2,
            maxTime = 5,
            timeUnit = TimeUnit.MINUTES
        )

        assertEquals(120L, settings.minSeconds)
        assertEquals(300L, settings.maxSeconds)

        repeat(50) {
            val randomDelay = settings.calculateRandomDelaySeconds()
            assertTrue("Delay $randomDelay must be >= 120", randomDelay >= 120L)
            assertTrue("Delay $randomDelay must be <= 300", randomDelay <= 300L)
        }
    }

    @Test
    fun testMessageEntityDefaults() {
        val entity = MessageEntity(text = "Hello world")
        assertEquals("Hello world", entity.text)
        assertEquals(0, entity.timesSent)
        assertTrue(entity.createdAt > 0)
    }
}
