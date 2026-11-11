package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBTableBuilder

private const val PENGUIN = "dbrow.agility_course_penguin"

private const val PENGUIN_JUMP = "seq.peng_jump"
private const val ICICLE_WALK = "seq.peng_icicle_walk"
private const val SLIPPERY_WALK = "seq.peng_slippery_walk"
private const val SLIDE_WALK = "seq.peng_slide_walk"

private val ICICLES =
    listOf(
        "dbrow.agility_penguin_icicles_1" to tile(2644, 4083, 1),
        "dbrow.agility_penguin_icicles_2" to tile(2652, 4079, 1),
        "dbrow.agility_penguin_icicles_3" to tile(2658, 4082, 1),
        "dbrow.agility_penguin_icicles_4" to tile(2662, 4082, 1),
    )

/*
 * UNVERIFIED: no capture and no wiki tiles; the lanes cross the obstacles' locs with the cache's
 * penguin seqs. None fail, although the wiki says every obstacle can.
 *
 * TODO(content): live lets only players turned into a penguin by the clockwork suit run this
 * course, and starts it with the crushers (55 xp), which are npcs. Neither exists here yet.
 */
internal fun DBTableBuilder.penguinObstacles() {
    row("dbrow.agility_penguin_stepping_stones") {
        obstacle(
            PENGUIN,
            step = 0,
            xp = 800,
            "loc.peng_agility_crushcourse_stepstone01",
        )
        lanes(
            lane(tile(2631, 4055, 0)) {
                val stones =
                    listOf(
                        tile(2631, 4057, 0) to FACE_NORTH,
                        tile(2631, 4059, 0) to FACE_NORTH,
                        tile(2633, 4059, 0) to FACE_EAST,
                        tile(2635, 4059, 0) to FACE_EAST,
                        tile(2635, 4061, 0) to FACE_NORTH,
                        tile(2635, 4063, 0) to FACE_NORTH,
                        tile(2635, 4065, 1) to FACE_NORTH,
                        tile(2635, 4067, 1) to FACE_NORTH,
                    )
                for ((dest, angle) in stones) {
                    exactMove(dest, delay1 = 15, delay2 = 40, angle = angle)
                    anim(PENGUIN_JUMP)
                    sound(JUMP_SYNTH)
                    next(3)
                }
                land()
            }
        )
    }

    for ((index, icicles) in ICICLES.withIndex()) {
        val (row, pillar) = icicles
        row(row) {
            obstacle(PENGUIN, step = 1 + index, xp = 400, "loc.peng_iciclepillar_clickzone")
            apRange(2)
            lanes(
                lane(tile(pillar.x - 1, pillar.z, 1)) {
                    exactMove(tile(pillar.x + 1, pillar.z, 1), 0, 120, angle = FACE_EAST)
                    anim(ICICLE_WALK)
                    next(4)
                    land()
                }
            )
        }
    }

    row("dbrow.agility_penguin_ice") {
        obstacle(
            PENGUIN,
            step = 5,
            xp = 1800,
            "loc.peng_agility_slippery_glitters01",
            "loc.peng_agility_slippery_glitters02",
            "loc.peng_agility_slippery_glitters03",
            "loc.peng_agility_slippery_glitters04",
            "loc.peng_agility_slippery_glitters05",
            "loc.peng_agility_slippery_glitters06",
            "loc.peng_agility_slippery_glitters07",
            "loc.peng_agility_slippery_glitters08",
            "loc.peng_agility_slippery_glitters09",
        )
        lanes(
            lane(tile(2665, 4078, 1)) {
                balance(SLIPPERY_WALK)
                step(tile(2665, 4077, 1))
                next()
                walkTo(tile(2665, 4068, 1))
                next()
                balanceOff()
                land()
            }
        )
    }

    row("dbrow.agility_penguin_gate") {
        obstacle(PENGUIN, step = 6, xp = 650, "loc.peng_agility_fencing_door")
        lanes(
            lane(tile(2653, 4039, 1)) {
                balance(SLIDE_WALK)
                step(tile(2652, 4039, 1))
                next()
                walkTo(tile(2650, 4039, 1))
                next()
                balanceOff()
                land()
            }
        )
    }
}
