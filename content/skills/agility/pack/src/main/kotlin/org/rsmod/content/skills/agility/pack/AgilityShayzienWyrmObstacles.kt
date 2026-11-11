package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBTableBuilder
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.REQUIREMENT_CROSSBOW_GRAPPLE
import org.rsmod.map.CoordGrid

private const val SHAYZIEN_BASIC = "dbrow.agility_course_shayzien_basic"
private const val SHAYZIEN_ADVANCED = "dbrow.agility_course_shayzien_advanced"
private const val WYRM_BASIC = "dbrow.agility_course_wyrm_basic"
private const val WYRM_ADVANCED = "dbrow.agility_course_wyrm_advanced"

private const val MITHRIL_GRAPPLE = "obj.xbows_grapple_tip_bolt_mithril_rope"

private const val MONKEYBARS_ON = "seq.human_monkeybars_on"
private const val MONKEYBARS_WALK = "seq.human_monkeybars_walk"
private const val MONKEYBARS_OFF = "seq.human_monkeybars_off"
private const val GRAPPLE_SWING = "seq.dorgesh_grapple_swing"
private const val ZIPLINE = "seq.zipline_slide"
private const val WYRM_SHORT_JUMP = "seq.wyrm_agility_shortjump"
private const val WYRM_LONG_JUMP = "seq.wyrm_agility_longjump"
private const val WYRM_ZIP_READY = "seq.wyrm_agility_zipline_ready"
private const val WYRM_ZIP_ON = "seq.wyrm_agility_zipline_on"
private const val WYRM_ZIP_WALK = "seq.wyrm_agility_zipline_walk"
private const val WYRM_ZIP_OFF = "seq.wyrm_agility_zipline_off"

/*
 * UNVERIFIED: neither course has a capture. Tiles follow OpenRune#227, whose author ran both in a
 * client, moved onto open floor where the collision map disagrees (the Shayzien beam's start and
 * landing, the Wyrm zipline's landing), and the seqs are the cache's for each obstacle; timings
 * are guesses. Shayzien's
 * advanced course fails below about 64 on the wiki, which does not say where a fall lands, so it
 * never fails here; the Colossal Wyrm courses never fail on live either.
 */
