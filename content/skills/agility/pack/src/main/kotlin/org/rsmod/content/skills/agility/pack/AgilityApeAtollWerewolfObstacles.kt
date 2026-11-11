package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBTableBuilder
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.REQUIREMENT_NINJA_GREEGREE

private const val APE_ATOLL = "dbrow.agility_course_ape_atoll"
private const val WEREWOLF = "dbrow.agility_course_werewolf"

private const val APE_STONE_JUMP = "seq.100_ilm_stepping_stone_jump"
private const val APE_TREE_CLIMB = "seq.100_ilm_climb_tree"
private const val APE_BARS_ON = "seq.100_ilm_monkeybar_jump_up"
private const val APE_BARS_MOVE = "seq.100_ilm_monkeybar_move"
private const val APE_BARS_OFF = "seq.100_ilm_monkeybar_jump_down"
private const val APE_SLOPE = "seq.100_ilm_climb_slope"
private const val APE_VINE_SWING = "seq.100_ilm_vine_swing"
private const val APE_DOWN_VINE = "seq.100_ilm_monkey_down_vine"

private const val PIPE_SQUEEZE = "seq.human_doublepipesqueeze"
private const val CLIMBING = "seq.human_climbing"
private const val ZIP_BITE = "seq.zipline_bite"
private const val ZIP_SLIDE = "seq.zipline_slide"

private const val SQUEEZE_SYNTH = "synth.squeeze_in"
private const val CLIMBING_SYNTH = "synth.climbing_loop"

private val NINJA_GREEGREES =
    arrayOf(
        "obj.mm_monkey_greegree_for_small_ninja_monkey",
        "obj.mm_monkey_greegree_for_medium_ninja_monkey",
        "obj.mm2_kruk_greegree",
    )

private val WEREWOLF_HURDLES =
    listOf(
        "dbrow.agility_werewolf_hurdle_1" to 9893,
        "dbrow.agility_werewolf_hurdle_2" to 9896,
        "dbrow.agility_werewolf_hurdle_3" to 9899,
    )
private val WEREWOLF_PIPE_EXITS =
    listOf(tile(3538, 9910, 0), tile(3541, 9910, 0), tile(3544, 9910, 0))

/*
 * UNVERIFIED: neither course has a capture. Ape Atoll's lanes follow the cache's monkey seqs over
 * the open tiles of its map, Werewolf's the Wilderness stones and the Gnome pipe. Both courses can
 * fail on the wiki, which does not say where a fall lands, so none fail here.
 */
internal fun DBTableBuilder.apeAtollObstacles() {
    row("dbrow.agility_ape_atoll_stepping_stone") {
        obstacle(APE_ATOLL, step = 0, xp = 400, "loc.100_ilm_stepping_stone")
        requirement(REQUIREMENT_NINJA_GREEGREE, *NINJA_GREEGREES)
        lanes(
            lane(tile(2755, 2742, 0)) {
                exactMove(tile(2754, 2742, 0), delay1 = 15, delay2 = 30, angle = FACE_WEST)
                anim(APE_STONE_JUMP)
                sound(JUMP_SYNTH)
                next(2)
                exactMove(tile(2753, 2742, 0), delay1 = 15, delay2 = 30, angle = FACE_WEST)
                anim(APE_STONE_JUMP)
                sound(JUMP_SYNTH)
                next(2)
                land()
            }
        )
    }

    row("dbrow.agility_ape_atoll_tropical_tree") {
        obstacle(APE_ATOLL, step = 1, xp = 400, "loc.100_ilm_climbable_tree")
        requirement(REQUIREMENT_NINJA_GREEGREE, *NINJA_GREEGREES)
        lanes(
            lane(tile(2753, 2742, 0)) {
                face(tile(2753, 2741, 0))
                anim(APE_TREE_CLIMB)
                next(4)
                telejump(tile(2752, 2741, 2))
                land()
            }
        )
    }

    row("dbrow.agility_ape_atoll_monkeybars") {
        obstacle(
            APE_ATOLL,
            step = 2,
            xp = 400,
            "loc.100_ilm_monkeybars_start",
            "loc.100_ilm_monkeybars_end",
        )
        requirement(REQUIREMENT_NINJA_GREEGREE, *NINJA_GREEGREES)
        lanes(
            lane(tile(2752, 2741, 2)) {
                anim(APE_BARS_ON)
                next(2)
                balance(APE_BARS_MOVE)
                for (x in 2751 downTo 2748) {
                    step(tile(x, 2741, 2))
                    next(2)
                }
                balanceOff()
                anim(APE_BARS_OFF)
                next()
                telejump(tile(2747, 2741, 0))
                land()
            }
        )
    }

    row("dbrow.agility_ape_atoll_skull_slope") {
        obstacle(APE_ATOLL, step = 3, xp = 600, "loc.100_ilm_cliff_climb_1")
        requirement(REQUIREMENT_NINJA_GREEGREE, *NINJA_GREEGREES)
        lanes(
            lane(tile(2747, 2741, 0)) {
                exactMove(tile(2743, 2741, 0), delay1 = 0, delay2 = 60, angle = FACE_WEST)
                anim(APE_SLOPE)
                next(2)
                land()
            }
        )
    }

    row("dbrow.agility_ape_atoll_rope") {
        obstacle(APE_ATOLL, step = 4, xp = 1000, "loc.100_ilm_rope_swing")
        requirement(REQUIREMENT_NINJA_GREEGREE, *NINJA_GREEGREES)
        lanes(
            lane(tile(2751, 2731, 0)) {
                exactMove(tile(2756, 2731, 0), delay1 = 30, delay2 = 70, angle = FACE_EAST)
                anim(APE_VINE_SWING)
                next(3)
                land()
            }
        )
    }

    row("dbrow.agility_ape_atoll_tropical_tree_down") {
        obstacle(APE_ATOLL, step = 5, xp = 0, "loc.100_ilm_agility_tree_base")
        requirement(REQUIREMENT_NINJA_GREEGREE, *NINJA_GREEGREES)
        lanes(
            lane(tile(2757, 2733, 0)) {
                face(tile(2757, 2734, 0))
                anim(APE_DOWN_VINE)
                next(2)
                telejump(tile(2755, 2741, 0))
                land()
            }
        )
    }
}

