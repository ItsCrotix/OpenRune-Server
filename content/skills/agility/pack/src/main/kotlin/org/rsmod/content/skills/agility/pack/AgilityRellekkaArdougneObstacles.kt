package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBTableBuilder
import org.rsmod.map.CoordGrid

private const val RELLEKKA = "dbrow.agility_course_rellekka"
private const val ARDOUGNE = "dbrow.agility_course_ardougne"

/*
 * Both courses follow captures from rsprox.net's archive: Rellekka 1864 and 1885 (Leagues V),
 * Ardougne 787 (19 laps). UNVERIFIED: the falls, none of which the captures show. The success
 * curves stop failing where the wiki says (Rellekka 85, Ardougne 95); Rellekka's fall damage is
 * the wiki's tenth of current hitpoints, Ardougne's a guess.
 */
private val ARDOUGNE_FALL = FallDamage(min = 3, max = 8)

private const val CLIMBING = "seq.human_climbing"
private const val INTO_SIDESTEP_RIGHT = "seq.human_into_sidestep"
private const val SIDESTEP_RIGHT = "seq.human_walk_sidestep"
private const val JUMP_DOWN_FLAT = "seq.agility_shortcut_wall_jumpdown_noreachforward"

internal fun DBTableBuilder.rellekkaObstacles() {
    row("dbrow.agility_rellekka_rough_wall") {
        obstacle(RELLEKKA, step = 0, xp = 200, "loc.rooftops_rellekka_wallclimb")
        lanes(lane(tile(2625, 3677, 0)) { climbWall(tile(2625, 3676, 0), tile(2625, 3676, 3)) })
    }

    row("dbrow.agility_rellekka_gap_1") {
        obstacle(RELLEKKA, step = 1, xp = 300, "loc.rooftops_rellekka_gap_1")
        lanes(lane(tile(2622, 3672, 3)) { runUpHurdle(tile(2622, 3668, 3), FACE_SOUTH) })
    }

    row("dbrow.agility_rellekka_tightrope_1") {
        obstacle(RELLEKKA, step = 2, xp = 400, "loc.rooftops_rellekka_tightrope_1")
        lanes(
            lane(tile(2622, 3658, 3)) {
                next()
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 3)
                step(tile(2623, 3657, 3))
                next()
                walkTo(tile(2626, 3654, 3))
                next()
                balanceOff()
                step(tile(2627, 3654, 3))
                land()
            }
        )
        fails(
            0,
            300,
            FallDamage(min = 0, max = 0, hpShare = 1 to 10),
            lane(tile(2622, 3658, 3)) { fall(tile(2624, 3656, 0)) },
        )
    }

    row("dbrow.agility_rellekka_gap_2") {
        obstacle(RELLEKKA, step = 3, xp = 850, "loc.rooftops_rellekka_gap_2")
        lanes(
            lane(tile(2629, 3655, 3)) {
                exactMove(
                    tile(2629, 3658, 3),
                    delay1 = 25,
                    delay2 = 30,
                    angle = FACE_NORTH,
                    faceAngle = false,
                )
                anim(WALL_JUMP, delay = 20)
                sound(JUMP_UP_SYNTH, delay = 20)
                next()
                anim(INTO_SIDESTEP_RIGHT)
                balance(SIDESTEP_RIGHT)
                faceAngle(FACE_EAST)
                sound(LEDGE_SYNTH, loops = 4)
                next()
                walkTo(tile(2634, 3658, 3))
                sound(LEDGE_SYNTH, loops = 2, delay = 10)
                next()
                step(tile(2635, 3658, 3))
                next()
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 4)
                step(tile(2636, 3657, 3))
                next()
                walkTo(tile(2639, 3654, 3))
                next()
                step(tile(2639, 3653, 3))
                next()
                balanceOff()
                next()
                land()
            }
        )
    }

    row("dbrow.agility_rellekka_gap_3") {
        obstacle(RELLEKKA, step = 4, xp = 250, "loc.rooftops_rellekka_gap_3")
        lanes(lane(tile(2643, 3653, 3)) { runUpHurdle(tile(2643, 3657, 3), FACE_NORTH) })
    }

    row("dbrow.agility_rellekka_tightrope_2") {
        obstacle(RELLEKKA, step = 5, xp = 1050, "loc.rooftops_rellekka_tightrope_3")
        lanes(
            lane(tile(2647, 3662, 3)) {
                faceAngle(1280)
                next()
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 6)
                step(tile(2647, 3663, 3))
                next()
                walkTo(tile(2654, 3670, 3))
                next()
                step(tile(2655, 3670, 3))
                next()
                balanceOff()
                next()
                land()
            }
        )
    }

    row("dbrow.agility_rellekka_pile_of_fish") {
        obstacle(RELLEKKA, step = 6, xp = 4750, "loc.rooftops_rellekka_dropoff")
        lanes(
            lane(tile(2655, 3676, 3)) {
                faceAngle(FACE_WEST)
                anim(JUMP_DOWN, delay = 15)
                sound(JUMP_DOWN_SYNTH, delay = 15)
                next()
                telejump(tile(2653, 3676, 0))
                anim(JUMP_DOWN_LAND)
                land()
                next()
                step(tile(2652, 3676, 0))
            }
        )
        diaryXp("varbit.fremennik_diary_hard_complete", 6150)
    }
}

