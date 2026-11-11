package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBTableBuilder

private const val BARBARIAN = "dbrow.agility_course_barbarian"
private const val WILDERNESS = "dbrow.agility_course_wilderness"

private const val ROPE_SWING = "seq.human_ropeswing_long"
private const val ROPE_SWING_MISS = "seq.human_ropeswing_long_miss"
private const val CRUMBLED_WALL = "seq.human_walk_crumbledwall"
private const val PIPE_SQUEEZE = "seq.human_doublepipesqueeze"
private const val CLIMBING = "seq.human_climbing"

private const val SWING_SYNTH = "synth.swing_across"
private const val SQUEEZE_SYNTH = "synth.squeeze_in"
private const val LOG_SYNTH = "synth.log_balance"
private const val CLIMBING_SYNTH = "synth.climbing_loop"

private const val SWING_MESSAGE = "You skillfully swing across."
private const val LOG_MESSAGE = "You walk carefully across the slippery log..."

private val BARBARIAN_WALLS =
    listOf(
        "dbrow.agility_barbarian_wall_1" to 2536,
        "dbrow.agility_barbarian_wall_2" to 2539,
        "dbrow.agility_barbarian_wall_3" to 2542,
    )

/*
 * UNVERIFIED: no Barbarian Outpost capture. Its lanes follow the Wilderness ropeswing and log and
 * the Draynor ledge. The wiki says some of its obstacles fail below 93 without saying which or
 * where the player lands, so none fail here.
 */
internal fun DBTableBuilder.barbarianObstacles() {
    row("dbrow.agility_barbarian_ropeswing") {
        obstacle(BARBARIAN, step = 0, xp = 220, "loc.obstical_ropeswing1")
        lanes(
            lane(tile(2551, 3554, 0)) {
                face(tile(2551, 3552, 0))
                next()
                exactMove(tile(2551, 3549, 0), delay1 = 45, delay2 = 70, angle = FACE_SOUTH)
                anim(ROPE_SWING, delay = 20)
                next()
                sound(SWING_SYNTH)
                message(SWING_MESSAGE)
                land()
            }
        )
    }

    row("dbrow.agility_barbarian_log_balance") {
        obstacle(BARBARIAN, step = 1, xp = 137, "loc.barbarian_log_balance1")
        lanes(
            lane(tile(2551, 3546, 0)) {
                message(LOG_MESSAGE)
                step(tile(2550, 3546, 0))
                balance(LOG_BALANCE)
                sound(LOG_SYNTH, loops = 5)
                next()
                walkTo(tile(2545, 3546, 0))
                sound(LOG_SYNTH, loops = 4)
                next()
                walkTo(tile(2541, 3546, 0))
                next()
                balanceOff()
                message("...You make it safely to the other side.")
                land()
            }
        )
    }

    row("dbrow.agility_barbarian_net") {
        obstacle(BARBARIAN, step = 2, xp = 82, "loc.agility_obstical_net_barbarian")
        lanes(
            *(3545..3547)
                .map { z ->
                    lane(tile(2539, z, 0)) {
                        opMessage("You climb the netting...")
                        anim(CLIMB)
                        next()
                        telejump(tile(2537, z, 1))
                        land()
                    }
                }
                .toTypedArray()
        )
    }

    row("dbrow.agility_barbarian_ledge") {
        obstacle(BARBARIAN, step = 3, xp = 220, "loc.balancing_ledge1")
        lanes(
            lane(tile(2536, 3547, 1)) {
                message("You put your foot on the ledge and try to edge across...")
                anim(INTO_SIDESTEP)
                balance(SIDESTEP)
                step(tile(2535, 3547, 1))
                next()
                sound(LEDGE_SYNTH, loops = 3)
                walkTo(tile(2532, 3547, 1))
                next()
                balanceOff()
                anim(OUT_OF_SIDESTEP)
                message("You skillfully edge across the gap.")
                land()
            }
        )
    }

    row("dbrow.agility_barbarian_ladder") {
        obstacle(BARBARIAN, step = 4, xp = 0, "loc.barbarian_laddertop_norim")
        lanes(
            lane(tile(2532, 3546, 1)) {
                anim(CLIMB)
                next()
                telejump(tile(2532, 3546, 0))
                land()
            }
        )
    }

    for ((index, wall) in BARBARIAN_WALLS.withIndex()) {
        val (row, x) = wall
        row(row) {
            obstacle(BARBARIAN, step = 5 + index, xp = 137, "loc.castlecrumbly1")
            lanes(
                lane(tile(x - 1, 3553, 0)) {
                    exactMove(tile(x + 1, 3553, 0), delay1 = 30, delay2 = 60, angle = FACE_EAST)
                    anim(CRUMBLED_WALL)
                    next(2)
                    land()
                }
            )
        }
    }
}

/*
 * The Wilderness lanes are from a live capture. Its falls are not: the wiki gives where each one
 * lands and the damage, the success curves and the pit tiles below are guesses (UNVERIFIED).
 */
