package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBRowBuilder
import dev.openrune.rscm.RSCM.asRSCM
import kotlin.math.sign
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTIONS
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_ANIM
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_BALANCE
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_BALANCE_OFF
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_EXACT_MOVE
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_EXACT_PATH
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_FACE
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_FACE_ANGLE
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_HIT
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_LAND
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_RESET_ANIM
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_SOUND
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_STEP
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.ACTION_TELEJUMP
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.AP_RANGE
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.COURSE
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.DIARY_XP
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.FAIL_ACTIONS
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.FAIL_DAMAGE
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.LAND_SPAWN_OBJ
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.LAND_SPAWN_TILES
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.LANES
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.LOCS
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.MESSAGES
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.REQUIREMENT
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.REQUIREMENT_OBJS
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.STEP
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.SUCCESS
import org.rsmod.content.skills.agility.pack.AgilityObstaclesTable.XP
import org.rsmod.map.CoordGrid

internal const val CLIMB = "seq.human_reachforladder"
internal const val LOG_BALANCE = "seq.human_walk_logbalance"
internal const val SIDESTEP = "seq.human_walk_sidestepl"
internal const val INTO_SIDESTEP = "seq.human_into_sidestepl"
internal const val OUT_OF_SIDESTEP = "seq.human_outof_sidestepl"
internal const val WALL_JUMP = "seq.agility_shortcut_wall_jump"
internal const val WALL_JUMP_LAND = "seq.agility_shortcut_wall_jump2"
internal const val JUMP_DOWN = "seq.agility_shortcut_wall_jumpdown"
internal const val JUMP_DOWN_LAND = "seq.agility_shortcut_wall_jumpdown2"
internal const val SPOT_JUMP = "seq.human_spot_jump"
internal const val HURDLE = "seq.human_jump_hurdle"
internal const val RUN_UP = "seq.ic_running"
internal const val HANDHOLDS = "seq.agilityarena_handholds_middle"
internal const val HANDHOLDS_LAST = "seq.agilityarena_handholds_last"
internal const val HANDHOLDS_REVERSE = "seq.agilityarena_handholds_middlereverse"
internal const val HANDHOLDS_REVERSE_LAST = "seq.agilityarena_handholds_lastreverse"
internal const val STUMBLE = "seq.human_walk_logbalance_stumble"

internal const val TIGHTROPE_SYNTH = "synth.tightrope"
internal const val LEDGE_SYNTH = "synth.balancing_ledge"
internal const val JUMP_SYNTH = "synth.jump"
internal const val JUMP_UP_SYNTH = "synth.jump_up"
internal const val JUMP_DOWN_SYNTH = "synth.jump2"
internal const val HURDLE_SYNTH = "synth.hurdle"
internal const val HANDHOLDS_SYNTH = "synth.handholds_loop"
internal const val FALL_LAND_SYNTH = "synth.fall_land"
internal const val STUMBLE_SYNTH = "synth.stumble_loop"

internal const val OP_TICK = -1

internal const val FACE_SOUTH = 0
internal const val FACE_WEST = 512
internal const val FACE_NORTH = 1024
internal const val FACE_EAST = 1536

internal class LaneAction(
    val tick: Int,
    val op: Int,
    val coord: CoordGrid = CoordGrid.ZERO,
    val ref: Int = -1,
    val arg1: Int = 0,
    val arg2: Int = 0,
)

internal class LaneMessage(val tick: Int, val text: String)

internal class Lane(
    val start: CoordGrid,
    val actions: List<LaneAction>,
    val messages: List<LaneMessage>,
)

/** Fall damage: a roll in [min]..[max], plus a [hpShare] fraction of the current hitpoints. */
internal class FallDamage(val min: Int, val max: Int, val hpShare: Pair<Int, Int> = 0 to 1)

internal class LaneBuilder(start: CoordGrid) {
    val actions = mutableListOf<LaneAction>()
    val messages = mutableListOf<LaneMessage>()
    private var tick = 0
    private var position = start

    fun next(ticks: Int = 1) {
        tick += ticks
    }

    fun message(text: String) {
        messages += LaneMessage(tick, text)
    }

    /** A message sent when the obstacle is clicked, a tick before the lane starts after a walk. */
    fun opMessage(text: String) {
        messages += LaneMessage(OP_TICK, text)
    }

    fun step(dest: CoordGrid) {
        actions += LaneAction(tick, ACTION_STEP, dest)
        position = dest
    }

