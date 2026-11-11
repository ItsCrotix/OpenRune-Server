package org.rsmod.content.skills.agility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.hit.plugin.PlayerHitScript
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.table.agility.AgilityCoursesRow
import org.rsmod.api.table.agility.AgilityObstaclesRow
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.map.CoordGrid
import org.rsmod.routefinder.flag.CollisionFlag

/**
 * The eight later rooftop courses (https://oldschool.runescape.wiki/w/Rooftop_Agility_Courses):
 * a full lap of each pays the wiki's xp and lands where live does, and the rooftop rules that
 * Gnome and Draynor lack: falls, diary xp and the diary mark rules.
 */
class RooftopCoursesTest {
    private class Course(
        val name: String,
        val start: CoordGrid,
        val lap: List<String>,
        val xpTenths: Int,
        val end: CoordGrid,
    )

    /** Runs a lap at 99 Agility, where nothing fails, with marks of grace held back. */
    private fun GameTestScope.runLap(course: Course) {
        setAgility(99)
        blockMarks()
        startAt(course.start)
        advance()
        val before = agilityXp()

        val messages = crossAll(course.lap)

        assertEquals(course.end, player.coords)
        assertEquals(before + course.xpTenths, agilityXp())
        assertEquals("Your ${course.name} lap count is: <col=ff0000>1</col>.", messages.last())
    }

    @Test
    fun GameTestState.`al kharid lap`() = runAgilityTest { runLap(AL_KHARID) }

    @Test
    fun GameTestState.`varrock lap`() = runAgilityTest { runLap(VARROCK) }

    @Test
    fun GameTestState.`canifis lap`() = runAgilityTest { runLap(CANIFIS) }

    @Test
    fun GameTestState.`falador lap`() = runAgilityTest { runLap(FALADOR) }

    @Test
    fun GameTestState.`seers lap`() = runAgilityTest { runLap(SEERS) }

    @Test
    fun GameTestState.`pollnivneach lap`() = runAgilityTest { runLap(POLLNIVNEACH) }

    @Test
    fun GameTestState.`rellekka lap`() = runAgilityTest { runLap(RELLEKKA) }

    @Test
    fun GameTestState.`ardougne lap`() = runAgilityTest { runLap(ARDOUGNE) }

    @Test
    fun GameTestState.`below the course level the obstacle refuses`() = runAgilityTest {
        setAgility(19)
        startAt(AL_KHARID.start)
        advance()

        val messages = assertRefused(AL_KHARID.lap.first())

        assertEquals(listOf("You need an Agility level of 20 to attempt this course."), messages)
    }

    @Test
    fun GameTestState.`a failed tightrope drops the player and hurts`() =
        runAgilityTest(PlayerHitScript::class) {
            setAgility(20)
            setHitpoints(10)
            startAt(CoordGrid(3272, 3182, 3))
            advance()
            val before = agilityXp()
            val hitpoints = player.hitpoints

            random.next = 100
            random.then = 3
            player.opLoc1(locNear("loc.rooftops_kharid_tightrope_1", player.coords))
            advance(ticks = 6)

            assertEquals(CoordGrid(3272, 3177, 0), player.coords)
            assertEquals(before, agilityXp())
            assertEquals(hitpoints - 3, player.hitpoints)
        }

    @Test
    fun GameTestState.`a rellekka fall takes a tenth of the hitpoints left`() =
        runAgilityTest(PlayerHitScript::class) {
            setAgility(80)
            setHitpoints(50)
            startAt(CoordGrid(2622, 3658, 3))
            advance()

            random.next = 100
            random.then = 0
            player.opLoc1(locNear("loc.rooftops_rellekka_tightrope_1", player.coords))
            advance(ticks = 6)

            assertEquals(CoordGrid(2624, 3656, 0), player.coords)
            assertEquals(45, player.hitpoints)
        }

    @Test
    fun GameTestState.`a fall breaks the lap`() = runAgilityTest {
        setAgility(20)
        blockMarks()
        startAt(AL_KHARID.start)
        advance()
        cross(AL_KHARID.lap.first())
        random.next = 100
        random.then = 1
        player.opLoc1(locNear(AL_KHARID.lap[1], player.coords))
        advance(ticks = 6)

        setAgility(99)
        startAt(CoordGrid(3266, 3166, 3))
        val messages = crossAll(AL_KHARID.lap.drop(2))

        assertTrue(messages.none { "lap count" in it })
    }

    @Test
    fun GameTestState.`hard desert diary raises the drying line xp`() = runAgilityTest {
        setAgility(70)
        startAt(CoordGrid(3362, 3002, 2))
        advance()
        val plain = agilityXp()
        cross("loc.rooftops_pollnivneach_line")
        assertEquals(plain + 5400, agilityXp())

        setVarbit("varbit.desert_diary_hard_complete", 1)
        startAt(CoordGrid(3362, 3002, 2))
        advance()
        val diary = agilityXp()
        cross("loc.rooftops_pollnivneach_line")
        assertEquals(diary + 6660, agilityXp())
    }

