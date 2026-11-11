package org.rsmod.content.skills.agility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MarksOfGraceChanceTest {
    @Test
    fun `timer opens on the third minute after a spawn`() {
        assertFalse(MarksOfGrace.isTimerExpired(lastMinute = 100, nowMinute = 100))
        assertFalse(MarksOfGrace.isTimerExpired(lastMinute = 100, nowMinute = 102))
        assertTrue(MarksOfGrace.isTimerExpired(lastMinute = 100, nowMinute = 103))
        assertTrue(MarksOfGrace.isTimerExpired(lastMinute = 0, nowMinute = 100))
    }

    @Test
    fun `base chance is two in six`() {
        val chance = MarksOfGrace.chance(baseLevel = 20, courseLevel = 1)
        assertEquals(2.0 / 6, chance.toDouble() / MarksOfGrace.CHANCE_DENOMINATOR, 1e-9)
    }

    @Test
    fun `chance is eighty percent lower twenty levels over the course`() {
        val base = MarksOfGrace.chance(baseLevel = 1, courseLevel = 1)
        val reduced = MarksOfGrace.chance(baseLevel = 21, courseLevel = 1)
        assertEquals(base * 0.2, reduced.toDouble(), 1e-9)
        assertEquals(base, MarksOfGrace.chance(baseLevel = 20, courseLevel = 1))
    }
}
