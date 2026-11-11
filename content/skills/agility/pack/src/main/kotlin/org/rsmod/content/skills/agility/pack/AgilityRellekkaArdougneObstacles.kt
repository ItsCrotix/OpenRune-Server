package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBTableBuilder

private const val RELLEKKA = "dbrow.agility_course_rellekka"
private const val ARDOUGNE = "dbrow.agility_course_ardougne"

/*
 * UNVERIFIED: no capture of either course. Each lane copies the captured pattern of the same kind
 * of obstacle on another rooftop, on start and landing tiles from another implementation checked
 * against the collision map: the roof past Ardougne's steep roof is a bridge, so on level 2. The
 * success curves stop failing where the wiki says (Rellekka 85, Ardougne 95); Rellekka's fall
 * damage is the wiki's tenth of current hitpoints, Ardougne's a guess.
 */
private val ARDOUGNE_FALL = FallDamage(min = 3, max = 8)

internal fun DBTableBuilder.rellekkaObstacles() {
    row("dbrow.agility_rellekka_rough_wall") {
        obstacle(RELLEKKA, step = 0, xp = 200, "loc.rooftops_rellekka_wallclimb")
        lanes(lane(tile(2625, 3677, 0)) { climbWall(tile(2625, 3676, 0), tile(2625, 3676, 3)) })
    }

    row("dbrow.agility_rellekka_gap_1") {
        obstacle(RELLEKKA, step = 1, xp = 300, "loc.rooftops_rellekka_gap_1")
        lanes(lane(tile(2622, 3672, 3)) { gap(tile(2622, 3668, 3), tile(2622, 3668, 3)) })
    }

    row("dbrow.agility_rellekka_tightrope_1") {
        obstacle(RELLEKKA, step = 2, xp = 400, "loc.rooftops_rellekka_tightrope_1")
        lanes(
            lane(tile(2622, 3658, 3)) {
                step(tile(2623, 3658, 3))
                next()
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 3)
                walkTo(tile(2626, 3655, 3))
                next()
                balanceOff()
                step(tile(2627, 3654, 3))
                land()
            }
        )
        fails(
            0,
            300,
            FallDamage(min = 0, max = 0, hpDivisor = 10),
            lane(tile(2622, 3658, 3)) { fall(tile(2624, 3656, 0)) },
        )
    }

    row("dbrow.agility_rellekka_gap_2") {
        obstacle(RELLEKKA, step = 3, xp = 850, "loc.rooftops_rellekka_gap_2")
        lanes(
            lane(tile(2629, 3655, 3)) {
                face(tile(2639, 3653, 3))
                anim(RUN_UP)
                next()
                exactMove(tile(2639, 3653, 3), delay1 = 20, delay2 = 50, angle = FACE_EAST)
                anim("seq.seaslug_ken_leap", delay = 15)
                sound(JUMP_SYNTH, delay = 15)
                next(2)
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_rellekka_gap_3") {
        obstacle(RELLEKKA, step = 4, xp = 250, "loc.rooftops_rellekka_gap_3")
        lanes(
            lane(tile(2643, 3653, 3)) {
                exactMove(tile(2643, 3657, 3), delay1 = 8, delay2 = 50, angle = FACE_NORTH)
                anim(HURDLE)
                sound(HURDLE_SYNTH)
                next(2)
                land()
            }
        )
    }

    row("dbrow.agility_rellekka_tightrope_2") {
        obstacle(RELLEKKA, step = 5, xp = 1050, "loc.rooftops_rellekka_tightrope_3")
        lanes(
            lane(tile(2647, 3662, 3)) {
                step(tile(2647, 3663, 3))
                next()
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 7)
                walkTo(tile(2653, 3669, 3))
                next()
                walkTo(tile(2654, 3669, 3))
                next()
                balanceOff()
                step(tile(2655, 3669, 3))
                land()
            }
        )
    }

    row("dbrow.agility_rellekka_pile_of_fish") {
        obstacle(RELLEKKA, step = 6, xp = 4750, "loc.rooftops_rellekka_dropoff")
        lanes(
            lane(tile(2655, 3676, 3)) {
                face(tile(2652, 3676, 3))
                next()
                jumpDown(tile(2652, 3676, 0))
                land()
                next()
                resetAnim()
            }
        )
        diaryXp("varbit.fremennik_diary_hard_complete", 6150)
    }
}

internal fun DBTableBuilder.ardougneObstacles() {
    row("dbrow.agility_ardougne_wooden_beams") {
        obstacle(ARDOUGNE, step = 0, xp = 430, "loc.rooftops_ardy_wallclimb")
        lanes(lane(tile(2673, 3298, 0)) { climbWall(tile(2672, 3298, 0), tile(2671, 3299, 3)) })
    }

    row("dbrow.agility_ardougne_gap_1") {
        obstacle(ARDOUGNE, step = 1, xp = 650, "loc.rooftops_ardy_jump")
        lanes(lane(tile(2671, 3309, 3)) { gap(tile(2666, 3317, 3), tile(2665, 3318, 3)) })
        fails(0, 266, ARDOUGNE_FALL, lane(tile(2671, 3309, 3)) { fall(tile(2668, 3314, 0)) })
    }

    row("dbrow.agility_ardougne_plank") {
        obstacle(ARDOUGNE, step = 2, xp = 500, "loc.rooftops_ardy_plank")
        lanes(
            lane(tile(2662, 3318, 3)) {
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 5)
                step(tile(2661, 3318, 3))
                next()
                walkTo(tile(2656, 3318, 3))
                next()
                balanceOff()
                land()
            }
        )
    }

    row("dbrow.agility_ardougne_gap_2") {
        obstacle(ARDOUGNE, step = 3, xp = 210, "loc.rooftops_ardy_jump_2")
        lanes(lane(tile(2654, 3318, 3)) { gap(tile(2653, 3315, 3), tile(2653, 3314, 3)) })
        fails(0, 266, ARDOUGNE_FALL, lane(tile(2654, 3318, 3)) { fall(tile(2653, 3316, 0)) })
    }

    row("dbrow.agility_ardougne_gap_3") {
        obstacle(ARDOUGNE, step = 4, xp = 280, "loc.rooftops_ardy_jump_3")
        lanes(lane(tile(2653, 3310, 3)) { gap(tile(2651, 3307, 3), tile(2651, 3308, 3)) })
    }

    row("dbrow.agility_ardougne_steep_roof") {
        obstacle(ARDOUGNE, step = 5, xp = 570, "loc.rooftops_ardy_wallcrossing")
        lanes(
            lane(tile(2653, 3301, 3)) {
                anim(INTO_SIDESTEP)
                balance(SIDESTEP)
                step(tile(2654, 3301, 3))
                next()
                sound(LEDGE_SYNTH, loops = 3)
                step(tile(2655, 3300, 2))
                next()
                step(tile(2656, 3299, 2))
                next()
                balanceOff()
                anim(OUT_OF_SIDESTEP)
                land()
            }
        )
    }

    row("dbrow.agility_ardougne_gap_4") {
        obstacle(ARDOUGNE, step = 6, xp = 6250, "loc.rooftops_ardy_jump_4")
        apRange(3)
        lanes(
            lane(tile(2656, 3299, 2)) {
                face(tile(2659, 3297, 2))
                next()
                jumpDown(tile(2668, 3297, 0))
                land()
                next()
                resetAnim()
            }
        )
    }
}