/** A run-up a tick before a hurdle to [dest], as on Rellekka's first and third gaps. */
private fun LaneBuilder.runUpHurdle(dest: CoordGrid, angle: Int) {
    anim(RUN_UP, delay = 15)
    next()
    exactMove(dest, delay1 = 8, delay2 = 50, angle = angle, faceAngle = false)
    anim(HURDLE)
    sound(HURDLE_SYNTH)
    next(2)
    land()
}

internal fun DBTableBuilder.ardougneObstacles() {
    row("dbrow.agility_ardougne_wooden_beams") {
        obstacle(ARDOUGNE, step = 0, xp = 430, "loc.rooftops_ardy_wallclimb")
        lanes(
            lane(tile(2673, 3298, 0)) {
                face(tile(2673, 3299, 0))
                anim(CLIMBING, delay = 15)
                next()
                telejump(tile(2673, 3298, 1))
                anim(CLIMBING)
                next()
                telejump(tile(2673, 3298, 2))
                anim(CLIMBING)
                next()
                telejump(tile(2671, 3299, 3))
                anim(JUMP_DOWN_LAND)
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_ardougne_gap_1") {
        obstacle(ARDOUGNE, step = 1, xp = 650, "loc.rooftops_ardy_jump")
        lanes(
            lane(tile(2671, 3309, 3)) {
                faceAngle(663)
                next()
                jumpAcross(tile(2667, 3311, 1))
                faceAngle(872)
                next()
                jumpAcross(tile(2665, 3315, 1))
                next()
                jumpAcross(tile(2665, 3318, 3), WALL_JUMP)
                next()
                land()
            }
        )
        fails(0, 266, ARDOUGNE_FALL, lane(tile(2671, 3309, 3)) { fall(tile(2668, 3314, 0)) })
    }

    row("dbrow.agility_ardougne_plank") {
        obstacle(ARDOUGNE, step = 2, xp = 500, "loc.rooftops_ardy_plank")
        lanes(
            lane(tile(2662, 3318, 3)) {
                next()
                balance(LOG_BALANCE)
                sound(TIGHTROPE_SYNTH, loops = 4)
                step(tile(2661, 3318, 3))
                next()
                walkTo(tile(2656, 3318, 3))
                balanceOff()
                next()
                land()
            }
        )
    }

    row("dbrow.agility_ardougne_gap_2") {
        obstacle(ARDOUGNE, step = 3, xp = 210, "loc.rooftops_ardy_jump_2")
        lanes(
            lane(tile(2654, 3318, 3)) {
                faceAngle(79)
                jumpAcross(tile(2653, 3314, 3))
                next()
                resetAnim()
                land()
            }
        )
        fails(0, 266, ARDOUGNE_FALL, lane(tile(2654, 3318, 3)) { fall(tile(2653, 3316, 0)) })
    }

    row("dbrow.agility_ardougne_gap_3") {
        obstacle(ARDOUGNE, step = 4, xp = 280, "loc.rooftops_ardy_jump_3")
        lanes(
            lane(tile(2653, 3310, 3)) {
                faceAngle(360)
                next()
                jumpAcross(tile(2651, 3309, 3), JUMP_DOWN_FLAT)
                next()
                resetAnim()
                land()
            }
        )
    }

    row("dbrow.agility_ardougne_steep_roof") {
        obstacle(ARDOUGNE, step = 5, xp = 570, "loc.rooftops_ardy_wallcrossing")
        lanes(
            lane(tile(2653, 3300, 3)) {
                anim(INTO_SIDESTEP)
                balance(SIDESTEP)
                step(tile(2654, 3299, 3))
                next()
                sound(LEDGE_SYNTH, loops = 3)
                step(tile(2655, 3298, 3))
                next()
                step(tile(2656, 3297, 3))
                next(2)
                balanceOff()
                anim(OUT_OF_SIDESTEP)
                land()
            }
        )
    }

    row("dbrow.agility_ardougne_gap_4") {
        obstacle(ARDOUGNE, step = 6, xp = 6250, "loc.rooftops_ardy_jump_4")
        apRange(2)
        lanes(
            lane(tile(2656, 3297, 3)) {
                faceAngle(FACE_EAST)
                jumpAcross(tile(2658, 3298, 1))
                next(2)
                walkTo(tile(2661, 3298, 1))
                next(3)
                exactMove(
                    tile(2663, 3297, 1),
                    delay1 = 15,
                    delay2 = 30,
                    angle = FACE_EAST,
                    faceAngle = false,
                )
                anim(SPOT_JUMP)
                sound(JUMP_SYNTH)
                next()
                walkTo(tile(2666, 3297, 1))
                next(3)
                exactMove(tile(2667, 3297, 1), delay1 = 15, delay2 = 30, angle = FACE_EAST)
                anim(SPOT_JUMP)
                sound(JUMP_SYNTH)
                next()
                anim(JUMP_DOWN)
                sound(JUMP_DOWN_SYNTH)
                next()
                telejump(tile(2668, 3297, 0))
                anim(JUMP_DOWN_LAND)
                next()
                resetAnim()
                land()
            }
        )
    }
}

/** A jump with [seq] to [dest] a tick later, landing there; what each Ardougne gap is made of. */
private fun LaneBuilder.jumpAcross(dest: CoordGrid, seq: String = JUMP_DOWN) {
    anim(seq, delay = 15)
    sound(JUMP_DOWN_SYNTH, delay = 15)
    next()
    telejump(dest)
    anim(JUMP_DOWN_LAND)
}
