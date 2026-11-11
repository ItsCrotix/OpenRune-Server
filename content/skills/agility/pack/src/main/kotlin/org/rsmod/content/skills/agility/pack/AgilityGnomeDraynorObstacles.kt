package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBTableBuilder

private const val GNOME = "dbrow.agility_course_gnome_stronghold"
private const val DRAYNOR = "dbrow.agility_course_draynor"

private const val PIPE_SQUEEZE = "seq.human_doublepipesqueeze"
private const val LOG_SYNTH = "synth.log_balance"
private const val SQUEEZE_SYNTH = "synth.squeeze_in"
private const val NET_MESSAGE = "You climb the netting..."

/** Each net is two tiles wide and lands the player in line with its west tile. */
private fun netX(x: Int): Int = x - (x - 2471) % 2

internal fun DBTableBuilder.gnomeStrongholdObstacles() {
    row("dbrow.agility_gnome_log_balance") {
        obstacle(GNOME, step = 0, xp = 100, "loc.gnome_log_balance1")
        lanes(
            lane(tile(2474, 3436, 0)) {
                message("You walk carefully across the slippery log...")
                balance(LOG_BALANCE)
                sound(LOG_SYNTH, loops = 4)
                step(tile(2474, 3435, 0))
                next()
                walkTo(tile(2474, 3429, 0))
                sound(LOG_SYNTH)
                next()
                balanceOff()
                face(tile(2474, 3429, 0))
                message("...You make it safely to the other side.")
                land()
            }
        )
    }

    row("dbrow.agility_gnome_obstacle_net_1") {
        obstacle(GNOME, step = 1, xp = 100, "loc.obstical_net2")
        lanes(
            *(2471..2476)
                .map { x ->
                    lane(tile(x, 3426, 0)) {
                        opMessage(NET_MESSAGE)
                        anim(CLIMB)
                        next()
                        telejump(tile(netX(x), 3423, 1))
                        land()
                    }
                }
                .toTypedArray()
        )
    }

    row("dbrow.agility_gnome_tree_branch_up") {
        obstacle(GNOME, step = 2, xp = 65, "loc.climbing_branch")
        lanes(
            lane(tile(2473, 3423, 1)) {
                opMessage("You climb the tree...")
                anim(CLIMB)
                next()
                message("...To the platform above.")
                telejump(tile(2473, 3420, 2))
                land()
            }
        )
    }

    row("dbrow.agility_gnome_balancing_rope") {
        obstacle(GNOME, step = 3, xp = 100, "loc.balancing_rope")
        lanes(
            lane(tile(2477, 3420, 2)) {
                message("You carefully cross the tightrope.")
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 5)
                step(tile(2478, 3420, 2))
                next()
                walkTo(tile(2483, 3420, 2))
                next()
                balanceOff()
                land()
            }
        )
    }

    row("dbrow.agility_gnome_tree_branch_down") {
        obstacle(GNOME, step = 4, xp = 65, "loc.climbing_tree", "loc.climbing_tree2")
        lanes(
            *listOf(tile(2485, 3419, 2), tile(2486, 3420, 2))
                .map { start ->
                    lane(start) {
                        opMessage("You climb down the tree...")
                        anim(CLIMB)
                        next()
                        message("You land on the ground.")
                        telejump(tile(2487, 3420, 0))
                        land()
                    }
                }
                .toTypedArray()
        )
    }

    row("dbrow.agility_gnome_obstacle_net_2") {
        obstacle(GNOME, step = 5, xp = 100, "loc.obstical_net3")
        lanes(
            *(2483..2488)
                .map { x ->
                    lane(tile(x, 3425, 0)) {
                        opMessage(NET_MESSAGE)
                        anim(CLIMB)
                        next()
                        step(tile(x - (x - 2483) % 2, 3428, 0))
                        land()
                    }
                }
                .toTypedArray()
        )
    }

    row("dbrow.agility_gnome_obstacle_pipe") {
        obstacle(GNOME, step = 6, xp = 75, "loc.obstical_pipe3_1", "loc.obstical_pipe3_2")
        lanes(
            *listOf(2484, 2487)
                .map { x ->
                    lane(tile(x, 3430, 0)) {
                        exactMove(tile(x, 3433, 0), delay1 = 30, delay2 = 96, angle = FACE_NORTH)
                        anim(PIPE_SQUEEZE, delay = 30)
                        next(3)
                        sound(SQUEEZE_SYNTH)
                        next(3)
                        step(tile(x, 3434, 0))
                        next()
                        exactMove(tile(x, 3437, 0), delay1 = 30, delay2 = 96, angle = FACE_NORTH)
                        anim(PIPE_SQUEEZE, delay = 30)
                        next(3)
                        sound(SQUEEZE_SYNTH)
                        land()
                    }
                }
                .toTypedArray()
        )
    }
}

