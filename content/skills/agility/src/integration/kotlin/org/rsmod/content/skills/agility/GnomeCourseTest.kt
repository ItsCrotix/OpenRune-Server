package org.rsmod.content.skills.agility

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

/**
 * Gnome Stronghold Agility Course: 10 + 10 + 6.5 + 10 + 6.5 + 10 + 7.5 xp and a 50 xp lap bonus,
 * 110.5 a lap (https://oldschool.runescape.wiki/w/Gnome_Stronghold_Agility_Course).
 */
class GnomeCourseTest {
    @Test
    fun GameTestState.`log balance grants its xp only on landing`() = runAgilityTest {
        startAt(GNOME_START)
        val before = agilityXp()

        player.opLoc1(locNear("loc.gnome_log_balance1", player.coords))
        val first = advanceCollecting(4)
        assertEquals(before, agilityXp())
        val rest = advanceCollecting(6)

        assertEquals(CoordGrid(2474, 3429, 0), player.coords)
        assertEquals(before + fineXp(10.0), agilityXp())
        assertEquals(
            listOf(
                "You walk carefully across the slippery log...",
                "...You make it safely to the other side.",
            ),
            first + rest,
        )
    }

    @Test
    fun GameTestState.`full lap in order pays the lap bonus`() = runAgilityTest {
        startAt(GNOME_START)
        val before = agilityXp()

        val messages = crossAll(GNOME_LAP)

        assertEquals(CoordGrid(2484, 3437, 0), player.coords)
        assertEquals(before + fineXp(60.5) + fineXp(50.0), agilityXp())
        assertEquals(
            listOf(
                "You walk carefully across the slippery log...",
                "...You make it safely to the other side.",
                "You climb the netting...",
                "You climb the tree...",
                "...To the platform above.",
                "You carefully cross the tightrope.",
                "You climb down the tree...",
                "You land on the ground.",
                "You climb the netting...",
                "Your Gnome Stronghold Agility lap count is: <col=ff0000>1</col>.",
            ),
            messages,
        )
    }

    @Test
    fun GameTestState.`east pipe also completes the lap`() = runAgilityTest {
        startAt(GNOME_START)
        val before = agilityXp()

        crossAll(GNOME_LAP.dropLast(1))
        player.teleport(CoordGrid(2487, 3428, 0))
        advance()
        cross("loc.obstical_pipe3_2")

        assertEquals(CoordGrid(2487, 3437, 0), player.coords)
        assertEquals(before + fineXp(60.5) + fineXp(50.0), agilityXp())
    }

    @Test
    fun GameTestState.`second tree branch loc climbs down too`() = runAgilityTest {
        startAt(CoordGrid(2486, 3420, 2))
        val before = agilityXp()

        cross("loc.climbing_tree2")

        assertEquals(CoordGrid(2487, 3420, 0), player.coords)
        assertEquals(before + fineXp(6.5), agilityXp())
    }

    @Test
    fun GameTestState.`skipping obstacles pays no lap bonus`() = runAgilityTest {
        startAt(GNOME_START)
        val before = agilityXp()

        cross("loc.gnome_log_balance1")
        startAt(CoordGrid(2487, 3425, 0))
        cross("loc.obstical_net3")
        cross("loc.obstical_pipe3_2")

        assertEquals(before + fineXp(27.5), agilityXp())
    }

    @Test
    fun GameTestState.`a second lap pays the bonus again`() = runAgilityTest {
        startAt(GNOME_START)
        val before = agilityXp()

        crossAll(GNOME_LAP)
        startAt(GNOME_START)
        crossAll(GNOME_LAP)

        assertEquals(before + 2 * (fineXp(60.5) + fineXp(50.0)), agilityXp())
    }

    @Test
    fun GameTestState.`net 2 refuses from its landing tile`() = runAgilityTest {
        startAt(CoordGrid(2487, 3428, 0))
        val before = agilityXp()

        player.opLoc1(locNear("loc.obstical_net3", player.coords))
        val messages = advanceCollecting(8)

        assertEquals(listOf("You can't do that from here."), messages)
        assertEquals(before, agilityXp())
        assertTrue(player.coords.z >= 3427)
    }

    @Test
    fun GameTestState.`pipe refuses from its far end`() = runAgilityTest {
        startAt(CoordGrid(2484, 3437, 0))

        assertRefused("loc.obstical_pipe3_1")
    }

    @Test
    fun GameTestState.`clicks mid obstacle are ignored`() = runAgilityTest {
        startAt(GNOME_START)
        val before = agilityXp()

        player.opLoc1(locNear("loc.gnome_log_balance1", player.coords))
        advance(ticks = 3)
        player.moveGameClick(CoordGrid(2474, 3440, 0))
        advance()
        player.opLoc1(locNear("loc.obstical_net2", CoordGrid(2474, 3426, 0)))
        advanceUntil({ agilityXp() > before }, timeoutTicks = 20)

        assertEquals(CoordGrid(2474, 3429, 0), player.coords)
        assertEquals(before + fineXp(10.0), agilityXp())
    }
}
