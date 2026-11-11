package org.rsmod.content.skills.agility

import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import dev.openrune.types.BasType
import jakarta.inject.Inject
import kotlin.math.sign
import org.rsmod.api.player.hook.TeleportType
import org.rsmod.api.player.output.ChatType
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.player.stat.baseAgilityLvl
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onApLoc1
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.table.QuestRow
import org.rsmod.api.table.agility.AgilityCoursesRow
import org.rsmod.content.quest.manager.QuestRequirements
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.util.EntityExactMove
import org.rsmod.game.entity.util.EntityFaceAngle
import org.rsmod.game.hit.HitType
import org.rsmod.game.type.getOrNull
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Runs every obstacle in `dbtable.agility_obstacles`: op1 (or ap1 within its range) on one of its
 * locs walks the player to the lane start and plays the lane tick by tick, granting the xp when it
 * lands, and tracks the lap. A failable obstacle rolls its success curve first and plays its fall
 * instead on a miss; the lap carries on once the obstacle is crossed. A lap passed in order counts
 * towards the course's lap count, pays its lap bonus and rolls for a mark of grace
 * (https://oldschool.runescape.wiki/w/Agility). The client's agility helper is fed as it is live.
 */
class AgilityCourseScript
@Inject
constructor(private val marks: MarksOfGrace, private val objRepo: ObjRepository) :
    PluginScript() {
    override fun ScriptContext.startup() {
        val obstacles = AgilityObstacle.loadAll()
        val laps = CourseLaps(obstacles)
        val byLoc = obstacles.flatMap { obstacle -> obstacle.locs.map { it to obstacle } }
        for ((loc, candidates) in byLoc.groupBy({ it.first.id }, { it.second })) {
            val type = candidates.first().locs.first { it.id == loc }
            onOpLoc1(type) { cross(candidates, laps) }
            val apRange = candidates.mapNotNull { it.apRange }.maxOrNull() ?: continue
            onApLoc1(type) {
                if (isWithinApRange(it.loc, apRange)) {
                    cross(candidates, laps)
                }
            }
        }
    }

    private suspend fun ProtectedAccess.cross(candidates: List<AgilityObstacle>, laps: CourseLaps) {
        val (obstacle, laneIndex) =
            choose(candidates, laps)
                ?: run {
                    // UNVERIFIED: wording guessed.
                    mes("You can't do that from here.")
                    return
                }
        if (!canAttempt(obstacle)) {
            return
        }
        val course = obstacle.course
        val lane = obstacle.lanes[laneIndex]
        lane.opMessages.forEach { mes(it.text, ChatType.Spam) }
        arriveDelay()
        if (obstacle.step == 0) {
            player.helperHighlightedCourse = course.helperCourse
            // UNVERIFIED: captures show only the highlighted course changing; live likely sets the
            // current course on entering the course, which the helper's lap scripts need.
            player.helperCurrentCourse = course.helperCourse
        }
        walkToStart(lane.start)
        val success = obstacle.success
        if (success != null && !statRandom(AGILITY, success.low, success.high, 0)) {
            play(obstacle.failLanes[laneIndex], land = {}, hit = { fall(obstacle) })
            return
        }
        play(lane, land = { land(obstacle, laps) }, hit = {})
    }

    /**
     * The obstacle among those sharing the clicked loc that the player can start from where they
     * stand, preferring the one that carries their lap on, then the nearest, and the lane they
     * start.
     */
    private fun ProtectedAccess.choose(
        candidates: List<AgilityObstacle>,
        laps: CourseLaps,
    ): Pair<AgilityObstacle, Int>? {
        val startable =
            candidates.mapNotNull { obstacle ->
                obstacle.laneFrom(coords).takeIf { it >= 0 }?.let { obstacle to it }
            }
        return startable.firstOrNull { (obstacle, _) ->
            laps.continues(player.lapCourse, player.lapStep, obstacle)
        }
            ?: startable.minByOrNull { (obstacle, lane) ->
                coords.chebyshevDistance(obstacle.lanes[lane].start)
            }
    }

    private fun ProtectedAccess.canAttempt(obstacle: AgilityObstacle): Boolean {
        val course = obstacle.course
        if (player.agilityLvl < course.level) {
            // UNVERIFIED: wording guessed.
            mes("You need an Agility level of ${course.level} to attempt this course.")
            return false
        }
        if (course.levelUnboostable == true && player.baseAgilityLvl < course.level) {
            mes("Your Agility boost won't help you here.")
            return false
        }
        val quest = course.quest
        if (quest != null && !hasQuest(quest, startedOnly = course.questStarted == true)) {
            // UNVERIFIED: wording guessed.
            mes("You need to have done more of ${quest.displayname} to attempt this course.")
            return false
        }
        val requirement = obstacle.requirement ?: return true
        if (!wears(requirement, obstacle.requirementObjs)) {
            mes(requirement.refusal())
            return false
        }
        return true
    }

    private fun ProtectedAccess.hasQuest(quest: QuestRow, startedOnly: Boolean): Boolean {
        val key = RSCM.getReverseMapping(RSCMType.DBROW, quest.rowId).substringAfter('.')
        return QuestRequirements.hasCompleted(player, key) ||
            (startedOnly && QuestRequirements.isOnQuest(player, key))
    }

    /** Whether the player wears one of [objs], with a crossbow too where [requirement] says. */
    private fun ProtectedAccess.wears(requirement: WornRequirement, objs: List<String>): Boolean {
        val wearsObj = objs.any { worn.count(it) > 0 }
        return when (requirement) {
            WornRequirement.NinjaGreegree -> wearsObj
            WornRequirement.CrossbowGrapple -> {
                val weapon = getOrNull(player.righthand)
                wearsObj && weapon != null && weapon.isCategoryType(CROSSBOW)
            }
        }
    }

    // UNVERIFIED: the crossbow and grapple wording is guessed.
    private fun WornRequirement.refusal(): String =
        when (this) {
            WornRequirement.NinjaGreegree ->
                "Only the stealthiest and most agile monkey can use this!"
            WornRequirement.CrossbowGrapple ->
                "You need a crossbow and a mithril grapple equipped to do that."
        }

    private suspend fun ProtectedAccess.walkToStart(start: CoordGrid) {
        while (coords != start) {
            teleport(coords.stepTowards(start), TeleportType.Exempt)
            delay(1)
        }
    }

    private suspend fun ProtectedAccess.play(
        lane: ObstacleLane,
        land: () -> Unit,
        hit: () -> Unit,
    ) {
        val actionsByTick = lane.actions.groupBy { it.tick }
        val messagesByTick = lane.messages.groupBy { it.tick }
        val previousBas = player.bas
        var finished = false
        player.busy = true
        try {
            for (tick in 0..lane.lastTick) {
                messagesByTick[tick]?.forEach { mes(it.text, ChatType.Spam) }
                val actions = actionsByTick[tick].orEmpty()
                for (action in actions) {
                    when (action.op) {
                        ActionOp.Land -> land()
                        ActionOp.Hit -> hit()
                        else -> perform(action, actions, previousBas)
                    }
                }
                if (tick < lane.lastTick) {
                    delay(1)
                }
            }
            finished = true
        } finally {
            if (!finished && coords != lane.end) {
                telejump(lane.end, TeleportType.Exempt)
            }
            if (player.bas !== previousBas) {
                player.bas = previousBas
                rebuildAppearance()
            }
            player.busy = false
        }
    }

    private fun ProtectedAccess.perform(
        action: ObstacleAction,
        tickActions: List<ObstacleAction>,
        previousBas: BasType?,
    ) {
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
            ActionOp.ExactMove -> {
                val path = tickActions.firstOrNull { it.op == ActionOp.ExactPath }
                exactMove(action, from = path?.coord ?: coords, to = path?.ref?.let(::CoordGrid))
            }
            ActionOp.Telejump -> telejump(action.coord, TeleportType.Exempt)
            ActionOp.FaceAngle -> player.pendingFaceAngle = EntityFaceAngle(action.ref)
            ActionOp.ExactPath,
            ActionOp.Land,
            ActionOp.Hit -> Unit
        }
    }

    /**
     * Puts the player on the move's tile and has the client draw them gliding from [from] to [to]
     * (the tile itself unless the lane recorded another), as live draws some obstacles a tile or
     * two off where the player ends up.
     */
    private fun ProtectedAccess.exactMove(move: ObstacleAction, from: CoordGrid, to: CoordGrid?) {
        val dest = move.coord
        val end = to ?: dest
        telejump(dest, TeleportType.Exempt)
        player.pendingExactMove =
            EntityExactMove(
                deltaX1 = from.x - dest.x,
                deltaZ1 = from.z - dest.z,
                deltaX2 = end.x - dest.x,
                deltaZ2 = end.z - dest.z,
                clientDelay1 = move.arg1,
                clientDelay2 = move.arg2,
                direction = move.ref,
            )
    }

    private fun ProtectedAccess.fall(obstacle: AgilityObstacle) {
        val damage = obstacle.fallDamage ?: return
        val share = player.hitpoints * damage.hpNumerator / damage.hpDenominator
        queueHit(1, HitType.Typeless, random.of(damage.min, damage.max) + share)
    }

    private fun ProtectedAccess.land(obstacle: AgilityObstacle, laps: CourseLaps) {
        val course = obstacle.course
        val lapDone = player.advanceLap(obstacle, laps)
        if (obstacle.step == 0) {
            runClientScript(START_LAP.asRSCM(RSCMType.CLIENTSCRIPT), player.currentMapClock)
        }
        if (lapDone) {
            player.helperHighlightedCourse = 0
            val count = player.addLap(course)
            mes("Your ${course.name} lap count is: <col=ff0000>$count</col>.")
        }
        val xpTenths = obstacle.xpTenthsFor(player)
        if (xpTenths > 0) {
            runClientScript(ADD_XP.asRSCM(RSCMType.CLIENTSCRIPT), xpTenths)
            statAdvance(AGILITY, xpTenths / 10.0)
        }
        obstacle.landSpawn?.let { spawn(it) }
        if (!lapDone) {
            return
        }
        if (course.lapBonusXp > 0) {
            runClientScript(ADD_XP.asRSCM(RSCMType.CLIENTSCRIPT), course.lapBonusXp)
            statAdvance(AGILITY, course.lapBonusXp / 10.0)
        }
        val statXp = course.lapStatXp
        if (statXp.size == 2) {
            statAdvance(RSCM.getReverseMapping(RSCMType.STAT, statXp[0]), statXp[1] / 10.0)
        }
        runClientScript(END_LAP.asRSCM(RSCMType.CLIENTSCRIPT), player.currentMapClock, 0)
        marks.rollLap(this, course)
    }

    private fun ProtectedAccess.spawn(spawn: LandSpawn) {
        if (invTotal(inv, spawn.obj) > 0) {
            return
        }
        objRepo.add(spawn.obj, random.pick(spawn.tiles), SPAWN_TICKS, player, reveal = NEVER_REVEAL)
    }

    private fun AgilityObstacle.xpTenthsFor(player: Player): Int {
        val diary = diaryXp ?: return xpTenths
        return if (player.vars[diary.varbit] != 0) diary.xpTenths else xpTenths
    }

    /**
     * Records [obstacle] in the lap; returns `true` when it finished an in-order lap. A way back
     * after a fall leaves the lap as it was.
     */
    private fun Player.advanceLap(obstacle: AgilityObstacle, laps: CourseLaps): Boolean {
        if (obstacle.isRecovery) {
            return false
        }
        val courseId = obstacle.course.rowId
        val inOrder = obstacle.step == 0 || laps.continues(lapCourse, lapStep, obstacle)
        if (!inOrder || obstacle.step == laps.lastStep(courseId)) {
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

    private fun CoordGrid.stepTowards(dest: CoordGrid): CoordGrid =
        translate((dest.x - x).sign, (dest.z - z).sign)

    private companion object {
        const val AGILITY = "stat.agility"
        const val START_LAP = "clientscript.[clientscript,helper_agility_start_lap]"
        const val ADD_XP = "clientscript.[clientscript,helper_agility_add_xp]"
        const val END_LAP = "clientscript.[clientscript,helper_agility_end_lap]"
        const val CROSSBOW = "category.crossbow"
        const val SPAWN_TICKS = 150
        const val NEVER_REVEAL = SPAWN_TICKS + 1

        var Player.lapCourse by intVarBit("varbit.agility_lap_course")
        var Player.lapStep by intVarBit("varbit.agility_lap_step")
        var Player.busy by boolVarBit("varbit.busy")
        var Player.helperHighlightedCourse by intVarBit("varbit.helper_agility_highlighted_course")
        var Player.helperCurrentCourse by intVarBit("varbit.helper_agility_current_course")
    }
}
