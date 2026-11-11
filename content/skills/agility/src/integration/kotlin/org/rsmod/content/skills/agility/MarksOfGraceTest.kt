package org.rsmod.content.skills.agility

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.obj.plugin.ObjTakePlugin
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.interact.InteractionOp
import org.rsmod.map.CoordGrid
import org.rsmod.routefinder.flag.CollisionFlag

/**
 * Marks of grace (https://oldschool.runescape.wiki/w/Mark_of_Grace): a 2/6 roll per finished lap
 * once three minutes have passed since the last mark, 80% lower at 20 levels over the course, the
 * mark private to its player for ten minutes.
 */
class MarksOfGraceTest {
    /**
     * Runs a Draynor lap, queueing [roll] (out of 30) and [tile] as the spawn index; a `null` roll
     * queues nothing, for laps that must not roll at all.
     */
    private fun GameTestScope.runDraynorLap(roll: Int?, tile: Int = 0) {
        startAt(DRAYNOR_START)
        crossAll(DRAYNOR_LAP.dropLast(1))
        if (roll != null) {
            random.next = roll
            random.then = tile
        }
        cross(DRAYNOR_LAP.last())
    }

    @Test
    fun GameTestState.`lap roll spawns a private mark on a roof`() = runAgilityTest { deps ->
        val other = registerPlayer(CoordGrid(3103, 3262, 0))
        other.observerUUID = other.uuid
        val before = currentMinute()

        runDraynorLap(roll = 0, tile = 1)

        val mark = marksOn(deps, DRAYNOR_MARK_TILES).single()
        assertEquals(DRAYNOR_MARK_TILES[1], mark.coords)
        assertEquals(1, mark.count)
        assertTrue(mark.isVisibleTo(player))
        assertFalse(mark.isVisibleTo(other))
        assertTrue(markReady() in before + 3..currentMinute() + 3)
    }

    @Test
    fun GameTestState.`every mark tile is open floor`() = runAgilityTest { deps ->
        for (tile in GNOME_MARK_TILES + DRAYNOR_MARK_TILES) {
            val flags = deps.collision[tile.x, tile.z, tile.level]
            val blocked =
                CollisionFlag.BLOCK_WALK or CollisionFlag.LOC or CollisionFlag.GROUND_DECOR
            assertTrue(flags and blocked == 0, "$tile is blocked: flags=$flags")
        }
    }

    @Test
    fun GameTestState.`missed roll spawns nothing`() = runAgilityTest { deps ->
        runDraynorLap(roll = 10)

        assertEquals(emptyList<Any>(), marksOn(deps, DRAYNOR_MARK_TILES))
        assertEquals(0, markReady())
    }

    @Test
    fun GameTestState.`no roll until the timer runs out`() = runAgilityTest { deps ->
        setMarkReady(currentMinute() + 1)
        runDraynorLap(roll = null)
        assertEquals(emptyList<Any>(), marksOn(deps, DRAYNOR_MARK_TILES))

        setMarkReady(currentMinute())
        runDraynorLap(roll = 0, tile = 2)
        val tiles = marksOn(deps, DRAYNOR_MARK_TILES).map { it.coords }
        assertEquals(listOf(DRAYNOR_MARK_TILES[2]), tiles)
    }

    @Test
    fun GameTestState.`chance drops twenty levels over the course`() = runAgilityTest { deps ->
        setAgility(21)
        random.next = 2
        startAt(DRAYNOR_START)
        crossAll(DRAYNOR_LAP)
        assertEquals(emptyList<Any>(), marksOn(deps, DRAYNOR_MARK_TILES))

        runDraynorLap(roll = 1)
        assertEquals(1, marksOn(deps, DRAYNOR_MARK_TILES).size)
    }

    @Test
    fun GameTestState.`out of order lap never rolls`() = runAgilityTest { deps ->
        startAt(CoordGrid(3101, 3261, 3))
        random.next = 0
        cross("loc.rooftops_draynor_crate")

        assertEquals(emptyList<Any>(), marksOn(deps, DRAYNOR_MARK_TILES))
    }

    @Test
    fun GameTestState.`marks on one tile stack`() = runAgilityTest { deps ->
        runDraynorLap(roll = 0, tile = 0)
        setMarkReady(0)
        runDraynorLap(roll = 0, tile = 0)

        val mark = marksOn(deps, DRAYNOR_MARK_TILES).single()
        assertEquals(2, mark.count)
    }

    @Test
    fun GameTestState.`mark despawns after ten minutes`() = runAgilityTest { deps ->
        runDraynorLap(roll = 0)

        advance(ticks = 990)
        assertEquals(1, marksOn(deps, DRAYNOR_MARK_TILES).size)
        advance(ticks = 20)
        assertEquals(emptyList<Any>(), marksOn(deps, DRAYNOR_MARK_TILES))
    }

    @Test
    fun GameTestState.`gnome lap spawns marks too`() = runAgilityTest { deps ->
        startAt(GNOME_START)
        crossAll(GNOME_LAP.dropLast(1))
        random.next = 0
        random.then = 0
        cross(GNOME_LAP.last())

        assertEquals(1, marksOn(deps, GNOME_MARK_TILES).size)
    }

    @Test
    fun GameTestState.`only the owner can take the mark`() =
        runAgilityTest(ObjTakePlugin::class) { deps ->
            runDraynorLap(roll = 0, tile = 0)
            val mark = marksOn(deps, DRAYNOR_MARK_TILES).single()
            val other = registerPlayer(mark.coords)
            other.observerUUID = other.uuid

            deps.objInteractions.interact(other, mark, InteractionOp.Op3)
            advance(ticks = 3)
            assertEquals(0, other.inv.count(MARK))
            assertEquals(1, marksOn(deps, DRAYNOR_MARK_TILES).size)

            player.teleport(mark.coords)
            advance()
            deps.objInteractions.interact(player, mark, InteractionOp.Op3)
            advance(ticks = 3)
            assertEquals(1, player.inv.count(MARK))
            assertEquals(emptyList<Any>(), marksOn(deps, DRAYNOR_MARK_TILES))
        }

    private companion object {
        val GNOME_MARK_TILES = listOf(CoordGrid(2484, 3419, 2))
        val DRAYNOR_MARK_TILES =
            listOf(
                CoordGrid(3100, 3279, 3),
                CoordGrid(3089, 3274, 3),
                CoordGrid(3090, 3255, 3),
                CoordGrid(3098, 3259, 3),
            )
    }
}