internal fun DBTableBuilder.werewolfObstacles() {
    row("dbrow.agility_werewolf_stepping_stones") {
        obstacle(WEREWOLF, step = 0, xp = 500, "loc.werewolf_steping_stone")
        apRange(2)
        lanes(
            lane(tile(3538, 9873, 0)) {
                val stones =
                    listOf(
                        tile(3538, 9875, 0) to FACE_NORTH,
                        tile(3538, 9877, 0) to FACE_NORTH,
                        tile(3540, 9877, 0) to FACE_EAST,
                        tile(3540, 9879, 0) to FACE_NORTH,
                        tile(3540, 9881, 0) to FACE_NORTH,
                        tile(3540, 9883, 0) to FACE_NORTH,
                    )
                for ((index, stone) in stones.withIndex()) {
                    val (dest, angle) = stone
                    exactMove(dest, delay1 = 15, delay2 = 30, angle = angle)
                    anim(SPOT_JUMP)
                    sound(JUMP_SYNTH)
                    next(if (index == stones.lastIndex) 1 else 2)
                }
                land()
            }
        )
    }

    for ((index, hurdle) in WEREWOLF_HURDLES.withIndex()) {
        val (row, z) = hurdle
        row(row) {
            obstacle(
                WEREWOLF,
                step = 1 + index,
                xp = 200,
                "loc.werewolf_hurdle_mid",
                "loc.werewolf_hurdle_end",
                "loc.werewolf_hurdle_end_mirror",
            )
            lanes(
                *(3537..3542)
                    .map { x -> lane(tile(x, z - 1, 0)) { hop(tile(x, z + 1, 0), FACE_NORTH) } }
                    .toTypedArray()
            )
            if (index == WEREWOLF_HURDLES.lastIndex) {
                landSpawn("obj.waa_stick", WEREWOLF_PIPE_EXITS)
            }
        }
    }

    row("dbrow.agility_werewolf_pipe") {
        obstacle(WEREWOLF, step = 4, xp = 150, "loc.waa_pipe")
        lanes(
            *WEREWOLF_PIPE_EXITS.map { exit -> lane(tile(exit.x, 9904, 0)) { pipe(exit.x) } }
                .toTypedArray()
        )
    }

    row("dbrow.agility_werewolf_skull_slope") {
        obstacle(WEREWOLF, step = 5, xp = 250, "loc.werewolf_skull_climb_1")
        lanes(
            *(9909..9911)
                .map { z ->
                    lane(tile(3533, z, 0)) {
                        step(tile(3532, z, 0))
                        balance(CLIMBING)
                        sound(CLIMBING_SYNTH, loops = 3)
                        next()
                        walkTo(tile(3530, z, 0))
                        next()
                        balanceOff()
                        land()
                    }
                }
                .toTypedArray()
        )
    }

    row("dbrow.agility_werewolf_zip_line") {
        obstacle(
            WEREWOLF,
            step = 6,
            xp = 2000,
            "loc.werewolf_slide_center",
            "loc.werewolf_slide_side",
            "loc.werewolf_slide_side_mirror",
        )
        lanes(
            lane(tile(3528, 9909, 0)) {
                face(tile(3528, 9911, 0))
                anim(ZIP_BITE)
                next(2)
                balance(ZIP_SLIDE)
                exactMove(tile(3528, 9867, 0), delay1 = 0, delay2 = 200, angle = FACE_SOUTH)
                next(7)
                balanceOff()
                land()
            }
        )
    }
}

private fun LaneBuilder.pipe(x: Int) {
    exactMove(tile(x, 9907, 0), delay1 = 30, delay2 = 96, angle = FACE_NORTH)
    anim(PIPE_SQUEEZE, delay = 30)
    next(3)
    sound(SQUEEZE_SYNTH)
    next()
    exactMove(tile(x, 9910, 0), delay1 = 30, delay2 = 96, angle = FACE_NORTH)
    anim(PIPE_SQUEEZE, delay = 30)
    next(3)
    sound(SQUEEZE_SYNTH)
    land()
}
