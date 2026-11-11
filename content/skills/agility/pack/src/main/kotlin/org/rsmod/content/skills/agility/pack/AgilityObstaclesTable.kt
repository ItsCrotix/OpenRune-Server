package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBRowBuilder
import dev.openrune.definition.dbtables.dbTable
import dev.openrune.definition.util.VarType
import dev.openrune.rscm.RSCM.asRSCM
import kotlin.math.sign
import org.rsmod.map.CoordGrid

/**
 * Every course obstacle in lap order. A lane is one way across: the player is put on its start
 * tile and its actions play out tick by tick, `land` granting the obstacle's xp. Xp is in tenths.
 *
 * The Draynor lanes are transcribed from a live capture of the course.
 */
object AgilityObstaclesTable {
    const val COURSE = 0
    const val STEP = 1
    const val LOCS = 2
    const val XP = 3
    const val LANES = 4
    const val ACTIONS = 5
    const val START_MESSAGE = 6
    const val END_MESSAGE = 7

    const val ACTION_STEP = 0
    const val ACTION_FACE = 1
    const val ACTION_ANIM = 2
    const val ACTION_RESET_ANIM = 3
    const val ACTION_SOUND = 4
    const val ACTION_BALANCE = 5
    const val ACTION_BALANCE_OFF = 6
    const val ACTION_EXACT_MOVE = 7
    const val ACTION_TELEJUMP = 8
    const val ACTION_LAND = 9

    private const val GNOME = "dbrow.agility_course_gnome_stronghold"
    private const val DRAYNOR = "dbrow.agility_course_draynor"

    private const val CLIMB = "seq.human_reachforladder"
    private const val LOG_BALANCE = "seq.human_walk_logbalance"
    private const val INTO_SIDESTEP = "seq.human_into_sidestepl"
    private const val SIDESTEP = "seq.human_walk_sidestepl"
    private const val OUT_OF_SIDESTEP = "seq.human_outof_sidestepl"
    private const val WALL_JUMP = "seq.agility_shortcut_wall_jump"
    private const val WALL_JUMP_LAND = "seq.agility_shortcut_wall_jump2"
    private const val JUMP_DOWN = "seq.agility_shortcut_wall_jumpdown"
    private const val JUMP_DOWN_LAND = "seq.agility_shortcut_wall_jumpdown2"
    private const val GNOME_CLIMB_DOWN = "seq.human_climbing_down"
    private const val PIPE_SQUEEZE = "seq.human_doublepipesqueeze"

    private const val TIGHTROPE_SYNTH = "synth.tightrope"
    private const val LEDGE_SYNTH = "synth.balancing_ledge"
    private const val JUMP_UP_SYNTH = "synth.jump_up"
    private const val JUMP_DOWN_SYNTH = "synth.jump2"

