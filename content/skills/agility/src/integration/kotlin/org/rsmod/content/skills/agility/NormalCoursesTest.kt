package org.rsmod.content.skills.agility

import dev.openrune.ServerCacheManager
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.hit.plugin.PlayerHitScript
import org.rsmod.api.player.quiver
import org.rsmod.api.player.righthand
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/**
 * The normal agility courses (https://oldschool.runescape.wiki/w/Agility#Courses): a lap of each
 * pays the wiki's xp, and each course's own rules hold.
 */
class NormalCoursesTest {
    private class Course(
        val name: String,
        val start: CoordGrid,
        val lap: List<String>,
        val xpTenths: Int,
        val level: Int = 99,
    )

    /** Runs a lap with marks of grace held back; returns the messages. */
    private fun GameTestScope.runLap(course: Course): List<String> {
        setAgility(course.level)
        blockMarks()
        startAt(course.start)
        advance()
        val before = agilityXp()

        val messages = crossAll(course.lap)

        assertEquals(before + course.xpTenths, agilityXp())
        assertEquals("Your ${course.name} lap count is: <col=ff0000>1</col>.", messages.last())
        return messages
    }

    @Test
    fun GameTestState.`barbarian outpost lap pays strength too`() = runAgilityTest {
        val strength = player.statMap.getFineXP("stat.strength")
        runLap(BARBARIAN)
        assertEquals(CoordGrid(2543, 3553, 0), player.coords)
        assertEquals(strength + 413, player.statMap.getFineXP("stat.strength"))
    }

    @Test
    fun GameTestState.`wilderness lap`() = runAgilityTest { runLap(WILDERNESS) }

    @Test
    fun GameTestState.`wilderness rocks pay nothing off a lap`() = runAgilityTest {
        setAgility(99)
        startAt(CoordGrid(2994, 3938, 0))
        advance()
        val before = agilityXp()

        cross("loc.wildclimbingrock")

        assertEquals(3933, player.coords.z)
        assertEquals(before, agilityXp())
    }

    @Test
    fun GameTestState.`a wilderness ropeswing fall drops into the pit`() =
        runAgilityTest(PlayerHitScript::class) {
            setAgility(99)
            setHitpoints(40)
            startAt(CoordGrid(3005, 3951, 0))
            advance()

            random.next = 100
            random.then = 1
            player.opLoc1(locNear("loc.obstical_ropeswing2", player.coords))
            advance(ticks = 8)

            assertEquals(CoordGrid(3005, 10357, 0), player.coords)
            assertEquals(40 - (40 * 15 / 100 + 1), player.hitpoints)
        }

    @Test
    fun GameTestState.`a failed obstacle can be retried without losing the lap`() =
        runAgilityTest(PlayerHitScript::class) {
            setAgility(99)
            setHitpoints(99)
            blockMarks()
            startAt(WILDERNESS.start)
            advance()
            crossAll(WILDERNESS.lap.take(2))

            random.next = 100
            random.then = 1
            player.opLoc1(locNear("loc.steppingstone1", player.coords))
            advance(ticks = 12)
            assertEquals(CoordGrid(3002, 3960, 0), player.coords)

            random.next = 0
            val messages = crossAll(WILDERNESS.lap.drop(2))

            assertTrue(messages.any { "lap count is" in it })
        }

    @Test
    fun GameTestState.`prifddinas lap`() = runAgilityTest {
        runLap(PRIFDDINAS)
        assertEquals(CoordGrid(3240, 6109, 0), player.coords)
    }

    @Test
    fun GameTestState.`a prifddinas treetop fall lands in the city and the ladder leads back`() =
        runAgilityTest(PlayerHitScript::class) {
            setAgility(80)
            setHitpoints(40)
            startAt(CoordGrid(2255, 3390, 2))
            advance()

            random.next = 100
            random.then = 1
            player.opLoc1(locNear("loc.prif_agility_tightrope1", player.coords))
            advance(ticks = 8)
            assertEquals(CoordGrid(3275, 6146, 0), player.coords)

            random.next = 0
            cross("loc.prif_agility_tree_ladder_short")
            assertEquals(CoordGrid(2243, 3394, 2), player.coords)
        }

