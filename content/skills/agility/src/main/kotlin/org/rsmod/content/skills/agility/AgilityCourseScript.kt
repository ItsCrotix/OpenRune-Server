package org.rsmod.content.skills.agility

import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import dev.openrune.types.BasType
import jakarta.inject.Inject
import kotlin.math.sign
import org.rsmod.api.config.constants
import org.rsmod.api.player.hook.TeleportType
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.table.agility.AgilityCoursesRow
import org.rsmod.game.entity.Player
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Runs every obstacle in `dbtable.agility_obstacles`: op1 on one of its locs plays the lane's
 * actions tick by tick, granting the xp when it lands, and tracks the lap. A lap passed in order
 * counts towards the course's lap count, pays its lap bonus and rolls for a mark of grace
 * (https://oldschool.runescape.wiki/w/Agility). The client's agility helper is fed as it is live.
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
        if (obstacle.step == 0) {
            player.helperHighlightedCourse = course.helperCourse
            // UNVERIFIED: a capture shows only the highlighted course changing; live likely sets
            // the current course on entering the course, which the helper's lap scripts need.
            player.helperCurrentCourse = course.helperCourse
        }
        obstacle.startMessage?.let(::mes)
        if (coords != lane.start) {
            teleport(lane.start, TeleportType.Exempt)
            delay(1)
        }
        play(lane) { land(obstacle, lastStep) }
    }

    private suspend fun ProtectedAccess.play(lane: ObstacleLane, land: () -> Unit) {
        val byTick = lane.actions.groupBy { it.tick }
        val previousBas = player.bas
        var landed = false
        player.busy = true
        try {
            for (tick in 0..lane.lastTick) {
                for (action in byTick[tick].orEmpty()) {
                    if (action.op == ActionOp.Land) {
                        land()
                        landed = true
                    } else {
                        perform(action, previousBas)
                    }
                }
                if (tick < lane.lastTick) {
                    delay(1)
                }
            }
        } finally {
            if (!landed && coords != lane.end) {
                telejump(lane.end, TeleportType.Exempt)
            }
            if (player.bas !== previousBas) {
                player.bas = previousBas
                rebuildAppearance()
            }
            player.busy = false
        }
    }

    private fun ProtectedAccess.perform(action: ObstacleAction, previousBas: BasType?) {
        when (action.op) {
            ActionOp.Step -> teleport(action.coord, TeleportType.Exempt)
            ActionOp.Face -> faceSquare(action.coord)
            ActionOp.Anim -> anim(RSCM.getReverseMapping(RSCMType.SEQ, action.ref), action.arg1)
            ActionOp.ResetAnim -> resetAnim()
            ActionOp.Sound -> {
                val synth = RSCM.getReverseMapping(RSCMType.SYNTH, action.ref)
                soundSynth(synth, loops = action.arg1, delay = action.arg2)
            }
            ActionOp.Balance -> {
                val seq = action.ref
                player.bas = BasType(-1, seq, seq, seq, seq, seq, seq, seq)
                rebuildAppearance()
            }
            ActionOp.BalanceOff -> {
                player.bas = previousBas
                rebuildAppearance()
            }
            ActionOp.ExactMove ->
                exactMove(
                    start = coords,
                    end = action.coord,
                    delay1 = action.arg1,
                    delay2 = action.arg2,
                    dir = faceAngle(coords, action.coord),
                    teleportType = TeleportType.Exempt,
                )
            ActionOp.Telejump -> telejump(action.coord, TeleportType.Exempt)
            ActionOp.Land -> error("Landing is handled by the lane player.")
        }
    }

    private fun ProtectedAccess.land(obstacle: AgilityObstacle, lastStep: Int) {
        obstacle.endMessage?.let(::mes)
        val course = obstacle.course
        val lapDone = player.advanceLap(obstacle, lastStep)
        if (obstacle.step == 0) {
            runClientScript(START_LAP.asRSCM(RSCMType.CLIENTSCRIPT), player.currentMapClock)
        }
        if (lapDone) {
            player.helperHighlightedCourse = 0
            val laps = player.addLap(course)
            mes("Your ${course.name} lap count is: <col=ff0000>$laps</col>.")
        }
        runClientScript(ADD_XP.asRSCM(RSCMType.CLIENTSCRIPT), obstacle.xpTenths)
        statAdvance(AGILITY, obstacle.xpTenths / 10.0)
        if (!lapDone) {
            return
        }
        if (course.lapBonusXp > 0) {
            runClientScript(ADD_XP.asRSCM(RSCMType.CLIENTSCRIPT), course.lapBonusXp)
            statAdvance(AGILITY, course.lapBonusXp / 10.0)
        }
        runClientScript(END_LAP.asRSCM(RSCMType.CLIENTSCRIPT), player.currentMapClock, 0)
        marks.rollLap(this, course)
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

    private fun Player.addLap(course: AgilityCoursesRow): Int {
        val varp = RSCM.getReverseMapping(RSCMType.VARP, course.lapCount)
        val laps = vars[varp] + 1
        VarPlayerIntMapSetter.set(this, varp, laps)
        return laps
    }

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
        const val START_LAP = "clientscript.[clientscript,helper_agility_start_lap]"
        const val ADD_XP = "clientscript.[clientscript,helper_agility_add_xp]"
        const val END_LAP = "clientscript.[clientscript,helper_agility_end_lap]"

        var Player.lapCourse by intVarBit("varbit.agility_lap_course")
        var Player.lapStep by intVarBit("varbit.agility_lap_step")
        var Player.busy by boolVarBit("varbit.busy")
        var Player.helperHighlightedCourse by intVarBit("varbit.helper_agility_highlighted_course")
        var Player.helperCurrentCourse by intVarBit("varbit.helper_agility_current_course")
    }
}
