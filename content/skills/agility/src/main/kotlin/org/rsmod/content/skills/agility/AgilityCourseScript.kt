package org.rsmod.content.skills.agility

import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCMType
import dev.openrune.types.BasType
import jakarta.inject.Inject
import kotlin.math.sign
import org.rsmod.api.config.constants
import org.rsmod.api.player.hook.TeleportType
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.script.onOpLoc1
import org.rsmod.game.entity.Player
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Runs every obstacle in `dbtable.agility_obstacles`: op1 on one of its locs carries the player
 * across, grants its xp on landing and tracks the lap. A lap passed in order pays the course's lap
 * bonus and rolls for a mark of grace (https://oldschool.runescape.wiki/w/Agility).
 */
class AgilityCourseScript @Inject constructor(private val marks: MarksOfGrace) : PluginScript() {
    override fun ScriptContext.startup() {
        val obstacles = AgilityObstacle.loadAll()
        val lastSteps =
            obstacles.groupBy { it.course.rowId }.mapValues { (_, it) -> it.maxOf { o -> o.step } }
        for (obstacle in obstacles) {
            val lastStep = lastSteps.getValue(obstacle.course.rowId)
            for (loc in obstacle.locs) {
                onOpLoc1(loc) { cross(obstacle, lastStep) }
            }
        }
    }

    private suspend fun ProtectedAccess.cross(obstacle: AgilityObstacle, lastStep: Int) {
        arriveDelay()
        val course = obstacle.course
        if (player.agilityLvl < course.level) {
            // UNVERIFIED: wording guessed; Gnome and Draynor (level 1) never show it.
            mes("You need an Agility level of ${course.level} to attempt this course.")
            return
        }
        val lane = obstacle.laneFrom(coords)
        if (lane == null) {
            // UNVERIFIED: wording guessed.
            mes("You can't do that from here.")
            return
        }
        obstacle.startMessage?.let(::mes)
        if (coords != lane.start) {
            teleport(lane.start, TeleportType.Exempt)
            delay(1)
        }
        for (move in lane.moves) {
            when (move.kind) {
                MoveKind.Walk -> walk(move)
                MoveKind.Climb -> climb(move)
                MoveKind.Leap -> leap(move)
            }
        }
        obstacle.endMessage?.let(::mes)
        statAdvance(AGILITY, obstacle.xp)

        if (!player.advanceLap(obstacle, lastStep)) {
            return
        }
        if (course.lapBonusXp > 0) {
            statAdvance(AGILITY, course.lapBonusXp / 10.0)
        }
        marks.rollLap(this, course)
    }

    /** One tile a tick in a straight line, walking in the move's seq. */
    private suspend fun ProtectedAccess.walk(move: ObstacleMove) {
        val seq = move.seq.id
        val previous = player.bas
        player.bas = BasType(-1, seq, seq, seq, seq, seq, seq, seq)
        rebuildAppearance()
        try {
            repeat(move.ticks) {
                teleport(coords.stepTowards(move.dest), TeleportType.Exempt)
                delay(1)
            }
        } finally {
            if (coords != move.dest) {
                telejump(move.dest, TeleportType.Exempt)
            }
            player.bas = previous
            rebuildAppearance()
        }
    }

    private suspend fun ProtectedAccess.climb(move: ObstacleMove) {
        faceSquare(move.dest)
        anim(move.seqName)
        delay(move.ticks)
        telejump(move.dest, TeleportType.Exempt)
    }

    private suspend fun ProtectedAccess.leap(move: ObstacleMove) {
        anim(move.seqName)
        exactMove(
            start = coords,
            end = move.dest,
            delay1 = 0,
            delay2 = move.ticks * CLIENT_CYCLES_PER_TICK,
            dir = faceAngle(coords, move.dest),
            teleportType = TeleportType.Exempt,
        )
        delay(move.ticks)
    }

    /** Records [obstacle] in the lap; returns `true` when it finished an in-order lap. */
    private fun Player.advanceLap(obstacle: AgilityObstacle, lastStep: Int): Boolean {
        val courseId = obstacle.course.rowId
        val inOrder = obstacle.step == 0 || (lapCourse == courseId && lapStep == obstacle.step)
        if (!inOrder || obstacle.step == lastStep) {
            lapCourse = 0
            lapStep = 0
            return inOrder
        }
        lapCourse = courseId
        lapStep = obstacle.step + 1
        return false
    }

    private val ObstacleMove.seqName: String
        get() = RSCM.getReverseMapping(RSCMType.SEQ, seq.id)

    private fun CoordGrid.stepTowards(dest: CoordGrid): CoordGrid =
        translate((dest.x - x).sign, (dest.z - z).sign)

    private fun faceAngle(from: CoordGrid, to: CoordGrid): Int {
        val dx = (to.x - from.x).sign
        val dz = (to.z - from.z).sign
        return when {
            dx == 0 && dz > 0 -> constants.em_face_north
            dx > 0 && dz > 0 -> constants.em_face_northeast
            dx > 0 && dz == 0 -> constants.em_face_east
            dx > 0 && dz < 0 -> constants.em_face_southeast
            dx == 0 && dz < 0 -> constants.em_face_south
            dx < 0 && dz < 0 -> constants.em_face_southwest
            dx < 0 && dz == 0 -> constants.em_face_west
            else -> constants.em_face_northwest
        }
    }

    private companion object {
        const val AGILITY = "stat.agility"
        const val CLIENT_CYCLES_PER_TICK = 30

        var Player.lapCourse by intVarBit("varbit.agility_lap_course")
        var Player.lapStep by intVarBit("varbit.agility_lap_step")
    }
}