    @Test
    fun GameTestState.`ape atoll lap`() = runAgilityTest {
        player.righthand = InvObj(GREEGREE, 1)
        runLap(APE_ATOLL)
    }

    @Test
    fun GameTestState.`ape atoll refuses a player without a ninja greegree`() = runAgilityTest {
        setAgility(99)
        startAt(APE_ATOLL.start)
        advance()

        val messages = refusal(APE_ATOLL.lap.first())

        assertEquals(listOf("Only the stealthiest and most agile monkey can use this!"), messages)
    }

    @Test
    fun GameTestState.`ape atoll refuses a boosted level`() = runAgilityTest {
        player.righthand = InvObj(GREEGREE, 1)
        player.statMap.setBaseLevel("stat.agility", 47.toByte())
        player.statMap.setCurrentLevel("stat.agility", 50.toByte())
        startAt(APE_ATOLL.start)
        advance()

        val messages = refusal(APE_ATOLL.lap.first())

        assertEquals(listOf("Your Agility boost won't help you here."), messages)
    }

    @Test
    fun GameTestState.`werewolf lap drops a stick past the pipes`() = runAgilityTest { deps ->
        runLap(WEREWOLF)
        val stick = "obj.waa_stick".asRSCM(RSCMType.OBJ)
        val sticks = WEREWOLF_PIPE_EXITS.flatMap { deps.objRepo.findAll(it).toList() }
        assertEquals(1, sticks.count { it.type == stick })
    }

    @Test
    fun GameTestState.`the agility trainer takes one stick for its xp`() =
        runAgilityTest(WerewolfStickScript::class) {
        startAt(STICK_TRAINER.translate(0, 1))
        val trainer = spawnNpc(STICK_TRAINER, npcType("npc.werewolf_trainer_stick"))
        player.inv[0] = InvObj(STICK, 1)
        advance()
        val before = agilityXp()

        player.opNpc1(trainer)
        player.opNpc1(trainer)
        advance(ticks = 4)
        player.opNpc1(trainer)
        advance(ticks = 4)

        assertEquals(before + 3800, agilityXp())
        assertEquals(0, player.inv.count(STICK))
    }

    @Test
    fun GameTestState.`the agility trainer pays nothing without a stick`() =
        runAgilityTest(WerewolfStickScript::class) {
        startAt(STICK_TRAINER.translate(0, 1))
        val trainer = spawnNpc(STICK_TRAINER, npcType("npc.werewolf_trainer_stick"))
        advance()
        val before = agilityXp()

        player.opNpc1(trainer)
        advance(ticks = 4)

        assertEquals(before, agilityXp())
    }

    @Test
    fun GameTestState.`shayzien basic lap`() = runAgilityTest {
        runLap(SHAYZIEN_BASIC)
        assertEquals(CoordGrid(1554, 3637, 0), player.coords)
    }

    @Test
    fun GameTestState.`shayzien advanced lap carries the shared start over`() = runAgilityTest {
        player.righthand = InvObj(CROSSBOW, 1)
        player.quiver = InvObj(MITHRIL_GRAPPLE, 1)
        runLap(SHAYZIEN_ADVANCED)
    }

    @Test
    fun GameTestState.`the shayzien beams need a crossbow and mithril grapple`() = runAgilityTest {
        setAgility(99)
        startAt(CoordGrid(1521, 3637, 2))
        advance()

        val messages = refusal("loc.shayzien_agility_up_swing_jump_1")

        val refusal = "You need a crossbow and a mithril grapple equipped to do that."
        assertEquals(listOf(refusal), messages)
    }

    @Test
    fun GameTestState.`colossal wyrm basic lap`() = runAgilityTest { runLap(WYRM_BASIC) }

    @Test
    fun GameTestState.`colossal wyrm advanced lap pays its own zipline`() = runAgilityTest {
        runLap(WYRM_ADVANCED)
    }