    /** Steps one tile a tick towards [dest]; the last step is on the current tick on return. */
    fun walkTo(dest: CoordGrid) {
        var first = true
        while (position != dest) {
            if (!first) {
                next()
            }
            step(position.translate((dest.x - position.x).sign, (dest.z - position.z).sign))
            first = false
        }
    }

    fun face(coord: CoordGrid) {
        actions += LaneAction(tick, ACTION_FACE, coord)
    }

    /** Turns the player to [angle] on this tick, even one they move on, as live sends it. */
    fun faceAngle(angle: Int) {
        actions += LaneAction(tick, ACTION_FACE_ANGLE, ref = angle)
    }

    fun anim(seq: String, delay: Int = 0) {
        actions += LaneAction(tick, ACTION_ANIM, ref = seq.asRSCM(), arg1 = delay)
    }

    fun resetAnim() {
        actions += LaneAction(tick, ACTION_RESET_ANIM)
    }

    fun sound(synth: String, loops: Int = 1, delay: Int = 0) {
        actions += LaneAction(tick, ACTION_SOUND, ref = synth.asRSCM(), arg1 = loops, arg2 = delay)
    }

    fun balance(seq: String) {
        actions += LaneAction(tick, ACTION_BALANCE, ref = seq.asRSCM())
    }

    fun balanceOff() {
        actions += LaneAction(tick, ACTION_BALANCE_OFF)
    }

    /**
     * Moves the player to [dest] while the client draws them gliding between [delay1] and
     * [delay2]. The glide runs from the player's tile to [dest] unless [from]/[to] give the path
     * live draws, which on some obstacles is a tile or two off the tile the player ends on. Live
     * sends [angle] as the face angle too, except on the few moves [faceAngle] turns off.
     */
    fun exactMove(
        dest: CoordGrid,
        delay1: Int,
        delay2: Int,
        angle: Int,
        from: CoordGrid? = null,
        to: CoordGrid? = null,
        faceAngle: Boolean = true,
    ) {
        actions += LaneAction(tick, ACTION_EXACT_MOVE, dest, angle, delay1, delay2)
        if (faceAngle) {
            actions += LaneAction(tick, ACTION_FACE_ANGLE, ref = angle)
        }
        if (from != null || to != null) {
            val path = (to ?: dest).packed
            actions += LaneAction(tick, ACTION_EXACT_PATH, from ?: position, ref = path)
        }
        position = dest
    }

    fun telejump(dest: CoordGrid) {
        actions += LaneAction(tick, ACTION_TELEJUMP, dest)
        position = dest
    }

    fun land() {
        actions += LaneAction(tick, ACTION_LAND)
    }

    fun hit() {
        actions += LaneAction(tick, ACTION_HIT)
    }

    /** A rough wall or beam: face, the climb seq, then up two ticks later. */
    fun climbWall(face: CoordGrid, dest: CoordGrid) {
        face(face)
        anim(CLIMB, delay = 15)
        next(2)
        telejump(dest)
        resetAnim()
        land()
    }

    /** A jump down onto a lower roof or the ground: the jump seq, then the landing a tick later. */
    fun jumpDown(dest: CoordGrid) {
        anim(JUMP_DOWN, delay = 15)
        sound(JUMP_DOWN_SYNTH, delay = 15)
        next()
        telejump(dest)
        anim(JUMP_DOWN_LAND)
    }

    /** A gap leapt from where the player stands: face across, then [jumpDown]. */
    fun gap(face: CoordGrid, dest: CoordGrid) {
        face(face)
        next()
        jumpDown(dest)
        next()
        resetAnim()
        land()
    }

    /** A short jump across a ledge or gap on the same roof level, done in one exactmove. */
    fun hop(dest: CoordGrid, angle: Int, seq: String = HURDLE) {
        exactMove(dest, delay1 = 15, delay2 = 30, angle = angle)
        anim(seq)
        sound(JUMP_SYNTH)
        next()
        resetAnim()
        land()
    }

    /** A tightrope or plank: a plain step on, [seq] walk style to [last], then a plain step off. */
    fun tightrope(on: CoordGrid, last: CoordGrid, off: CoordGrid, loops: Int, seq: String) {
        step(on)
        next()
        balance(seq)
        sound(TIGHTROPE_SYNTH, loops = loops)
        walkTo(last)
        next()
        balanceOff()
        step(off)
        land()
    }

