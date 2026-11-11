package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBRowBuilder
import dev.openrune.definition.dbtables.dbTable
import dev.openrune.definition.util.VarType
import dev.openrune.rscm.RSCM.asRSCM
import org.rsmod.map.CoordGrid

/**
 * Every course obstacle in lap order. A lane is one way across: the player is put on its start
 * tile and carried through its moves, the last of which lands them. Xp is in tenths.
 *
 * Tiles and climb/leap tick counts come from the Shortest Path transport table
 * (`agility_shortcuts.tsv`); a walk lasts one tick per tile.
 */
object AgilityObstaclesTable {
    const val COURSE = 0
    const val STEP = 1
    const val LOCS = 2
    const val XP = 3
    const val LANES = 4
    const val MOVES = 5
    const val START_MESSAGE = 6
    const val END_MESSAGE = 7

    const val MOVE_WALK = 0
    const val MOVE_CLIMB = 1
    const val MOVE_LEAP = 2

    private const val GNOME = "dbrow.agility_course_gnome_stronghold"
    private const val DRAYNOR = "dbrow.agility_course_draynor"

    // UNVERIFIED: no source pairs these seqs with the obstacles; they are picked by name.
    private const val BALANCE = "seq.human_walk_logbalance"
    private const val SIDESTEP = "seq.human_walk_sidestep"
    private const val CLIMB_UP = "seq.human_reachforladder"
    private const val CLIMB_DOWN = "seq.human_climbing_down"
    private const val PIPE_SQUEEZE = "seq.human_doublepipesqueeze"
    private const val WALL_JUMP = "seq.agility_shortcut_wall_jump2"
    private const val LONG_JUMP = "seq.human_longjump"

    fun obstacles() =
        dbTable("dbtable.agility_obstacles", serverOnly = true) {
            column("course", COURSE, VarType.DBROW)
            column("step", STEP, VarType.INT)
            column("locs", LOCS, VarType.LOC)
            column("xp", XP, VarType.INT)
            column("lanes", LANES, VarType.INT, VarType.COORDGRID)
            column(
                "moves",
                MOVES,
                VarType.INT,
                VarType.INT,
                VarType.COORDGRID,
                VarType.SEQ,
                VarType.INT,
            )
            column("start_message", START_MESSAGE, VarType.STRING)
            column("end_message", END_MESSAGE, VarType.STRING)

            // UNVERIFIED: the Gnome messages are the classic ones; no wiki page quotes them.
            row("dbrow.agility_gnome_log_balance") {
                obstacle(GNOME, step = 0, xp = 100, "loc.gnome_log_balance1")
                lanes(lane(tile(2474, 3436, 0), walk(tile(2474, 3429, 0), BALANCE)))
                column(START_MESSAGE, "You walk carefully across the slippery log...")
                column(END_MESSAGE, "...You make it safely to the other side.")
            }

            row("dbrow.agility_gnome_obstacle_net_1") {
                obstacle(GNOME, step = 1, xp = 100, "loc.obstical_net2")
                lanes(
                    *(2471..2476)
                        .map { x -> lane(tile(x, 3426, 0), climb(tile(x, 3423, 1), CLIMB_UP, 2)) }
                        .toTypedArray()
                )
                column(START_MESSAGE, "You climb the netting...")
            }

            row("dbrow.agility_gnome_tree_branch_up") {
                obstacle(GNOME, step = 2, xp = 65, "loc.climbing_branch")
                lanes(lane(tile(2473, 3423, 1), climb(tile(2473, 3420, 2), CLIMB_UP, 2)))
                column(START_MESSAGE, "You climb the tree...")
                column(END_MESSAGE, "...To the platform above.")
            }

            row("dbrow.agility_gnome_balancing_rope") {
                obstacle(GNOME, step = 3, xp = 100, "loc.balancing_rope")
                lanes(lane(tile(2477, 3420, 2), walk(tile(2483, 3420, 2), BALANCE)))
                column(START_MESSAGE, "You carefully cross the tightrope.")
            }

            row("dbrow.agility_gnome_tree_branch_down") {
                obstacle(GNOME, step = 4, xp = 65, "loc.climbing_tree", "loc.climbing_tree2")
                lanes(
                    lane(tile(2485, 3419, 2), climb(tile(2487, 3420, 0), CLIMB_DOWN, 2)),
                    lane(tile(2486, 3420, 2), climb(tile(2487, 3420, 0), CLIMB_DOWN, 2)),
                )
                column(START_MESSAGE, "You climb down the tree...")
                column(END_MESSAGE, "You land on the ground.")
            }

            row("dbrow.agility_gnome_obstacle_net_2") {
                obstacle(GNOME, step = 5, xp = 100, "loc.obstical_net3")
                lanes(
                    *(2483..2488)
                        .map { x -> lane(tile(x, 3425, 0), climb(tile(x, 3428, 0), CLIMB_UP, 2)) }
                        .toTypedArray()
                )
                column(START_MESSAGE, "You climb the netting...")
            }

            row("dbrow.agility_gnome_obstacle_pipe") {
                obstacle(GNOME, step = 6, xp = 100, "loc.obstical_pipe3_1", "loc.obstical_pipe3_2")
                lanes(
                    lane(tile(2484, 3430, 0), leap(tile(2484, 3437, 0), PIPE_SQUEEZE, 12)),
                    lane(tile(2487, 3430, 0), leap(tile(2487, 3437, 0), PIPE_SQUEEZE, 12)),
                )
                column(START_MESSAGE, "You pull yourself through the pipes.")
            }

            row("dbrow.agility_draynor_rough_wall") {
                obstacle(DRAYNOR, step = 0, xp = 50, "loc.rooftops_draynor_wallclimb")
                lanes(lane(tile(3103, 3279, 0), climb(tile(3102, 3279, 3), CLIMB_UP, 4)))
            }

            row("dbrow.agility_draynor_tightrope_1") {
                obstacle(DRAYNOR, step = 1, xp = 80, "loc.rooftops_draynor_tightrope_1")
                lanes(
                    lane(
                        tile(3099, 3277, 3),
                        walk(tile(3090, 3277, 3), BALANCE),
                        walk(tile(3090, 3276, 3), BALANCE),
                    )
                )
            }

            row("dbrow.agility_draynor_tightrope_2") {
                obstacle(DRAYNOR, step = 2, xp = 70, "loc.rooftops_draynor_tightrope_2")
                lanes(
                    lane(
                        tile(3091, 3276, 3),
                        walk(tile(3092, 3276, 3), BALANCE),
                        walk(tile(3092, 3266, 3), BALANCE),
                    )
                )
            }

            row("dbrow.agility_draynor_narrow_wall") {
                obstacle(DRAYNOR, step = 3, xp = 70, "loc.rooftops_draynor_wallcrossing")
                lanes(
                    lane(
                        tile(3089, 3265, 3),
                        walk(tile(3089, 3262, 3), SIDESTEP),
                        walk(tile(3088, 3261, 3), SIDESTEP),
                    )
                )
            }

            row("dbrow.agility_draynor_wall") {
                obstacle(DRAYNOR, step = 4, xp = 100, "loc.rooftops_draynor_wallscramble")
                lanes(lane(tile(3088, 3257, 3), leap(tile(3088, 3255, 3), WALL_JUMP, 4)))
            }

            row("dbrow.agility_draynor_gap") {
                obstacle(DRAYNOR, step = 5, xp = 40, "loc.rooftops_draynor_leapdown")
                lanes(lane(tile(3094, 3255, 3), leap(tile(3096, 3256, 3), LONG_JUMP, 4)))
            }

            row("dbrow.agility_draynor_crate") {
                obstacle(DRAYNOR, step = 6, xp = 790, "loc.rooftops_draynor_crate")
                lanes(lane(tile(3101, 3261, 3), climb(tile(3103, 3261, 0), CLIMB_DOWN, 8)))
            }
        }