internal fun DBTableBuilder.shayzienObstacles() {
    row("dbrow.agility_shayzien_basic_ladder") {
        obstacle(SHAYZIEN_BASIC, step = 0, xp = 55, "loc.shayzien_agility_both_start_ladder")
        lanes(shayzienLadder())
    }
    row("dbrow.agility_shayzien_basic_monkeybars") {
        obstacle(SHAYZIEN_BASIC, step = 1, xp = 80, "loc.shayzien_agility_both_rope_climb")
        lanes(shayzienMonkeybars())
    }
    row("dbrow.agility_shayzien_basic_tightrope") {
        obstacle(SHAYZIEN_BASIC, step = 2, xp = 90, "loc.shayzien_agility_both_rope_walk")
        lanes(shayzienTightrope())
    }
    row("dbrow.agility_shayzien_basic_bar") {
        obstacle(SHAYZIEN_BASIC, step = 3, xp = 70, "loc.shayzien_agility_low_bar_climb")
        lanes(
            lane(tile(1523, 3639, 2)) {
                face(tile(1523, 3640, 2))
                anim(CLIMB, delay = 15)
                next(2)
                telejump(tile(1523, 3643, 3))
                resetAnim()
                land()
            }
        )
    }
    row("dbrow.agility_shayzien_basic_tightrope_2") {
        obstacle(SHAYZIEN_BASIC, step = 4, xp = 90, "loc.shayzien_agility_low_rope_walk_1")
        lanes(
            lane(tile(1524, 3644, 3)) {
                tightrope(
                    on = tile(1525, 3644, 3),
                    last = tile(1539, 3644, 3),
                    off = tile(1540, 3644, 2),
                    loops = 8,
                    seq = LOG_BALANCE,
                )
            }
        )
    }
    row("dbrow.agility_shayzien_basic_tightrope_3") {
        obstacle(SHAYZIEN_BASIC, step = 5, xp = 90, "loc.shayzien_agility_low_rope_walk_2")
        lanes(
            lane(tile(1540, 3644, 2)) {
                tightrope(
                    on = tile(1541, 3644, 2),
                    last = tile(1552, 3644, 2),
                    off = tile(1553, 3644, 2),
                    loops = 6,
                    seq = LOG_BALANCE,
                )
            }
        )
    }
    row("dbrow.agility_shayzien_basic_gap") {
        obstacle(SHAYZIEN_BASIC, step = 6, xp = 1060, "loc.shayzien_agility_low_end_jump")
        lanes(lane(tile(1554, 3642, 2)) { gap(tile(1554, 3640, 2), tile(1554, 3637, 0)) })
    }

    row("dbrow.agility_shayzien_advanced_ladder") {
        obstacle(SHAYZIEN_ADVANCED, step = 0, xp = 60, "loc.shayzien_agility_both_start_ladder")
        lanes(shayzienLadder())
    }
    row("dbrow.agility_shayzien_advanced_monkeybars") {
        obstacle(SHAYZIEN_ADVANCED, step = 1, xp = 80, "loc.shayzien_agility_both_rope_climb")
        lanes(shayzienMonkeybars())
    }
    row("dbrow.agility_shayzien_advanced_tightrope") {
        obstacle(SHAYZIEN_ADVANCED, step = 2, xp = 90, "loc.shayzien_agility_both_rope_walk")
        lanes(shayzienTightrope())
    }
    row("dbrow.agility_shayzien_advanced_beam") {
        obstacle(SHAYZIEN_ADVANCED, step = 3, xp = 230, "loc.shayzien_agility_up_swing_jump_1")
        requirement(REQUIREMENT_CROSSBOW_GRAPPLE, MITHRIL_GRAPPLE)
        lanes(lane(tile(1521, 3637, 2)) { grappleSwing(tile(1510, 3635, 2), FACE_WEST) })
    }
    row("dbrow.agility_shayzien_advanced_edge") {
        obstacle(SHAYZIEN_ADVANCED, step = 4, xp = 180, "loc.shayzien_agility_up_jump_platform_1")
        lanes(lane(tile(1510, 3635, 2)) { leap(tile(1510, 3630, 2), FACE_SOUTH) })
    }
    row("dbrow.agility_shayzien_advanced_edge_2") {
        obstacle(SHAYZIEN_ADVANCED, step = 5, xp = 210, "loc.shayzien_agility_up_jump_platform_2")
        lanes(lane(tile(1510, 3630, 2)) { leap(tile(1511, 3622, 2), FACE_SOUTH) })
    }
    row("dbrow.agility_shayzien_advanced_beam_2") {
        obstacle(SHAYZIEN_ADVANCED, step = 6, xp = 230, "loc.shayzien_agility_up_swing_jump_2")
        requirement(REQUIREMENT_CROSSBOW_GRAPPLE, MITHRIL_GRAPPLE)
        lanes(lane(tile(1511, 3622, 2)) { grappleSwing(tile(1519, 3621, 2), FACE_EAST) })
    }
    row("dbrow.agility_shayzien_advanced_zipline") {
        obstacle(SHAYZIEN_ADVANCED, step = 7, xp = 4000, "loc.shayzien_agility_up_end_jump")
        lanes(
            lane(tile(1519, 3621, 2)) {
                face(tile(1522, 3621, 2))
                anim(ZIPLINE)
                next()
                balance(ZIPLINE)
                exactMove(tile(1551, 3630, 2), delay1 = 0, delay2 = 240, angle = FACE_EAST)
                next(8)
                balanceOff()
                telejump(tile(1551, 3630, 0))
                land()
            }
        )
    }
}