    @Test
    fun GameTestState.`penguin lap`() = runAgilityTest {
        setAgility(99)
        blockMarks()
        startAt(PENGUIN_START)
        advance()
        val before = agilityXp()

        val messages =
            cross("loc.peng_agility_crushcourse_stepstone01") +
                PENGUIN_ICICLES.flatMap { crossNear("loc.peng_iciclepillar_clickzone", it) } +
                cross("loc.peng_agility_slippery_glitters01") +
                crossFromGate()

        assertEquals(before + 4850, agilityXp())
        assertEquals("Your Penguin Agility lap count is: <col=ff0000>1</col>.", messages.last())
    }

    /** The map has no walk from the ice to the Penguin gate, an unverified layout; skip it. */
    private fun GameTestScope.crossFromGate(): List<String> {
        startAt(CoordGrid(2653, 4039, 1))
        return cross("loc.peng_agility_fencing_door")
    }

    /** Clicks [loc] and checks the obstacle never starts; returns the messages sent. */
    private fun GameTestScope.refusal(loc: String): List<String> {
        val before = agilityXp()
        player.opLoc1(locNear(loc, player.coords))
        val messages = mutableListOf<String>()
        repeat(10) {
            advance()
            messages += gameMessages()
            assertEquals(0, player.vars[BUSY])
        }
        assertEquals(before, agilityXp())
        return messages
    }

    private fun GameTestScope.setHitpoints(level: Int) {
        player.statMap.setBaseLevel("stat.hitpoints", level.toByte())
        player.statMap.setCurrentLevel("stat.hitpoints", level.toByte())
    }

    private fun npcType(npc: String) =
        checkNotNull(ServerCacheManager.getNpc(npc.asRSCM(RSCMType.NPC)))