    private class Lane(val start: CoordGrid, val moves: List<Move>)

    private class Move(val kind: Int, val dest: CoordGrid, val seq: String, val ticks: Int)

    private fun tile(x: Int, z: Int, level: Int): CoordGrid = CoordGrid(x, z, level)

    private fun lane(start: CoordGrid, vararg moves: Move): Lane = Lane(start, moves.toList())

    private fun walk(dest: CoordGrid, seq: String): Move = Move(MOVE_WALK, dest, seq, ticks = 0)

    private fun climb(dest: CoordGrid, seq: String, ticks: Int): Move =
        Move(MOVE_CLIMB, dest, seq, ticks)

    private fun leap(dest: CoordGrid, seq: String, ticks: Int): Move =
        Move(MOVE_LEAP, dest, seq, ticks)

    private fun DBRowBuilder.obstacle(course: String, step: Int, xp: Int, vararg locs: String) {
        columnRSCM(COURSE, course)
        column(STEP, step)
        columnRSCM(LOCS, *locs)
        column(XP, xp)
    }

    private fun DBRowBuilder.lanes(vararg lanes: Lane) {
        column(LANES, lanes.flatMapIndexed { index, lane -> listOf(index, lane.start.packed) })
        val moves = mutableListOf<Any>()
        for ((index, lane) in lanes.withIndex()) {
            var from = lane.start
            for (move in lane.moves) {
                val ticks =
                    if (move.kind == MOVE_WALK) from.chebyshevDistance(move.dest) else move.ticks
                moves.addAll(listOf(index, move.kind, move.dest.packed, move.seq.asRSCM(), ticks))
                from = move.dest
            }
        }
        column(MOVES, moves)
    }
}
