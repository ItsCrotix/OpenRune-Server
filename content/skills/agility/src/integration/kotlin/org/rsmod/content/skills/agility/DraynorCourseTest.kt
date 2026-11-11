package org.rsmod.content.skills.agility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

/**
 * Draynor Village Rooftop Course: 5 + 8 + 7 + 7 + 10 + 4 + 79 = 120 xp a lap with no separate
 * bonus (https://oldschool.runescape.wiki/w/Draynor_Village_Rooftop_Course). Paths and timings
 * are those of a live capture of the course.
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
    fun GameTestState.`each obstacle lands as many ticks after the click as live`() =
        runAgilityTest {
            setMarkMinute(currentMinute())
            val obstacles =
                listOf(
                    Triple("loc.rooftops_draynor_wallclimb", CoordGrid(3103, 3279, 0), 2),
                    Triple("loc.rooftops_draynor_tightrope_1", CoordGrid(3099, 3277, 3), 9),
                    Triple("loc.rooftops_draynor_tightrope_2", CoordGrid(3091, 3276, 3), 10),
                    Triple("loc.rooftops_draynor_wallcrossing", CoordGrid(3089, 3265, 3), 4),
                    Triple("loc.rooftops_draynor_wallscramble", CoordGrid(3088, 3257, 3), 3),
                    Triple("loc.rooftops_draynor_leapdown", CoordGrid(3094, 3255, 3), 3),
                    Triple("loc.rooftops_draynor_crate", CoordGrid(3101, 3261, 3), 4),
                )

            for ((loc, start, ticks) in obstacles) {
                startAt(start)
                advance()
                val before = agilityXp()
                player.opLoc1(locNear(loc, start))
                var taken = 0
                while (agilityXp() == before && taken < 20) {
                    advance()
                    taken++
                }
                assertEquals(ticks + 1, taken, loc)
            }
        }

    @Test
    fun GameTestState.`tightrope 1 walks the live path`() = runAgilityTest {
        startAt(CoordGrid(3099, 3277, 3))
        advance()
        val before = agilityXp()

        player.opLoc1(locNear("loc.rooftops_draynor_tightrope_1", player.coords))
        val path = mutableListOf<CoordGrid>()
        var balancing = false
        var busy = false
        while (agilityXp() == before && path.size < 20) {
            advance()
            path += player.coords
            balancing = balancing || player.bas != null
            busy = busy || player.vars["varbit.busy"] == 1
        }

        val expected =
            (3098 downTo 3090).map { CoordGrid(it, 3277, 3) } + CoordGrid(3090, 3276, 3)
        assertEquals(expected, path)
        assertTrue(balancing && busy)
        assertNull(player.bas)
        assertEquals(0, player.vars["varbit.busy"])
    }

    @Test
    fun GameTestState.`finished laps count up`() = runAgilityTest {
        setMarkMinute(currentMinute())

        startAt(DRAYNOR_START)
        val first = crossAll(DRAYNOR_LAP)
        startAt(DRAYNOR_START)
        val second = crossAll(DRAYNOR_LAP)

        assertEquals(lapCount(1), first.last())
        assertEquals(lapCount(2), second.last())
        assertEquals(2, player.vars["varp.agility_laps_draynor"])
    }

    @Test
    fun GameTestState.`helper highlights the course for the lap`() = runAgilityTest {
        setMarkMinute(currentMinute())
        startAt(DRAYNOR_START)

        cross(DRAYNOR_LAP.first())
        assertEquals(3, player.vars["varbit.helper_agility_highlighted_course"])

        crossAll(DRAYNOR_LAP.drop(1))
        assertEquals(0, player.vars["varbit.helper_agility_highlighted_course"])
    }

    private fun lapCount(laps: Int): String =
        "Your Draynor Village Rooftop lap count is: <col=ff0000>$laps</col>."

    @Test
    fun GameTestState.`wall refuses from its landing tile`() = runAgilityTest {
        startAt(CoordGrid(3088, 3255, 3))

        assertRefused("loc.rooftops_draynor_wallscramble")
    }
}