    fun obstacles() =
        dbTable("dbtable.agility_obstacles", serverOnly = true) {
            column("course", COURSE, VarType.DBROW)
            column("step", STEP, VarType.INT)
            column("locs", LOCS, VarType.LOC)
            column("xp", XP, VarType.INT)
            column("lanes", LANES, VarType.INT, VarType.COORDGRID)
            column(
                "actions",
                ACTIONS,
                VarType.INT,
                VarType.INT,
                VarType.INT,
                VarType.COORDGRID,
                VarType.INT,
                VarType.INT,
                VarType.INT,
            )
            column("start_message", START_MESSAGE, VarType.STRING)
            column("end_message", END_MESSAGE, VarType.STRING)

            // UNVERIFIED: no capture of the Gnome course yet. Its lanes copy the Draynor patterns
            // (climbs, balance walks), its pipe seq is picked by name and its messages are the
            // classic ones no wiki page quotes.
            row("dbrow.agility_gnome_log_balance") {
                obstacle(GNOME, step = 0, xp = 100, "loc.gnome_log_balance1")
                lanes(lane(tile(2474, 3436, 0)) { balanceWalk(tile(2474, 3429, 0), LOG_BALANCE) })
                column(START_MESSAGE, "You walk carefully across the slippery log...")
                column(END_MESSAGE, "...You make it safely to the other side.")
            }

            row("dbrow.agility_gnome_obstacle_net_1") {
                obstacle(GNOME, step = 1, xp = 100, "loc.obstical_net2")
                lanes(
                    *(2471..2476)
                        .map { x -> lane(tile(x, 3426, 0)) { climb(tile(x, 3423, 1), CLIMB) } }
                        .toTypedArray()
                )
                column(START_MESSAGE, "You climb the netting...")
            }

            row("dbrow.agility_gnome_tree_branch_up") {
                obstacle(GNOME, step = 2, xp = 65, "loc.climbing_branch")
                lanes(lane(tile(2473, 3423, 1)) { climb(tile(2473, 3420, 2), CLIMB) })
                column(START_MESSAGE, "You climb the tree...")
                column(END_MESSAGE, "...To the platform above.")
            }

            row("dbrow.agility_gnome_balancing_rope") {
                obstacle(GNOME, step = 3, xp = 100, "loc.balancing_rope")
                lanes(lane(tile(2477, 3420, 2)) { balanceWalk(tile(2483, 3420, 2), LOG_BALANCE) })
                column(START_MESSAGE, "You carefully cross the tightrope.")
            }

            row("dbrow.agility_gnome_tree_branch_down") {
                obstacle(GNOME, step = 4, xp = 65, "loc.climbing_tree", "loc.climbing_tree2")
                lanes(
                    lane(tile(2485, 3419, 2)) { climb(tile(2487, 3420, 0), GNOME_CLIMB_DOWN) },
                    lane(tile(2486, 3420, 2)) { climb(tile(2487, 3420, 0), GNOME_CLIMB_DOWN) },
                )
                column(START_MESSAGE, "You climb down the tree...")
                column(END_MESSAGE, "You land on the ground.")
            }

            row("dbrow.agility_gnome_obstacle_net_2") {
                obstacle(GNOME, step = 5, xp = 100, "loc.obstical_net3")
                lanes(
                    *(2483..2488)
                        .map { x -> lane(tile(x, 3425, 0)) { climb(tile(x, 3428, 0), CLIMB) } }
                        .toTypedArray()
                )
                column(START_MESSAGE, "You climb the netting...")
            }

            row("dbrow.agility_gnome_obstacle_pipe") {
                obstacle(GNOME, step = 6, xp = 100, "loc.obstical_pipe3_1", "loc.obstical_pipe3_2")
                lanes(
                    lane(tile(2484, 3430, 0)) { squeeze(tile(2484, 3437, 0)) },
                    lane(tile(2487, 3430, 0)) { squeeze(tile(2487, 3437, 0)) },
                )
                column(START_MESSAGE, "You pull yourself through the pipes.")
            }

            row("dbrow.agility_draynor_rough_wall") {
                obstacle(DRAYNOR, step = 0, xp = 50, "loc.rooftops_draynor_wallclimb")
                lanes(lane(tile(3103, 3279, 0)) { climb(tile(3102, 3279, 3), CLIMB) })
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
                        step(tile(3092, 3276, 3))
                        next()
                        balance(LOG_BALANCE)
                        sound(TIGHTROPE_SYNTH, loops = 7)
                        walkTo(tile(3092, 3267, 3))
                        next()
                        balanceOff()
                        step(tile(3092, 3266, 3))
                        land()
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
                        exactMove(tile(3088, 3256, 3), delay1 = 25, delay2 = 30)
                        anim(WALL_JUMP, delay = 20)
                        sound(JUMP_UP_SYNTH, delay = 20)
                        next()
                        anim(WALL_JUMP_LAND)
                        next()
                        exactMove(tile(3088, 3255, 3), delay1 = 17, delay2 = 26)
                        next()
                        resetAnim()
                        land()
                    }
                )
            }

            row("dbrow.agility_draynor_gap") {
                obstacle(DRAYNOR, step = 5, xp = 40, "loc.rooftops_draynor_leapdown")
                lanes(
                    lane(tile(3094, 3255, 3)) {
                        face(tile(3097, 3257, 3))
                        next()
                        jumpDown(tile(3096, 3256, 3))
                        next()
                        resetAnim()
                        land()
                    }
                )
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

    private class Action(
        val tick: Int,
        val op: Int,
        val coord: CoordGrid = CoordGrid.ZERO,
        val ref: Int = -1,
        val arg1: Int = 0,
        val arg2: Int = 0,
    )

    private class Lane(val start: CoordGrid, val actions: List<Action>)

    private class LaneBuilder(start: CoordGrid) {
        val actions = mutableListOf<Action>()
        private var tick = 0
        private var position = start

        fun next(ticks: Int = 1) {
            tick += ticks
        }

        fun step(dest: CoordGrid) {
            actions += Action(tick, ACTION_STEP, dest)
            position = dest
        }

        /** One step a tick towards [dest]; the last step is on the current tick on return. */
        fun walkTo(dest: CoordGrid) {
            var first = true
            while (position != dest) {
                if (!first) {
                    next()
                }
                step(
                    position.translate((dest.x - position.x).sign, (dest.z - position.z).sign)
                )
                first = false
            }
        }

        fun face(coord: CoordGrid) {
            actions += Action(tick, ACTION_FACE, coord)
        }

        fun anim(seq: String, delay: Int = 0) {
            actions += Action(tick, ACTION_ANIM, ref = seq.asRSCM(), arg1 = delay)
        }

        fun resetAnim() {
            actions += Action(tick, ACTION_RESET_ANIM)
        }

        fun sound(synth: String, loops: Int = 1, delay: Int = 0) {
            actions += Action(tick, ACTION_SOUND, ref = synth.asRSCM(), arg1 = loops, arg2 = delay)
        }

        fun balance(seq: String) {
            actions += Action(tick, ACTION_BALANCE, ref = seq.asRSCM())
        }

        fun balanceOff() {
            actions += Action(tick, ACTION_BALANCE_OFF)
        }

        fun exactMove(dest: CoordGrid, delay1: Int, delay2: Int) {
            actions += Action(tick, ACTION_EXACT_MOVE, dest, arg1 = delay1, arg2 = delay2)
            position = dest
        }

        fun telejump(dest: CoordGrid) {
            actions += Action(tick, ACTION_TELEJUMP, dest)
            position = dest
        }

        fun land() {
            actions += Action(tick, ACTION_LAND)
        }

        /** The Draynor rough wall: the climb seq, then a telejump two ticks later. */
        fun climb(dest: CoordGrid, seq: String) {
            anim(seq, delay = 15)
            next(2)
            telejump(dest)
            resetAnim()
            land()
        }

        /** The Draynor tightropes: a plain step on, balance walk, plain step off. */
        fun balanceWalk(dest: CoordGrid, seq: String) {
            val dx = (dest.x - position.x).sign
            val dz = (dest.z - position.z).sign
            step(position.translate(dx, dz))
            next()
            balance(seq)
            walkTo(dest.translate(-dx, -dz))
            next()
            balanceOff()
            step(dest)
            land()
        }

        /** The Draynor gap and crate: jump seq, then a telejump with the landing seq. */
        fun jumpDown(dest: CoordGrid) {
            anim(JUMP_DOWN, delay = 15)
            sound(JUMP_DOWN_SYNTH, delay = 15)
            next()
            telejump(dest)
            anim(JUMP_DOWN_LAND)
        }

        fun squeeze(dest: CoordGrid) {
            anim(PIPE_SQUEEZE)
            exactMove(dest, delay1 = 0, delay2 = PIPE_TICKS * CLIENT_CYCLES_PER_TICK)
            next(PIPE_TICKS)
            resetAnim()
            land()
        }
    }

    private const val PIPE_TICKS = 12
    private const val CLIENT_CYCLES_PER_TICK = 30

    private fun tile(x: Int, z: Int, level: Int): CoordGrid = CoordGrid(x, z, level)

    private fun lane(start: CoordGrid, block: LaneBuilder.() -> Unit): Lane =
        Lane(start, LaneBuilder(start).apply(block).actions)

    private fun DBRowBuilder.obstacle(course: String, step: Int, xp: Int, vararg locs: String) {
        columnRSCM(COURSE, course)
        column(STEP, step)
        columnRSCM(LOCS, *locs)
        column(XP, xp)
    }

    private fun DBRowBuilder.lanes(vararg lanes: Lane) {
        column(LANES, lanes.flatMapIndexed { index, lane -> listOf(index, lane.start.packed) })
        val actions = mutableListOf<Any>()
        for ((index, lane) in lanes.withIndex()) {
            require(lane.actions.count { it.op == ACTION_LAND } == 1) {
                "Lane $index starting ${lane.start} must land exactly once."
            }
            for (action in lane.actions) {
                actions.addAll(
                    listOf(
                        index,
                        action.tick,
                        action.op,
                        action.coord.packed,
                        action.ref,
                        action.arg1,
                        action.arg2,
                    )
                )
            }
        }
        column(ACTIONS, actions)
    }
}