    /** Runs a lap with the mark rolls queued for the last obstacle. */
    private fun GameTestScope.runLapRolling(course: Course, vararg rolls: Int) {
        setAgility(99)
        startAt(course.start)
        advance()
        crossAll(course.lap.dropLast(1))
        for (roll in rolls) {
            random.next = roll
        }
        cross(course.lap.last())
    }

    @Test
    fun GameTestState.`kandarin diary rerolls a missed seers mark`() = runAgilityTest { deps ->
        runLapRolling(SEERS, 29)
        assertEquals(emptyList<Any>(), marksOn(deps, markTiles(SEERS_ROW)))

        setVarbit("varbit.kandarin_diary_hard_complete", 1)
        setMarkReady(0)
        runLapRolling(SEERS, 29, 14, 0)
        assertEquals(1, marksOn(deps, markTiles(SEERS_ROW)).size)
    }

    @Test
    fun GameTestState.`elite ardougne diary can shorten the timer`() = runAgilityTest { deps ->
        setVarbit("varbit.ardougne_diary_elite_complete", 1)
        val before = currentMinute()

        runLapRolling(ARDOUGNE, 0, 0, 0)

        assertEquals(1, marksOn(deps, markTiles(ARDOUGNE_ROW)).size)
        assertTrue(markReady() in before + 2..currentMinute() + 2)
    }

    @Test
    fun GameTestState.`canifis marks ignore the level penalty`() = runAgilityTest { deps ->
        runLapRolling(CANIFIS, 9, 0)

        assertEquals(1, marksOn(deps, markTiles(CANIFIS_ROW)).size)
    }

    @Test
    fun GameTestState.`every mark and fall tile is open floor`() = runAgilityTest { deps ->
        val tiles =
            AgilityCoursesRow.all().flatMap { it.markTiles } +
                AgilityObstaclesRow.all().flatMap { row ->
                    row.failActions.filter { it.t2 == FALL_TELEJUMP }.map { it.t3 }
                }
        val blocking = CollisionFlag.BLOCK_WALK or CollisionFlag.LOC or CollisionFlag.GROUND_DECOR
        val blocked = tiles.filter { deps.collision[it.x, it.z, it.level] and blocking != 0 }
        assertEquals(emptyList<CoordGrid>(), blocked)
    }

    @Test
    fun GameTestState.`every lane starts and lands on open floor`() = runAgilityTest { deps ->
        val blocking = CollisionFlag.BLOCK_WALK or CollisionFlag.LOC or CollisionFlag.GROUND_DECOR
        val tiles =
            AgilityObstaclesRow.all().flatMap { row ->
                val ends =
                    row.actions
                        .groupBy { it.t0 }
                        .values
                        .map { actions -> actions.last { it.t2 in LANE_MOVES }.t3 }
                row.lanes.map { it.t1 } + ends
            }
        val blocked =
            (tiles.distinct() - CAPTURED_OFF_FLOOR).filter {
                deps.collision[it.x, it.z, it.level] and blocking != 0
            }
        assertEquals(emptyList<CoordGrid>(), blocked)
    }

    private fun GameTestScope.setHitpoints(level: Int) {
        player.statMap.setBaseLevel("stat.hitpoints", level.toByte())
        player.statMap.setCurrentLevel("stat.hitpoints", level.toByte())
    }

    private fun markTiles(row: String): List<CoordGrid> = AgilityCoursesRow.getRow(row).markTiles