internal fun DBTableBuilder.wyrmObstacles() {
    row("dbrow.agility_wyrm_basic_ladder") {
        obstacle(WYRM_BASIC, step = 0, xp = 372, "loc.varlamore_wyrm_agility_start_ladder_trigger")
        lanes(wyrmLadder())
    }
    row("dbrow.agility_wyrm_basic_tightrope") {
        obstacle(WYRM_BASIC, step = 1, xp = 744, "loc.varlamore_wyrm_agility_balance_1_trigger")
        lanes(wyrmFirstTightrope())
    }
    row("dbrow.agility_wyrm_basic_tightrope_2") {
        obstacle(
            WYRM_BASIC,
            step = 2,
            xp = 372,
            "loc.varlamore_wyrm_agility_basic_balance_1_trigger",
        )
        lanes(
            lane(tile(1647, 2910, 1)) {
                tightrope(
                    on = tile(1646, 2910, 1),
                    last = tile(1636, 2910, 1),
                    off = tile(1635, 2910, 1),
                    loops = 6,
                    seq = LOG_BALANCE,
                )
            }
        )
    }
    row("dbrow.agility_wyrm_basic_rope") {
        obstacle(
            WYRM_BASIC,
            step = 3,
            xp = 744,
            "loc.varlamore_wyrm_agility_basic_monkeybars_1_trigger",
        )
        lanes(
            lane(tile(1632, 2910, 1)) {
                step(tile(1631, 2910, 1))
                monkeybars(tile(1627, 2914, 1))
                walkTo(tile(1627, 2918, 1))
                next()
                wyrmJump(tile(1627, 2923, 1), FACE_NORTH, WYRM_LONG_JUMP, ticks = 3)
                walkTo(tile(1627, 2926, 1))
                next()
                wyrmJump(tile(1627, 2931, 1), FACE_NORTH, WYRM_LONG_JUMP, ticks = 3)
                land()
            }
        )
    }
    row("dbrow.agility_wyrm_basic_ladder_2") {
        obstacle(
            WYRM_BASIC,
            step = 4,
            xp = 372,
            "loc.varlamore_wyrm_agility_basic_ladder_1_trigger",
        )
        lanes(
            lane(tile(1627, 2931, 1)) {
                anim(CLIMB)
                next()
                telejump(tile(1625, 2932, 2))
                land()
            }
        )
    }
    row("dbrow.agility_wyrm_basic_zipline") {
        obstacle(WYRM_BASIC, step = 5, xp = 3412, "loc.varlamore_wyrm_agility_end_zipline_trigger")
        lanes(wyrmZipline())
    }

    row("dbrow.agility_wyrm_advanced_ladder") {
        obstacle(
            WYRM_ADVANCED,
            step = 0,
            xp = 372,
            "loc.varlamore_wyrm_agility_start_ladder_trigger",
        )
        lanes(wyrmLadder())
    }
    row("dbrow.agility_wyrm_advanced_tightrope") {
        obstacle(WYRM_ADVANCED, step = 1, xp = 744, "loc.varlamore_wyrm_agility_balance_1_trigger")
        lanes(wyrmFirstTightrope())
    }
    row("dbrow.agility_wyrm_advanced_ladder_2") {
        obstacle(
            WYRM_ADVANCED,
            step = 2,
            xp = 700,
            "loc.varlamore_wyrm_agility_advanced_ladder_1_trigger",
        )
        lanes(
            lane(tile(1648, 2910, 1)) {
                anim(CLIMB)
                next()
                telejump(tile(1648, 2908, 2))
                land()
            }
        )
    }
    row("dbrow.agility_wyrm_advanced_edge") {
        obstacle(
            WYRM_ADVANCED,
            step = 3,
            xp = 700,
            "loc.varlamore_wyrm_agility_advanced_jump_1_trigger",
        )
        lanes(
            lane(tile(1647, 2907, 2)) {
                for (x in listOf(1643, 1639, 1635)) {
                    wyrmJump(tile(x, 2907, 2), FACE_WEST, WYRM_LONG_JUMP, ticks = 3)
                }
                land()
            }
        )
    }
    row("dbrow.agility_wyrm_advanced_tightrope_2") {
        obstacle(
            WYRM_ADVANCED,
            step = 4,
            xp = 1400,
            "loc.varlamore_wyrm_agility_advanced_balance_1_trigger",
        )
        lanes(
            lane(tile(1634, 2908, 2)) {
                step(tile(1633, 2908, 2))
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 8)
                next()
                walkTo(tile(1625, 2916, 2))
                next()
                balanceOff()
                walkTo(tile(1624, 2918, 2))
                next()
                monkeybars(tile(1624, 2931, 2))
                land()
            }
        )
    }
    row("dbrow.agility_wyrm_advanced_zipline") {
        obstacle(
            WYRM_ADVANCED,
            step = 5,
            xp = 6620,
            "loc.varlamore_wyrm_agility_end_zipline_trigger",
        )
        lanes(wyrmZipline())
    }
}