private const val WILDERNESS_FAIL_LOW = 200
private const val WILDERNESS_FAIL_HIGH = 250
private val ROPE_AND_LOG_FALL = FallDamage(min = 1, max = 1, hpShare = 15 to 100)
private val STONES_FALL = FallDamage(min = 1, max = 1, hpShare = 20 to 100)

internal fun DBTableBuilder.wildernessObstacles() {
    row("dbrow.agility_wilderness_pipe") {
        obstacle(WILDERNESS, step = 0, xp = 125, "loc.obstical_pipe2")
        lanes(
            lane(tile(3004, 3937, 0)) {
                exactMove(tile(3004, 3940, 0), delay1 = 30, delay2 = 96, angle = FACE_NORTH)
                anim(PIPE_SQUEEZE, delay = 30)
                next(3)
                sound(SQUEEZE_SYNTH)
                next()
                telejump(tile(3004, 3947, 0))
                next()
                exactMove(tile(3004, 3950, 0), delay1 = 30, delay2 = 96, angle = FACE_NORTH)
                anim(PIPE_SQUEEZE, delay = 30)
                next(3)
                sound(SQUEEZE_SYNTH)
                land()
            }
        )
    }

    row("dbrow.agility_wilderness_ropeswing") {
        obstacle(WILDERNESS, step = 1, xp = 200, "loc.obstical_ropeswing2")
        lanes(
            lane(tile(3005, 3951, 0)) {
                walkOntoRope()
                exactMove(tile(3005, 3958, 0), delay1 = 45, delay2 = 70, angle = FACE_NORTH)
                anim(ROPE_SWING, delay = 20)
                next()
                sound(SWING_SYNTH)
                message(SWING_MESSAGE)
                land()
            }
        )
        fails(
            WILDERNESS_FAIL_LOW,
            WILDERNESS_FAIL_HIGH,
            ROPE_AND_LOG_FALL,
            lane(tile(3005, 3951, 0)) {
                walkOntoRope()
                anim(ROPE_SWING_MISS, delay = 20)
                next()
                message("You slip and fall to the pit below.")
                fall(tile(3005, 10357, 0))
            },
        )
    }

    row("dbrow.agility_wilderness_stepping_stones") {
        obstacle(WILDERNESS, step = 2, xp = 200, "loc.steppingstone1")
        lanes(
            lane(tile(3002, 3960, 0)) {
                message("You carefully start crossing the stepping stones...")
                for (x in 3001 downTo 2996) {
                    stone(x)
                    next(if (x == 2996) 1 else 2)
                }
                message("...You safely cross to the other side.")
                land()
            }
        )
        fails(
            WILDERNESS_FAIL_LOW,
            WILDERNESS_FAIL_HIGH,
            STONES_FALL,
            lane(tile(3002, 3960, 0)) {
                message("You carefully start crossing the stepping stones...")
                stone(3001)
                next(2)
                stone(3000)
                next(2)
                fall(tile(3002, 3960, 0))
            },
        )
    }

    row("dbrow.agility_wilderness_log_balance") {
        obstacle(WILDERNESS, step = 3, xp = 200, "loc.wilderness_log_balance1")
        lanes(
            lane(tile(3002, 3945, 0)) {
                message(LOG_MESSAGE)
                step(tile(3001, 3945, 0))
                balance(LOG_BALANCE)
                sound(LOG_SYNTH, loops = 3)
                next()
                walkTo(tile(2997, 3945, 0))
                sound(LOG_SYNTH, loops = 2)
                next()
                walkTo(tile(2994, 3945, 0))
                balanceOff()
                sound(LOG_SYNTH, loops = 2)
                message("You skillfully edge across the gap.")
                next()
                land()
            }
        )
        fails(
            WILDERNESS_FAIL_LOW,
            WILDERNESS_FAIL_HIGH,
            ROPE_AND_LOG_FALL,
            lane(tile(3002, 3945, 0)) {
                message(LOG_MESSAGE)
                step(tile(3001, 3945, 0))
                balance(LOG_BALANCE)
                next()
                balanceOff()
                fall(tile(3001, 10346, 0))
            },
        )
    }

    row("dbrow.agility_wilderness_rocks") {
        obstacle(WILDERNESS, step = 4, xp = 0, "loc.wildclimbingrock")
        lanes(
            *(2993..2995)
                .map { x ->
                    lane(tile(x, 3937, 0)) {
                        step(tile(x, 3936, 0))
                        balance(CLIMBING)
                        sound(CLIMBING_SYNTH, loops = 7)
                        next()
                        walkTo(tile(x, 3933, 0))
                        balanceOff()
                        next()
                        message("You reach the top.")
                        land()
                    }
                }
                .toTypedArray()
        )
    }
}

private fun LaneBuilder.walkOntoRope() {
    step(tile(3005, 3952, 0))
    next()
    step(tile(3005, 3953, 0))
    next()
    face(tile(3005, 3955, 0))
    next()
}

private fun LaneBuilder.stone(x: Int) {
    exactMove(tile(x, 3960, 0), delay1 = 15, delay2 = 30, angle = FACE_WEST)
    anim(SPOT_JUMP)
    sound(JUMP_SYNTH)
}