    private companion object {
        const val FALL_TELEJUMP = 8
        val LANE_MOVES = setOf(0, 7, 8)

        /** Live puts the player on these tiles although the map flags no floor there. */
        val CAPTURED_OFF_FLOOR = setOf(CoordGrid(2243, 3394, 2))
        const val SEERS_ROW = "dbrow.agility_course_seers"
        const val ARDOUGNE_ROW = "dbrow.agility_course_ardougne"
        const val CANIFIS_ROW = "dbrow.agility_course_canifis"

        val AL_KHARID =
            Course(
                name = "Al Kharid Rooftop",
                start = CoordGrid(3273, 3195, 0),
                lap =
                    listOf(
                        "loc.rooftops_kharid_wallclimb",
                        "loc.rooftops_kharid_tightrope_1",
                        "loc.rooftops_kharid_rope_swing",
                        "loc.rooftops_kharid_slide_side",
                        "loc.rooftops_kharid_bamboo_tree_top",
                        "loc.rooftops_kharid_wallclimb_2",
                        "loc.rooftops_kharid_tightrope_4",
                        "loc.rooftops_kharid_leapdown",
                    ),
                xpTenths = 2160,
                end = CoordGrid(3299, 3194, 0),
            )

        val VARROCK =
            Course(
                name = "Varrock Rooftop",
                start = CoordGrid(3221, 3414, 0),
                lap =
                    listOf(
                        "loc.rooftops_varrock_wallclimb",
                        "loc.rooftops_varrock_clothesline",
                        "loc.rooftops_varrock_leaptoruins",
                        "loc.rooftops_varrock_wallswing",
                        "loc.rooftops_varrock_wallscramble",
                        "loc.rooftops_varrock_leaptobalcony",
                        "loc.rooftops_varrock_leapdown",
                        "loc.rooftops_varrock_stepuproof",
                        "loc.rooftops_varrock_finish",
                    ),
                xpTenths = 2697,
                end = CoordGrid(3236, 3417, 0),
            )

        val CANIFIS =
            Course(
                name = "Canifis Rooftop",
                start = CoordGrid(3508, 3488, 0),
                lap =
                    listOf(
                        "loc.rooftops_canifis_start_tree",
                        "loc.rooftops_canifis_jump",
                        "loc.rooftops_canifis_jump_2",
                        "loc.rooftops_canifis_jump_5",
                        "loc.rooftops_canifis_jump_3",
                        "loc.rooftops_canifis_polevault",
                        "loc.rooftops_canifis_jump_4",
                        "loc.rooftops_canifis_leapdown",
                    ),
                xpTenths = 2400,
                end = CoordGrid(3510, 3485, 0),
            )

        val FALADOR =
            Course(
                name = "Falador Rooftop",
                start = CoordGrid(3036, 3341, 0),
                lap =
                    listOf(
                        "loc.rooftops_falador_wallclimb",
                        "loc.rooftops_falador_tightrope_1",
                        "loc.rooftops_falador_handholds_start",
                        "loc.rooftops_falador_gap_1",
                        "loc.rooftops_falador_gap_2",
                        "loc.rooftops_falador_tightrope_2",
                        "loc.rooftops_falador_tightrope_3",
                        "loc.rooftops_falador_gap_3",
                        "loc.rooftops_falador_ledge_1",
                        "loc.rooftops_falador_ledge_2",
                        "loc.rooftops_falador_ledge_3a",
                        "loc.rooftops_falador_ledge_4",
                        "loc.rooftops_falador_edge",
                    ),
                xpTenths = 5860,
                end = CoordGrid(3029, 3333, 0),
            )

        val SEERS =
            Course(
                name = "Seers' Village Rooftop",
                start = CoordGrid(2729, 3489, 0),
                lap =
                    listOf(
                        "loc.rooftops_seers_wallclimb",
                        "loc.rooftops_seers_jump",
                        "loc.rooftops_seers_tightrope",
                        "loc.rooftops_seers_jump_1",
                        "loc.rooftops_seers_jump_2",
                        "loc.rooftops_seers_leapdown",
                    ),
                xpTenths = 5700,
                end = CoordGrid(2704, 3464, 0),
            )

        val POLLNIVNEACH =
            Course(
                name = "Pollnivneach Rooftop",
                start = CoordGrid(3351, 2961, 0),
                lap =
                    listOf(
                        "loc.rooftops_pollnivneach_basket",
                        "loc.rooftops_pollnivneach_marketstall",
                        "loc.rooftops_pollnivneach_hangingbanner",
                        "loc.rooftops_pollnivneach_gap",
                        "loc.rooftops_pollnivneach_tree",
                        "loc.rooftops_pollnivneach_wallclimb",
                        "loc.rooftops_pollnivneach_monkeybars_start",
                        "loc.rooftops_pollnivneach_treetop",
                        "loc.rooftops_pollnivneach_line",
                    ),
                xpTenths = 8900,
                end = CoordGrid(3363, 2998, 0),
            )

        val RELLEKKA =
            Course(
                name = "Rellekka Rooftop",
                start = CoordGrid(2625, 3677, 0),
                lap =
                    listOf(
                        "loc.rooftops_rellekka_wallclimb",
                        "loc.rooftops_rellekka_gap_1",
                        "loc.rooftops_rellekka_tightrope_1",
                        "loc.rooftops_rellekka_gap_2",
                        "loc.rooftops_rellekka_gap_3",
                        "loc.rooftops_rellekka_tightrope_3",
                        "loc.rooftops_rellekka_dropoff",
                    ),
                xpTenths = 7800,
                end = CoordGrid(2652, 3676, 0),
            )

        val ARDOUGNE =
            Course(
                name = "Ardougne Rooftop",
                start = CoordGrid(2673, 3298, 0),
                lap =
                    listOf(
                        "loc.rooftops_ardy_wallclimb",
                        "loc.rooftops_ardy_jump",
                        "loc.rooftops_ardy_plank",
                        "loc.rooftops_ardy_jump_2",
                        "loc.rooftops_ardy_jump_3",
                        "loc.rooftops_ardy_wallcrossing",
                        "loc.rooftops_ardy_jump_4",
                    ),
                xpTenths = 8890,
                end = CoordGrid(2668, 3297, 0),
            )
    }
}