private fun shayzienLadder(): Lane =
    lane(tile(1554, 3630, 0)) {
        anim(CLIMB)
        next()
        telejump(tile(1554, 3632, 3))
        land()
    }

private fun shayzienMonkeybars(): Lane =
    lane(tile(1553, 3633, 3)) {
        monkeybars(tile(1538, 3633, 3))
        telejump(tile(1537, 3633, 2))
        land()
    }

private fun shayzienTightrope(): Lane =
    lane(tile(1537, 3633, 2)) {
        tightrope(
            on = tile(1536, 3633, 2),
            last = tile(1528, 3633, 2),
            off = tile(1527, 3633, 2),
            loops = 5,
            seq = LOG_BALANCE,
        )
    }

private fun wyrmLadder(): Lane =
    lane(tile(1653, 2931, 0)) {
        anim(CLIMB)
        next()
        telejump(tile(1654, 2931, 1))
        land()
    }

/** The first tightrope, then the walk and the run of short jumps the edge after it makes. */
private fun wyrmFirstTightrope(): Lane =
    lane(tile(1655, 2926, 1)) {
        step(tile(1655, 2925, 1))
        balance(LOG_BALANCE)
        sound(TIGHTROPE_SYNTH, loops = 6)
        next()
        walkTo(tile(1655, 2919, 1))
        next()
        balanceOff()
        walkTo(tile(1655, 2916, 1))
        next()
        val jumps =
            listOf(
                tile(1655, 2914, 1) to FACE_SOUTH,
                tile(1653, 2914, 1) to FACE_WEST,
                tile(1653, 2912, 1) to FACE_SOUTH,
                tile(1651, 2912, 1) to FACE_WEST,
                tile(1651, 2910, 1) to FACE_SOUTH,
                tile(1649, 2910, 1) to FACE_WEST,
            )
        for ((dest, angle) in jumps) {
            wyrmJump(dest, angle, WYRM_SHORT_JUMP, ticks = 2)
        }
        land()
    }

private fun wyrmZipline(): Lane =
    lane(tile(1625, 2932, 2)) {
        step(tile(1625, 2933, 2))
        anim(WYRM_ZIP_READY)
        next(2)
        anim(WYRM_ZIP_ON)
        next()
        balance(WYRM_ZIP_WALK)
        exactMove(tile(1642, 2933, 2), delay1 = 0, delay2 = 120, angle = FACE_EAST)
        next(4)
        balanceOff()
        anim(WYRM_ZIP_OFF)
        telejump(tile(1645, 2933, 0))
        land()
    }

/** Swings along a set of monkeybars to [last], one tile a tick. */
private fun LaneBuilder.monkeybars(last: CoordGrid) {
    anim(MONKEYBARS_ON)
    next(2)
    balance(MONKEYBARS_WALK)
    walkTo(last)
    next()
    balanceOff()
    anim(MONKEYBARS_OFF)
    next(2)
}

private fun LaneBuilder.grappleSwing(dest: CoordGrid, angle: Int) {
    anim(GRAPPLE_SWING)
    next(2)
    exactMove(dest, delay1 = 0, delay2 = 90, angle = angle)
    next(4)
    land()
}

private fun LaneBuilder.leap(dest: CoordGrid, angle: Int) {
    exactMove(dest, delay1 = 15, delay2 = 45, angle = angle)
    anim(SPOT_JUMP)
    sound(JUMP_SYNTH)
    next(2)
    land()
}

private fun LaneBuilder.wyrmJump(dest: CoordGrid, angle: Int, seq: String, ticks: Int) {
    exactMove(dest, delay1 = 15, delay2 = 30 * (ticks - 1), angle = angle)
    anim(seq)
    sound(JUMP_SYNTH)
    next(ticks)
}
