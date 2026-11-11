package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBTableBuilder

private const val AL_KHARID = "dbrow.agility_course_al_kharid"
private const val VARROCK = "dbrow.agility_course_varrock"
private const val CANIFIS = "dbrow.agility_course_canifis"

/*
 * Fail curves and fall tiles are not in any capture. UNVERIFIED: Al Kharid and Varrock stop failing
 * at 30 and 40 (another implementation's guess, the wiki gives no level); their fall tiles come
 * from that implementation. Canifis' gap curve is the wiki's (88.28% at 40, certain from 64), its
 * damage a guess.
 */
private val AL_KHARID_FALL = FallDamage(min = 1, max = 5)

internal fun DBTableBuilder.alKharidObstacles() {
    row("dbrow.agility_kharid_rough_wall") {
        obstacle(AL_KHARID, step = 0, xp = 120, "loc.rooftops_kharid_wallclimb")
        lanes(lane(tile(3273, 3195, 0)) { climbWall(tile(3273, 3194, 0), tile(3273, 3192, 3)) })
    }

    row("dbrow.agility_kharid_tightrope_1") {
        obstacle(AL_KHARID, step = 1, xp = 360, "loc.rooftops_kharid_tightrope_1")
        lanes(
            lane(tile(3272, 3182, 3)) {
                tightrope(
                    on = tile(3272, 3181, 3),
                    last = tile(3272, 3173, 3),
                    off = tile(3272, 3172, 3),
                    loops = 6,
                    seq = LOG_BALANCE,
                )
            }
        )
        fails(0, 861, AL_KHARID_FALL, lane(tile(3272, 3182, 3)) { fall(tile(3272, 3177, 0)) })
    }

    row("dbrow.agility_kharid_cable") {
        obstacle(AL_KHARID, step = 2, xp = 480, "loc.rooftops_kharid_rope_swing")
        lanes(
            lane(tile(3266, 3166, 3)) {
                message("You begin an almighty run-up...")
                exactMove(tile(3269, 3166, 3), delay1 = 0, delay2 = 30, angle = FACE_EAST)
                anim(RUN_UP)
                next()
                message("You gained enough momentum to swing to the other side!")
                exactMove(tile(3284, 3166, 3), delay1 = 0, delay2 = 60, angle = FACE_EAST)
                anim("seq.human_ropeswing_long")
                land()
            }
        )
    }

    row("dbrow.agility_kharid_zip_line") {
        obstacle(AL_KHARID, step = 3, xp = 480, "loc.rooftops_kharid_slide_side")
        lanes(
            lane(tile(3301, 3163, 3)) {
                anim(JUMP_DOWN, delay = 10)
                next()
                telejump(tile(3303, 3163, 1))
                anim("seq.zipline_bite")
                sound("synth.bite")
                sound("synth.deathslide_first", delay = 15)
                next()
                anim("seq.zipline_slide")
                for (x in 3304..3313) {
                    exactMove(tile(x, 3163, 1), delay1 = 0, delay2 = 30, angle = FACE_EAST)
                    next()
                }
                exactMove(tile(3314, 3163, 1), delay1 = 0, delay2 = 15, angle = FACE_EAST)
                next()
                step(tile(3315, 3163, 1))
                resetAnim()
                land()
            }
        )
        fails(0, 861, AL_KHARID_FALL, lane(tile(3301, 3163, 3)) { fall(tile(3308, 3163, 0)) })
    }

    row("dbrow.agility_kharid_tropical_tree") {
        obstacle(AL_KHARID, step = 4, xp = 120, "loc.rooftops_kharid_bamboo_tree_top")
        apRange(2)
        lanes(
            lane(tile(3318, 3165, 1)) {
                exactMove(tile(3318, 3169, 1), delay1 = 35, delay2 = 49, angle = FACE_NORTH)
                anim(WALL_JUMP)
                sound(JUMP_UP_SYNTH, delay = 10)
                next(2)
                exactMove(tile(3318, 3170, 1), delay1 = 34, delay2 = 42, angle = FACE_EAST)
                anim(HANDHOLDS_REVERSE)
                sound(HANDHOLDS_SYNTH, delay = 35)
                next(2)
                exactMove(tile(3317, 3174, 1), delay1 = 34, delay2 = 52, angle = FACE_NORTH)
                anim(HANDHOLDS_REVERSE_LAST)
                sound(HANDHOLDS_SYNTH, delay = 35)
                next(2)
                telejump(tile(3317, 3174, 2))
                anim(JUMP_DOWN_LAND)
                sound(FALL_LAND_SYNTH)
                next()
                face(tile(3317, 3176, 2))
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_kharid_roof_top_beams") {
        obstacle(AL_KHARID, step = 5, xp = 60, "loc.rooftops_kharid_wallclimb_2")
        lanes(lane(tile(3316, 3179, 2)) { climbWall(tile(3316, 3180, 2), tile(3316, 3180, 3)) })
    }

    row("dbrow.agility_kharid_tightrope_2") {
        obstacle(AL_KHARID, step = 6, xp = 180, "loc.rooftops_kharid_tightrope_4")
        lanes(
            lane(tile(3313, 3185, 3)) {
                step(tile(3313, 3186, 3))
                next()
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 6)
                walkTo(tile(3303, 3186, 3))
                next(2)
                step(tile(3302, 3186, 3))
                balanceOff()
                land()
                next()
                step(tile(3302, 3187, 3))
            }
        )
    }

    row("dbrow.agility_kharid_gap") {
        obstacle(AL_KHARID, step = 7, xp = 360, "loc.rooftops_kharid_leapdown")
        lanes(
            lane(tile(3300, 3192, 3)) {
                face(tile(3298, 3194, 3))
                anim(JUMP_DOWN)
                sound(JUMP_DOWN_SYNTH, delay = 15)
                next()
                telejump(tile(3299, 3194, 0))
                resetAnim()
                land()
            }
        )
    }
}

internal fun DBTableBuilder.varrockObstacles() {
    row("dbrow.agility_varrock_rough_wall") {
        obstacle(VARROCK, step = 0, xp = 135, "loc.rooftops_varrock_wallclimb")
        lanes(
            lane(tile(3221, 3414, 0)) {
                face(tile(3218, 3414, 0))
                anim(CLIMB, delay = 15)
                next(2)
                telejump(tile(3220, 3414, 3))
                anim(WALL_JUMP_LAND)
                sound(JUMP_UP_SYNTH)
                next()
                exactMove(tile(3219, 3414, 3), delay1 = 17, delay2 = 26, angle = FACE_WEST)
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_varrock_clothes_line") {
        obstacle(VARROCK, step = 1, xp = 230, "loc.rooftops_varrock_clothesline")
        lanes(
            lane(tile(3214, 3414, 3)) {
                exactMove(tile(3212, 3414, 3), delay1 = 15, delay2 = 30, angle = FACE_WEST)
                anim(SPOT_JUMP)
                sound(JUMP_SYNTH)
                next()
                exactMove(tile(3210, 3414, 3), delay1 = 30, delay2 = 45, angle = FACE_WEST)
                anim(SPOT_JUMP, delay = 15)
                sound(JUMP_SYNTH, delay = 15)
                next(2)
                exactMove(tile(3208, 3414, 3), delay1 = 15, delay2 = 30, angle = FACE_WEST)
                anim(SPOT_JUMP)
                sound(JUMP_SYNTH)
                next(2)
                land()
            }
        )
        fails(
            0,
            640,
            FallDamage(min = 3, max = 8),
            lane(tile(3214, 3414, 3)) { fall(tile(3211, 3414, 0)) },
        )
    }

    row("dbrow.agility_varrock_gap_1") {
        obstacle(VARROCK, step = 2, xp = 190, "loc.rooftops_varrock_leaptoruins")
        lanes(lane(tile(3201, 3416, 3)) { gap(tile(3196, 3416, 3), tile(3197, 3416, 1)) })
    }

    row("dbrow.agility_varrock_wall") {
        obstacle(VARROCK, step = 3, xp = 280, "loc.rooftops_varrock_wallswing")
        apRange(3)
        lanes(
            lane(tile(3194, 3416, 1)) {
                exactMove(tile(3193, 3416, 1), delay1 = 15, delay2 = 45, angle = FACE_WEST)
                anim(RUN_UP, delay = 15)
                next()
                exactMove(tile(3190, 3414, 1), delay1 = 25, delay2 = 30, angle = FACE_WEST)
                anim(WALL_JUMP, delay = 20)
                sound(JUMP_UP_SYNTH, delay = 20)
                for (z in 3413 downTo 3409) {
                    next()
                    exactMove(tile(3190, z, 1), delay1 = 34, delay2 = 52, angle = FACE_WEST)
                    anim(if (z == 3409) HANDHOLDS_REVERSE_LAST else HANDHOLDS_REVERSE)
                    sound(HANDHOLDS_SYNTH, delay = 35)
                    next()
                }
                next()
                balance(SIDESTEP)
                anim(INTO_SIDESTEP)
                next()
                step(tile(3190, 3408, 1))
                sound(LEDGE_SYNTH, loops = 2)
                next()
                step(tile(3190, 3407, 1))
                sound(LEDGE_SYNTH, loops = 2)
                next()
                exactMove(tile(3192, 3406, 1), delay1 = 5, delay2 = 30, angle = FACE_EAST)
                balanceOff()
                anim(SPOT_JUMP)
                sound(JUMP_UP_SYNTH)
                next()
                telejump(tile(3192, 3406, 3))
                resetAnim()
                next()
                land()
            }
        )
        fails(
            0,
            640,
            FallDamage(min = 2, max = 5),
            lane(tile(3194, 3416, 1)) { fall(tile(3191, 3412, 0)) },
        )
    }

    row("dbrow.agility_varrock_gap_2") {
        obstacle(VARROCK, step = 4, xp = 100, "loc.rooftops_varrock_wallscramble")
        lanes(
            lane(tile(3193, 3402, 3)) {
                exactMove(tile(3193, 3399, 3), delay1 = 25, delay2 = 30, angle = FACE_SOUTH)
                anim(WALL_JUMP, delay = 20)
                sound(JUMP_UP_SYNTH, delay = 20)
                next()
                anim(WALL_JUMP_LAND)
                next()
                exactMove(tile(3193, 3398, 3), delay1 = 17, delay2 = 26, angle = FACE_SOUTH)
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_varrock_gap_3") {
        obstacle(VARROCK, step = 5, xp = 245, "loc.rooftops_varrock_leaptobalcony")
        lanes(
            lane(tile(3208, 3397, 3)) {
                face(tile(3215, 3399, 3))
                anim(RUN_UP)
                next()
                exactMove(tile(3215, 3399, 3), delay1 = 20, delay2 = 50, angle = FACE_EAST)
                anim("seq.seaslug_ken_leap", delay = 15)
                sound(JUMP_SYNTH, delay = 15)
                next(2)
                exactMove(tile(3217, 3399, 3), delay1 = 5, delay2 = 10, angle = FACE_EAST)
                anim(WALL_JUMP)
                sound(JUMP_UP_SYNTH)
                next()
                exactMove(tile(3218, 3399, 3), delay1 = 18, delay2 = 27, angle = FACE_EAST)
                anim(WALL_JUMP_LAND)
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_varrock_gap_4") {
        obstacle(VARROCK, step = 6, xp = 45, "loc.rooftops_varrock_leapdown")
        lanes(lane(tile(3232, 3402, 3)) { gap(tile(3237, 3404, 3), tile(3236, 3403, 3)) })
    }

    row("dbrow.agility_varrock_ledge") {
        obstacle(VARROCK, step = 7, xp = 35, "loc.rooftops_varrock_stepuproof")
        lanes(
            lane(tile(3236, 3408, 3)) {
                exactMove(tile(3236, 3410, 3), delay1 = 8, delay2 = 50, angle = FACE_NORTH)
                anim(HURDLE)
                sound(HURDLE_SYNTH)
                next(2)
                land()
            }
        )
    }

    row("dbrow.agility_varrock_edge") {
        obstacle(VARROCK, step = 8, xp = 1437, "loc.rooftops_varrock_finish")
        lanes(
            lane(tile(3236, 3415, 3)) {
                exactMove(tile(3236, 3416, 3), delay1 = 15, delay2 = 30, angle = FACE_NORTH)
                anim(SPOT_JUMP)
                sound(JUMP_SYNTH)
                next()
                jumpDown(tile(3236, 3417, 0))
                land()
                next()
                resetAnim()
            }
        )
    }
}

internal fun DBTableBuilder.canifisObstacles() {
    row("dbrow.agility_canifis_tall_tree") {
        obstacle(CANIFIS, step = 0, xp = 100, "loc.rooftops_canifis_start_tree")
        lanes(
            lane(tile(3508, 3488, 0)) {
                exactMove(
                    tile(3507, 3489, 0),
                    delay1 = 30,
                    delay2 = 60,
                    angle = FACE_NORTH,
                    to = tile(3508, 3489, 0),
                )
                anim("seq.mdaughter_tree_climb_combi", delay = 60)
                sound("synth.tree_climbing", delay = 60)
                next(2)
                exactMove(tile(3505, 3489, 0), delay1 = 60, delay2 = 115, angle = FACE_EAST)
                next(4)
                telejump(tile(3506, 3492, 2))
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_canifis_gap_1") {
        obstacle(CANIFIS, step = 1, xp = 80, "loc.rooftops_canifis_jump")
        lanes(
            lane(tile(3505, 3497, 2)) {
                exactMove(tile(3505, 3498, 2), delay1 = 20, delay2 = 40, angle = FACE_NORTH)
                anim(RUN_UP, delay = 20)
                next()
                jumpDown(tile(3502, 3504, 2))
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_canifis_gap_2") {
        obstacle(CANIFIS, step = 2, xp = 80, "loc.rooftops_canifis_jump_2")
        lanes(
            lane(tile(3498, 3504, 2)) {
                face(tile(3497, 3504, 2))
                next()
                jumpDown(tile(3493, 3504, 2))
                next()
                step(tile(3492, 3504, 2))
                resetAnim()
                next()
                land()
            }
        )
    }

    row("dbrow.agility_canifis_gap_3") {
        obstacle(CANIFIS, step = 3, xp = 100, "loc.rooftops_canifis_jump_5")
        lanes(
            lane(tile(3487, 3499, 2)) {
                exactMove(tile(3481, 3499, 2), delay1 = 25, delay2 = 30, angle = FACE_WEST)
                anim(WALL_JUMP, delay = 20)
                sound(JUMP_UP_SYNTH, delay = 20)
                next()
                anim(WALL_JUMP_LAND)
                next()
                exactMove(tile(3479, 3499, 2), delay1 = 17, delay2 = 26, angle = FACE_WEST)
                next()
                telejump(tile(3479, 3499, 3))
                resetAnim()
                land()
            }
        )
        fails(
            175,
            300,
            FallDamage(min = 2, max = 5),
            lane(tile(3487, 3499, 2)) { fall(tile(3482, 3499, 0)) },
        )
    }

    row("dbrow.agility_canifis_gap_4") {
        obstacle(CANIFIS, step = 4, xp = 80, "loc.rooftops_canifis_jump_3")
        lanes(
            lane(tile(3478, 3493, 3)) {
                jumpDown(tile(3478, 3486, 2))
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_canifis_pole_vault") {
        obstacle(CANIFIS, step = 5, xp = 100, "loc.rooftops_canifis_polevault")
        lanes(
            lane(tile(3480, 3484, 2)) {
                exactMove(
                    tile(3478, 3485, 2),
                    delay1 = 0,
                    delay2 = 30,
                    angle = FACE_EAST,
                    from = tile(3478, 3486, 2),
                    to = tile(3480, 3484, 2),
                    faceAngle = false,
                )
                anim(RUN_UP)
                next()
                exactMove(
                    tile(3482, 3481, 2),
                    delay1 = 0,
                    delay2 = 30,
                    angle = FACE_EAST,
                    to = tile(3480, 3483, 2),
                )
                anim("seq.rooftops_pole_vault")
                for (n in 1..5) {
                    next()
                    val delay2 = if (n == 5) 28 else 30
                    exactMove(tile(3482 + n, 3481 - n, 2), 0, delay2, angle = FACE_EAST)
                }
                next()
                telejump(tile(3489, 3476, 3))
                anim(JUMP_DOWN_LAND)
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_canifis_gap_5") {
        obstacle(CANIFIS, step = 6, xp = 110, "loc.rooftops_canifis_jump_4")
        lanes(lane(tile(3502, 3476, 3)) { gap(tile(3504, 3476, 3), tile(3510, 3476, 2)) })
    }

    row("dbrow.agility_canifis_gap_6") {
        obstacle(CANIFIS, step = 7, xp = 1750, "loc.rooftops_canifis_leapdown")
        lanes(lane(tile(3510, 3482, 2)) { gap(tile(3510, 3484, 2), tile(3510, 3485, 0)) })
    }
}