    private companion object {
        const val GREEGREE = "obj.mm_monkey_greegree_for_small_ninja_monkey"
        const val CROSSBOW = "obj.xbows_crossbow_bronze"
        const val MITHRIL_GRAPPLE = "obj.xbows_grapple_tip_bolt_mithril_rope"
        const val STICK = "obj.waa_stick"
        val STICK_TRAINER = CoordGrid(3528, 9865, 0)
        val WEREWOLF_PIPE_EXITS =
            listOf(CoordGrid(3538, 9910, 0), CoordGrid(3541, 9910, 0), CoordGrid(3544, 9910, 0))

        val BARBARIAN =
            Course(
                name = "Barbarian Outpost",
                start = CoordGrid(2551, 3555, 0),
                lap =
                    listOf(
                        "loc.obstical_ropeswing1",
                        "loc.barbarian_log_balance1",
                        "loc.agility_obstical_net_barbarian",
                        "loc.balancing_ledge1",
                        "loc.barbarian_laddertop_norim",
                        "loc.castlecrumbly1",
                        "loc.castlecrumbly1",
                        "loc.castlecrumbly1",
                    ),
                xpTenths = 1533,
            )

        val WILDERNESS =
            Course(
                name = "Wilderness Agility",
                start = CoordGrid(3004, 3937, 0),
                lap =
                    listOf(
                        "loc.obstical_pipe2",
                        "loc.obstical_ropeswing2",
                        "loc.steppingstone1",
                        "loc.wilderness_log_balance1",
                        "loc.wildclimbingrock",
                    ),
                xpTenths = 5714,
            )

        val PRIFDDINAS =
            Course(
                name = "Prifddinas Agility Course",
                start = CoordGrid(3253, 6109, 0),
                lap =
                    listOf(
                        "loc.prif_agility_start_ladder",
                        "loc.prif_agility_tightrope_start1",
                        "loc.prif_agility_chimney_jump",
                        "loc.prif_agility_roof_jump",
                        "loc.prif_agility_dark_hole_active",
                        "loc.prif_agility_tree_ladder_long",
                        "loc.prif_agility_rope_bridge1",
                        "loc.prif_agility_tightrope1",
                        "loc.prif_agility_rope_bridge2",
                        "loc.prif_agility_tightrope2",
                        "loc.prif_agility_tightrope3",
                        "loc.prif_agility_dark_hole_end",
                    ),
                xpTenths = 12852,
            )

        val APE_ATOLL =
            Course(
                name = "Ape Atoll",
                start = CoordGrid(2756, 2742, 0),
                lap =
                    listOf(
                        "loc.100_ilm_stepping_stone",
                        "loc.100_ilm_climbable_tree",
                        "loc.100_ilm_monkeybars_start",
                        "loc.100_ilm_cliff_climb_1",
                        "loc.100_ilm_rope_swing",
                        "loc.100_ilm_agility_tree_base",
                    ),
                xpTenths = 5800,
            )

        val WEREWOLF =
            Course(
                name = "Werewolf Agility",
                start = CoordGrid(3538, 9873, 0),
                lap =
                    listOf(
                        "loc.werewolf_steping_stone",
                        "loc.werewolf_hurdle_mid",
                        "loc.werewolf_hurdle_mid",
                        "loc.werewolf_hurdle_mid",
                        "loc.waa_pipe",
                        "loc.werewolf_skull_climb_1",
                        "loc.werewolf_slide_center",
                    ),
                xpTenths = 3500,
            )

        private val SHAYZIEN_START =
            listOf(
                "loc.shayzien_agility_both_start_ladder",
                "loc.shayzien_agility_both_rope_climb",
                "loc.shayzien_agility_both_rope_walk",
            )

        val SHAYZIEN_BASIC =
            Course(
                name = "Shayzien Basic Agility Course",
                start = CoordGrid(1554, 3630, 0),
                lap =
                    SHAYZIEN_START +
                        listOf(
                            "loc.shayzien_agility_low_bar_climb",
                            "loc.shayzien_agility_low_rope_walk_1",
                            "loc.shayzien_agility_low_rope_walk_2",
                            "loc.shayzien_agility_low_end_jump",
                        ),
                xpTenths = 1535,
            )

        /** The shared start pays the basic course's xp: the ladder can't tell the courses apart. */
        val SHAYZIEN_ADVANCED =
            Course(
                name = "Shayzien Advanced Agility Course",
                start = CoordGrid(1554, 3630, 0),
                lap =
                    SHAYZIEN_START +
                        listOf(
                            "loc.shayzien_agility_up_swing_jump_1",
                            "loc.shayzien_agility_up_jump_platform_1",
                            "loc.shayzien_agility_up_jump_platform_2",
                            "loc.shayzien_agility_up_swing_jump_2",
                            "loc.shayzien_agility_up_end_jump",
                        ),
                xpTenths = 5075,
            )

        private val WYRM_START =
            listOf(
                "loc.varlamore_wyrm_agility_start_ladder_trigger",
                "loc.varlamore_wyrm_agility_balance_1_trigger",
            )

        val WYRM_BASIC =
            Course(
                name = "Colossal Wyrm Basic Agility Course",
                start = CoordGrid(1653, 2931, 0),
                lap =
                    WYRM_START +
                        listOf(
                            "loc.varlamore_wyrm_agility_basic_balance_1_trigger",
                            "loc.varlamore_wyrm_agility_basic_monkeybars_1_trigger",
                            "loc.varlamore_wyrm_agility_basic_ladder_1_trigger",
                            "loc.varlamore_wyrm_agility_end_zipline_trigger",
                        ),
                xpTenths = 6016,
            )

        val WYRM_ADVANCED =
            Course(
                name = "Colossal Wyrm Advanced Agility Course",
                start = CoordGrid(1653, 2931, 0),
                lap =
                    WYRM_START +
                        listOf(
                            "loc.varlamore_wyrm_agility_advanced_ladder_1_trigger",
                            "loc.varlamore_wyrm_agility_advanced_jump_1_trigger",
                            "loc.varlamore_wyrm_agility_advanced_balance_1_trigger",
                            "loc.varlamore_wyrm_agility_end_zipline_trigger",
                        ),
                xpTenths = 10536,
            )

        val PENGUIN_START = CoordGrid(2631, 4055, 0)
        val PENGUIN_ICICLES =
            listOf(
                CoordGrid(2644, 4083, 1),
                CoordGrid(2652, 4079, 1),
                CoordGrid(2658, 4082, 1),
                CoordGrid(2662, 4082, 1),
            )
    }
}
