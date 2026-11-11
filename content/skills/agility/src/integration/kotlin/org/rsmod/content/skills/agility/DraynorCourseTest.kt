package org.rsmod.content.skills.agility

import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

/**
 * Draynor Village Rooftop Course: 5 + 8 + 7 + 7 + 10 + 4 + 79 = 120 xp a lap with no separate
 * bonus (https://oldschool.runescape.wiki/w/Draynor_Village_Rooftop_Course).
 */
class DraynorCourseTest {
    @Test
    fun GameTestState.`full lap pays 120 xp and ends beside the crate`() = runAgilityTest {
        startAt(DRAYNOR_START)
        setMarkMinute(currentMinute())
        val before = agilityXp()

        crossAll(DRAYNOR_LAP)

        assertEquals(CoordGrid(3103, 3261, 0), player.coords)
        assertEquals(before + fineXp(120.0), agilityXp())
    }

    @Test
    fun GameTestState.`each obstacle lands on its landing tile`() = runAgilityTest {
        startAt(DRAYNOR_START)
        setMarkMinute(currentMinute())
        val landings =
            listOf(
                CoordGrid(3102, 3279, 3),
                CoordGrid(3090, 3276, 3),
                CoordGrid(3092, 3266, 3),
                CoordGrid(3088, 3261, 3),
                CoordGrid(3088, 3255, 3),
                CoordGrid(3096, 3256, 3),
                CoordGrid(3103, 3261, 0),
            )

        val reached =
            DRAYNOR_LAP.map {
                cross(it)
                player.coords
            }

        assertEquals(landings, reached)
    }

    @Test
    fun GameTestState.`wall refuses from its landing tile`() = runAgilityTest {
        startAt(CoordGrid(3088, 3255, 3))

        assertRefused("loc.rooftops_draynor_wallscramble")
    }
}