internal fun DBTableBuilder.draynorObstacles() {
    row("dbrow.agility_draynor_rough_wall") {
        obstacle(DRAYNOR, step = 0, xp = 50, "loc.rooftops_draynor_wallclimb")
        lanes(lane(tile(3103, 3279, 0)) { climbWall(tile(3102, 3279, 0), tile(3102, 3279, 3)) })
    }

    row("dbrow.agility_draynor_tightrope_1") {
        obstacle(DRAYNOR, step = 1, xp = 80, "loc.rooftops_draynor_tightrope_1")
        lanes(
            lane(tile(3099, 3277, 3)) {
                step(tile(3098, 3277, 3))
                next()
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 3)
                walkTo(tile(3094, 3277, 3))
                next()
                sound(TIGHTROPE_SYNTH, loops = 3)
                walkTo(tile(3090, 3277, 3))
                next()
                balanceOff()
                step(tile(3090, 3276, 3))
                land()
            }
        )
    }

    row("dbrow.agility_draynor_tightrope_2") {
        obstacle(DRAYNOR, step = 2, xp = 70, "loc.rooftops_draynor_tightrope_2")
        lanes(
            lane(tile(3091, 3276, 3)) {
                tightrope(
                    on = tile(3092, 3276, 3),
                    last = tile(3092, 3267, 3),
                    off = tile(3092, 3266, 3),
                    loops = 7,
                    seq = LOG_BALANCE,
                )
            }
        )
    }

    row("dbrow.agility_draynor_narrow_wall") {
        obstacle(DRAYNOR, step = 3, xp = 70, "loc.rooftops_draynor_wallcrossing")
        lanes(
            lane(tile(3089, 3265, 3)) {
                anim(INTO_SIDESTEP)
                balance(SIDESTEP)
                step(tile(3089, 3264, 3))
                next()
                sound(LEDGE_SYNTH, loops = 3)
                walkTo(tile(3089, 3262, 3))
                next()
                step(tile(3088, 3261, 3))
                next()
                balanceOff()
                anim(OUT_OF_SIDESTEP)
                land()
            }
        )
    }

    row("dbrow.agility_draynor_wall") {
        obstacle(DRAYNOR, step = 4, xp = 100, "loc.rooftops_draynor_wallscramble")
        lanes(
            lane(tile(3088, 3257, 3)) {
                exactMove(tile(3088, 3256, 3), delay1 = 25, delay2 = 30, angle = FACE_SOUTH)
                anim(WALL_JUMP, delay = 20)
                sound(JUMP_UP_SYNTH, delay = 20)
                next()
                anim(WALL_JUMP_LAND)
                next()
                exactMove(tile(3088, 3255, 3), delay1 = 17, delay2 = 26, angle = FACE_SOUTH)
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_draynor_gap") {
        obstacle(DRAYNOR, step = 5, xp = 40, "loc.rooftops_draynor_leapdown")
        lanes(lane(tile(3094, 3255, 3)) { gap(tile(3097, 3257, 3), tile(3096, 3256, 3)) })
    }

    row("dbrow.agility_draynor_crate") {
        obstacle(DRAYNOR, step = 6, xp = 790, "loc.rooftops_draynor_crate")
        lanes(
            lane(tile(3101, 3261, 3)) {
                jumpDown(tile(3102, 3261, 1))
                next()
                resetAnim()
                next()
                jumpDown(tile(3103, 3261, 0))
                land()
                next()
                resetAnim()
            }
        )
    }
}
