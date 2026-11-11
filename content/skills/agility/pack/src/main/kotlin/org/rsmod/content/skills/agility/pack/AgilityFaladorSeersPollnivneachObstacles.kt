package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBTableBuilder

private const val FALADOR = "dbrow.agility_course_falador"
private const val SEERS = "dbrow.agility_course_seers"
private const val POLLNIVNEACH = "dbrow.agility_course_pollnivneach"

/*
 * UNVERIFIED: no capture shows a fall. The success curves are fitted to the wiki's levels where
 * failing stops (Falador hand holds 66, Seers' 79, Pollnivneach's market stall 81, which also
 * matches its 89.8% at 70); fall tiles and the Seers' damage come from another implementation.
 */
private val SEERS_FALL = FallDamage(min = 2, max = 6)

internal fun DBTableBuilder.faladorObstacles() {
    row("dbrow.agility_falador_rough_wall") {
        obstacle(FALADOR, step = 0, xp = 110, "loc.rooftops_falador_wallclimb")
        lanes(lane(tile(3036, 3341, 0)) { climbWall(tile(3036, 3342, 0), tile(3036, 3342, 3)) })
    }

    row("dbrow.agility_falador_tightrope_1") {
        obstacle(FALADOR, step = 1, xp = 220, "loc.rooftops_falador_tightrope_1")
        lanes(
            lane(tile(3039, 3343, 3)) {
                tightrope(
                    on = tile(3040, 3343, 3),
                    last = tile(3047, 3343, 3),
                    off = tile(3047, 3344, 3),
                    loops = 5,
                    seq = LOG_BALANCE,
                )
            }
        )
    }

    row("dbrow.agility_falador_hand_holds") {
        obstacle(FALADOR, step = 2, xp = 610, "loc.rooftops_falador_handholds_start")
        lanes(
            lane(tile(3050, 3349, 3)) {
                exactMove(tile(3050, 3351, 3), delay1 = 25, delay2 = 30, angle = FACE_NORTH)
                anim(WALL_JUMP, delay = 20)
                sound(JUMP_UP_SYNTH, delay = 20)
                next()
                telejump(tile(3050, 3351, 2))
                anim(HANDHOLDS)
                sound(HANDHOLDS_SYNTH, delay = 35)
                next()
                exactMove(
                    tile(3051, 3352, 2),
                    delay1 = 13,
                    delay2 = 22,
                    angle = FACE_WEST,
                    to = tile(3050, 3352, 2),
                )
                for (z in 3353..3355) {
                    next()
                    exactMove(tile(3051, z, 2), delay1 = 34, delay2 = 52, angle = FACE_WEST)
                    anim(HANDHOLDS)
                    sound(HANDHOLDS_SYNTH, delay = 35)
                    next()
                }
                exactMove(tile(3050, 3357, 2), delay1 = 34, delay2 = 60, angle = FACE_WEST)
                anim(HANDHOLDS_LAST)
                sound(HANDHOLDS_SYNTH, delay = 35)
                next(2)
                telejump(tile(3050, 3357, 3))
                land()
            }
        )
        fails(
            165,
            300,
            FallDamage(min = 3, max = 8),
            lane(tile(3050, 3349, 3)) { fall(tile(3052, 3353, 0)) },
        )
    }

    row("dbrow.agility_falador_gap_1") {
        obstacle(FALADOR, step = 3, xp = 270, "loc.rooftops_falador_gap_1")
        lanes(lane(tile(3048, 3358, 3)) { hop(tile(3048, 3361, 3), FACE_NORTH, SPOT_JUMP) })
    }

    row("dbrow.agility_falador_gap_2") {
        obstacle(FALADOR, step = 4, xp = 260, "loc.rooftops_falador_gap_2")
        lanes(lane(tile(3045, 3361, 3)) { hop(tile(3041, 3361, 3), FACE_WEST, SPOT_JUMP) })
    }

    row("dbrow.agility_falador_tightrope_2") {
        obstacle(FALADOR, step = 5, xp = 610, "loc.rooftops_falador_tightrope_2")
        lanes(
            lane(tile(3035, 3361, 3)) {
                step(tile(3034, 3361, 3))
                next()
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 4)
                step(tile(3033, 3361, 3))
                next()
                walkTo(tile(3028, 3356, 3))
                next()
                walkTo(tile(3028, 3354, 3))
                balanceOff()
                land()
            }
        )
    }

    row("dbrow.agility_falador_tightrope_3") {
        obstacle(FALADOR, step = 6, xp = 530, "loc.rooftops_falador_tightrope_3")
        lanes(
            lane(tile(3027, 3353, 3)) {
                step(tile(3026, 3353, 3))
                next()
                exactMove(tile(3021, 3353, 3), delay1 = 0, delay2 = 89, angle = FACE_WEST)
                balance(LOG_BALANCE)
                anim("seq.human_walk_logbalance_loop")
                sound("synth.deathslide_first", delay = 5)
                next(3)
                step(tile(3020, 3353, 3))
                balanceOff()
                resetAnim()
                sound(FALL_LAND_SYNTH)
                land()
            }
        )
    }

    row("dbrow.agility_falador_gap_3") {
        obstacle(FALADOR, step = 7, xp = 300, "loc.rooftops_falador_gap_3")
        lanes(lane(tile(3018, 3353, 3)) { hop(tile(3018, 3349, 3), FACE_SOUTH) })
    }

    row("dbrow.agility_falador_ledge_1") {
        obstacle(FALADOR, step = 8, xp = 140, "loc.rooftops_falador_ledge_1")
        lanes(lane(tile(3016, 3346, 3)) { hop(tile(3014, 3346, 3), FACE_WEST) })
    }

    row("dbrow.agility_falador_ledge_2") {
        obstacle(FALADOR, step = 9, xp = 130, "loc.rooftops_falador_ledge_2")
        lanes(lane(tile(3013, 3344, 3)) { hop(tile(3013, 3342, 3), FACE_SOUTH) })
    }

    row("dbrow.agility_falador_ledge_3") {
        obstacle(
            FALADOR,
            step = 10,
            xp = 130,
            "loc.rooftops_falador_ledge_3a",
            "loc.rooftops_falador_ledge_3b",
        )
        lanes(lane(tile(3013, 3335, 3)) { hop(tile(3013, 3333, 3), FACE_SOUTH) })
    }

    row("dbrow.agility_falador_ledge_4") {
        obstacle(FALADOR, step = 11, xp = 140, "loc.rooftops_falador_ledge_4")
        lanes(lane(tile(3017, 3333, 3)) { hop(tile(3019, 3333, 3), FACE_EAST) })
    }

    row("dbrow.agility_falador_edge") {
        obstacle(FALADOR, step = 12, xp = 2410, "loc.rooftops_falador_edge")
        lanes(
            lane(tile(3024, 3333, 3)) {
                exactMove(tile(3027, 3333, 3), delay1 = 15, delay2 = 30, angle = FACE_EAST)
                anim(HURDLE)
                sound(JUMP_SYNTH)
                next()
                jumpDown(tile(3029, 3333, 0))
                land()
            }
        )
    }
}