    /** A fall off a rope as captured on Prifddinas: a stumble, then the drop to [tile]. */
    fun stumble(tile: CoordGrid) {
        anim(STUMBLE)
        sound(STUMBLE_SYNTH, loops = 10)
        next()
        telejump(tile)
        balanceOff()
        resetAnim()
        sound(FALL_LAND_SYNTH)
        hit()
    }

    // UNVERIFIED: no capture shows a fall off a roof; this stumble, drop and hit is a guess.
    fun fall(tile: CoordGrid) {
        anim(STUMBLE)
        next()
        telejump(tile)
        anim(JUMP_DOWN_LAND)
        sound(FALL_LAND_SYNTH)
        hit()
        next()
        resetAnim()
    }
}

internal fun tile(x: Int, z: Int, level: Int): CoordGrid = CoordGrid(x, z, level)

internal fun lane(start: CoordGrid, block: LaneBuilder.() -> Unit): Lane {
    val builder = LaneBuilder(start).apply(block)
    return Lane(start, builder.actions, builder.messages)
}

internal fun DBRowBuilder.obstacle(course: String, step: Int, xp: Int, vararg locs: String) {
    columnRSCM(COURSE, course)
    column(STEP, step)
    columnRSCM(LOCS, *locs)
    column(XP, xp)
}

internal fun DBRowBuilder.lanes(vararg lanes: Lane) {
    for ((index, lane) in lanes.withIndex()) {
        require(lane.actions.count { it.op == ACTION_LAND } == 1) {
            "Lane $index starting ${lane.start} must land exactly once."
        }
    }
    column(LANES, lanes.flatMapIndexed { index, lane -> listOf(index, lane.start.packed) })
    column(ACTIONS, actionValues(lanes))
    val messages = lanes.flatMapIndexed { index, lane ->
        lane.messages.flatMap { listOf(index, it.tick, it.text) }
    }
    if (messages.isNotEmpty()) {
        column(MESSAGES, messages)
    }
}

/**
 * Makes the obstacle failable: success is `statRandom(agility, low, high)` and each fail lane, in
 * the order of the lanes, replaces the matching lane on a failed roll.
 */
internal fun DBRowBuilder.fails(low: Int, high: Int, damage: FallDamage, vararg lanes: Lane) {
    for ((index, lane) in lanes.withIndex()) {
        require(lane.actions.count { it.op == ACTION_HIT } == 1) {
            "Fail lane $index starting ${lane.start} must hit exactly once."
        }
        require(lane.actions.none { it.op == ACTION_LAND }) {
            "Fail lane $index starting ${lane.start} must not land."
        }
    }
    column(SUCCESS, low, high)
    column(FAIL_ACTIONS, actionValues(lanes))
    column(FAIL_DAMAGE, damage.min, damage.max, damage.hpShare.first, damage.hpShare.second)
}

/**
 * The obstacle refuses players who do not meet [requirement], a `REQUIREMENT_*` id, wearing one
 * of [objs].
 */
internal fun DBRowBuilder.requirement(requirement: Int, vararg objs: String) {
    column(REQUIREMENT, requirement)
    columnRSCM(REQUIREMENT_OBJS, *objs)
}

/**
 * Landing drops a private [obj] for the player on one of [tiles], unless they carry one already:
 * the stick the Werewolf course's Agility Boss throws.
 */
internal fun DBRowBuilder.landSpawn(obj: String, tiles: List<CoordGrid>) {
    columnRSCM(LAND_SPAWN_OBJ, obj)
    column(LAND_SPAWN_TILES, tiles.map { it.packed })
}

/**
 * Starts the obstacle once the player is within [distance] tiles with a clear line to it, for
 * obstacles whose start tile is behind a roof edge or across a gap from the loc.
 */
internal fun DBRowBuilder.apRange(distance: Int) {
    column(AP_RANGE, distance)
}

/** The obstacle pays [xp] instead once the player has completed the diary task in [varbit]. */
internal fun DBRowBuilder.diaryXp(varbit: String, xp: Int) {
    column(DIARY_XP, varbit.asRSCM(), xp)
}

private fun actionValues(lanes: Array<out Lane>): List<Any> {
    val values = mutableListOf<Any>()
    for ((index, lane) in lanes.withIndex()) {
        for (action in lane.actions) {
            values.addAll(
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
    return values
}