internal fun DBTableBuilder.seersObstacles() {
    row("dbrow.agility_seers_wall") {
        obstacle(SEERS, step = 0, xp = 450, "loc.rooftops_seers_wallclimb")
        lanes(
            lane(tile(2729, 3489, 0)) {
                message("You climb up the wall...")
                face(tile(2729, 3492, 0))
                anim("seq.human_climbing", delay = 15)
                next(2)
                message("...jump, and grab hold of the sign!")
                telejump(tile(2729, 3488, 1))
                anim(HANDHOLDS)
                next(2)
                telejump(tile(2729, 3491, 3))
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_seers_gap_1") {
        obstacle(SEERS, step = 1, xp = 200, "loc.rooftops_seers_jump")
        lanes(
            lane(tile(2721, 3494, 3)) {
                face(tile(2718, 3495, 3))
                next()
                jumpDown(tile(2719, 3495, 2))
                next()
                jumpDown(tile(2713, 3494, 2))
                next()
                resetAnim()
                land()
            }
        )
        fails(77, 300, SEERS_FALL, lane(tile(2721, 3494, 3)) { fall(tile(2716, 3494, 0)) })
    }

    row("dbrow.agility_seers_tightrope") {
        obstacle(SEERS, step = 2, xp = 200, "loc.rooftops_seers_tightrope")
        lanes(
            lane(tile(2710, 3490, 2)) {
                step(tile(2710, 3489, 2))
                next()
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 3)
                step(tile(2710, 3487, 2))
                next()
                walkTo(tile(2710, 3484, 2))
                next()
                sound(TIGHTROPE_SYNTH, loops = 2)
                walkTo(tile(2710, 3480, 2))
                balanceOff()
                land()
            }
        )
        fails(77, 300, SEERS_FALL, lane(tile(2710, 3490, 2)) { fall(tile(2711, 3485, 0)) })
    }

    row("dbrow.agility_seers_gap_2") {
        obstacle(SEERS, step = 3, xp = 350, "loc.rooftops_seers_jump_1")
        lanes(
            lane(tile(2710, 3477, 2)) {
                exactMove(tile(2710, 3474, 2), delay1 = 25, delay2 = 30, angle = FACE_SOUTH)
                anim(WALL_JUMP, delay = 20)
                sound(JUMP_UP_SYNTH, delay = 20)
                next()
                telejump(tile(2710, 3474, 3))
                anim(WALL_JUMP_LAND)
                next()
                exactMove(tile(2710, 3472, 3), delay1 = 17, delay2 = 26, angle = FACE_SOUTH)
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_seers_gap_3") {
        obstacle(SEERS, step = 4, xp = 150, "loc.rooftops_seers_jump_2")
        lanes(lane(tile(2702, 3470, 3)) { gap(tile(2702, 3468, 3), tile(2702, 3465, 2)) })
    }

    row("dbrow.agility_seers_edge") {
        obstacle(SEERS, step = 5, xp = 4350, "loc.rooftops_seers_leapdown")
        lanes(lane(tile(2702, 3464, 2)) { gap(tile(2706, 3464, 2), tile(2704, 3464, 0)) })
    }
}

internal fun DBTableBuilder.pollnivneachObstacles() {
    row("dbrow.agility_pollnivneach_basket") {
        obstacle(POLLNIVNEACH, step = 0, xp = 100, "loc.rooftops_pollnivneach_basket")
        lanes(
            lane(tile(3351, 2961, 0)) {
                exactMove(tile(3351, 2962, 1), delay1 = 25, delay2 = 30, angle = FACE_NORTH)
                anim(WALL_JUMP, delay = 20)
                sound(JUMP_UP_SYNTH, delay = 20)
                next()
                anim(WALL_JUMP, delay = 20)
                sound(JUMP_UP_SYNTH, delay = 20)
                next()
                telejump(tile(3351, 2964, 1))
                anim(JUMP_DOWN_LAND)
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_pollnivneach_market_stall") {
        obstacle(POLLNIVNEACH, step = 1, xp = 450, "loc.rooftops_pollnivneach_marketstall")
        apRange(2)
        lanes(
            lane(tile(3349, 2968, 1)) {
                exactMove(tile(3350, 2967, 1), delay1 = 15, delay2 = 29, angle = FACE_NORTH)
                anim(RUN_UP, delay = 15)
                next()
                exactMove(
                    tile(3350, 2971, 2),
                    delay1 = 8,
                    delay2 = 50,
                    angle = FACE_NORTH,
                    to = tile(3350, 2970, 2),
                )
                anim(HURDLE)
                sound(HURDLE_SYNTH)
                next(2)
                exactMove(tile(3352, 2973, 2), delay1 = 25, delay2 = 59, angle = FACE_NORTH)
                anim(WALL_JUMP)
                sound(JUMP_UP_SYNTH)
                next(2)
                telejump(tile(3352, 2973, 1))
                anim(JUMP_DOWN_LAND)
                sound(FALL_LAND_SYNTH)
                next()
                resetAnim()
                land()
            }
        )
        fails(
            60,
            300,
            FallDamage(min = 2, max = 2, hpDivisor = 17),
            lane(tile(3349, 2968, 1)) { fall(tile(3349, 2970, 0)) },
        )
    }

    row("dbrow.agility_pollnivneach_banner") {
        obstacle(POLLNIVNEACH, step = 2, xp = 650, "loc.rooftops_pollnivneach_hangingbanner")
        apRange(3)
        lanes(
            lane(tile(3354, 2976, 1)) {
                face(tile(3359, 2981, 1))
                anim(RUN_UP, delay = 15)
                next()
                exactMove(tile(3357, 2977, 2), delay1 = 35, delay2 = 59, angle = FACE_NORTH)
                anim(WALL_JUMP, delay = 10)
                sound(JUMP_UP_SYNTH, delay = 10)
                next(2)
                exactMove(tile(3360, 2977, 2), delay1 = 34, delay2 = 59, angle = FACE_NORTH)
                anim(HANDHOLDS)
                sound(HANDHOLDS_SYNTH, delay = 35)
                next(2)
                telejump(tile(3360, 2977, 1))
                anim(JUMP_DOWN_LAND)
                sound(FALL_LAND_SYNTH)
                next()
                face(tile(3361, 2978, 1))
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_pollnivneach_gap") {
        obstacle(POLLNIVNEACH, step = 3, xp = 350, "loc.rooftops_pollnivneach_gap")
        apRange(2)
        lanes(
            lane(tile(3362, 2977, 1)) {
                exactMove(tile(3365, 2976, 2), delay1 = 40, delay2 = 49, angle = FACE_EAST)
                anim(WALL_JUMP, delay = 15)
                sound(JUMP_UP_SYNTH, delay = 15)
                next(2)
                exactMove(tile(3366, 2976, 1), delay1 = 25, delay2 = 30, angle = FACE_EAST)
                anim(WALL_JUMP_LAND)
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_pollnivneach_tree") {
        obstacle(POLLNIVNEACH, step = 4, xp = 750, "loc.rooftops_pollnivneach_tree")
        apRange(2)
        lanes(
            lane(tile(3368, 2976, 1)) {
                exactMove(tile(3368, 2978, 2), delay1 = 35, delay2 = 49, angle = FACE_NORTH)
                anim(WALL_JUMP, delay = 10)
                sound(JUMP_UP_SYNTH, delay = 10)
                next(2)
                exactMove(tile(3368, 2979, 2), delay1 = 34, delay2 = 52, angle = FACE_EAST)
                anim(HANDHOLDS_REVERSE)
                sound(HANDHOLDS_SYNTH, delay = 35)
                next(2)
                exactMove(tile(3368, 2982, 2), delay1 = 34, delay2 = 52, angle = FACE_EAST)
                anim(HANDHOLDS_REVERSE_LAST)
                sound(HANDHOLDS_SYNTH, delay = 35)
                next(2)
                telejump(tile(3368, 2982, 1))
                anim(JUMP_DOWN_LAND)
                sound(FALL_LAND_SYNTH)
                next()
                face(tile(3368, 2983, 1))
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_pollnivneach_rough_wall") {
        obstacle(POLLNIVNEACH, step = 5, xp = 50, "loc.rooftops_pollnivneach_wallclimb")
        lanes(lane(tile(3365, 2982, 1)) { climbWall(tile(3365, 2983, 1), tile(3365, 2983, 2)) })
    }

    row("dbrow.agility_pollnivneach_monkeybars") {
        obstacle(POLLNIVNEACH, step = 6, xp = 550, "loc.rooftops_pollnivneach_monkeybars_start")
        lanes(
            lane(tile(3358, 2984, 2)) {
                balance("seq.human_monkeybars_walk")
                anim("seq.human_monkeybars_on")
                sound("synth.monkeybars_on")
                sound("synth.monkeybars_loop", loops = 13, delay = 40)
                step(tile(3358, 2985, 2))
                next()
                walkTo(tile(3358, 2991, 2))
                next(2)
                balanceOff()
                face(tile(3358, 2994, 2))
                anim("seq.human_monkeybars_off")
                sound("synth.monkeybars_off")
                next()
                land()
            }
        )
    }

    row("dbrow.agility_pollnivneach_treetop") {
        obstacle(POLLNIVNEACH, step = 7, xp = 600, "loc.rooftops_pollnivneach_treetop")
        apRange(2)
        lanes(
            lane(tile(3360, 2995, 2)) {
                exactMove(tile(3360, 2997, 3), delay1 = 8, delay2 = 50, angle = FACE_NORTH)
                anim(HURDLE)
                sound(HURDLE_SYNTH)
                next(2)
                exactMove(tile(3359, 3000, 3), delay1 = 18, delay2 = 60, angle = FACE_NORTH)
                anim(HURDLE, delay = 10)
                sound(HURDLE_SYNTH, delay = 10)
                next(2)
                telejump(tile(3359, 3000, 2))
                next()
                land()
            }
        )
    }

    row("dbrow.agility_pollnivneach_drying_line") {
        obstacle(POLLNIVNEACH, step = 8, xp = 5400, "loc.rooftops_pollnivneach_line")
        apRange(2)
        lanes(
            lane(tile(3362, 3002, 2)) {
                face(tile(3363, 2997, 2))
                next()
                exactMove(tile(3363, 3000, 3), delay1 = 15, delay2 = 45, angle = FACE_SOUTH)
                anim(SPOT_JUMP)
                sound(JUMP_SYNTH)
                next(2)
                jumpDown(tile(3363, 2998, 0))
                land()
                next()
                resetAnim()
            }
        )
        diaryXp("varbit.desert_diary_hard_complete", 6660)
    }
}
