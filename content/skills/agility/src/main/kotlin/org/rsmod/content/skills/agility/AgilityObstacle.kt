package org.rsmod.content.skills.agility

import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCMType
import dev.openrune.types.ObjectServerType
import org.rsmod.api.table.Tuple7
import org.rsmod.api.table.agility.AgilityCoursesRow
import org.rsmod.api.table.agility.AgilityObstaclesRow
import org.rsmod.map.CoordGrid

internal class AgilityObstacle(
    val course: AgilityCoursesRow,
    val step: Int,
    val locs: List<ObjectServerType>,
    val xpTenths: Int,
    val lanes: List<ObstacleLane>,
    val failLanes: List<ObstacleLane>,
    val success: SuccessCurve?,
    val fallDamage: FallDamage?,
    val diaryXp: DiaryXp?,
    val apRange: Int?,
    val requirement: WornRequirement?,
    val requirementObjs: List<String>,
    val landSpawn: LandSpawn?,
) {
    /** Whether this is a way back onto the course after a fall rather than part of the lap. */
    val isRecovery: Boolean
        get() = step == RECOVERY_STEP

    /**
     * The index of the lane [coords] may start whose start tile is nearest, or `-1`. No landing on
     * its level may be nearer [coords] than that start, so a click from the far side of the
     * obstacle never carries the player back over.
     */
    fun laneFrom(coords: CoordGrid): Int {
        val nearestEnd =
            lanes.filter { it.end.level == coords.level }.minOfOrNull {
                coords.chebyshevDistance(it.end)
            }
        val starts =
            lanes.withIndex().filter { (_, lane) ->
                lane.canStartFrom(coords) &&
                    (nearestEnd == null || coords.chebyshevDistance(lane.start) <= nearestEnd)
            }
        return starts.minByOrNull { (_, lane) -> coords.chebyshevDistance(lane.start) }?.index ?: -1
    }

    companion object {
        private const val RECOVERY_STEP = -1

        fun loadAll(): List<AgilityObstacle> = AgilityObstaclesRow.all().map(::from)

        private fun from(row: AgilityObstaclesRow): AgilityObstacle {
            val messages =
                row.messages.groupBy(
                    keySelector = { it.t0 },
                    valueTransform = { LaneMessage(it.t1, it.t2) },
                )
            val actions = actionsByLane(row.actions)
            val lanes =
                row.lanes.map { (index, start) ->
                    ObstacleLane(start, actions.getValue(index), messages[index].orEmpty())
                }
            val failActions = actionsByLane(row.failActions)
            val failLanes =
                failActions.entries
                    .sortedBy { it.key }
                    .map { (index, laneActions) ->
                        ObstacleLane(lanes[index].start, laneActions, emptyList())
                    }
            val success = row.success
            val damage = row.failDamage
            val diary = row.diaryXp
            return AgilityObstacle(
                course = row.course,
                step = row.step,
                locs = row.locs,
                xpTenths = row.xp,
                lanes = lanes,
                failLanes = failLanes,
                success = if (success.size == 2) SuccessCurve(success[0], success[1]) else null,
                fallDamage =
                    if (damage.size == 4) {
                        FallDamage(damage[0], damage[1], damage[2], damage[3])
                    } else {
                        null
                    },
                diaryXp =
                    if (diary.size == 2) {
                        DiaryXp(RSCM.getReverseMapping(RSCMType.VARBIT, diary[0]), diary[1])
                    } else {
                        null
                    },
                apRange = row.apRange,
                requirement = row.requirement?.let(WornRequirement::of),
                requirementObjs = row.requirementObjs.map { it.internalName },
                landSpawn =
                    row.landSpawnObj?.let { LandSpawn(it.internalName, row.landSpawnTiles) },
            )
        }

        private fun actionsByLane(
            rows: List<Tuple7<Int, Int, Int, CoordGrid, Int, Int, Int>>
        ): Map<Int, List<ObstacleAction>> =
            rows
                .groupBy(
                    keySelector = { it.t0 },
                    valueTransform = {
                        ObstacleAction(it.t1, ActionOp.of(it.t2), it.t3, it.t4, it.t5, it.t6)
                    },
                )
                .mapValues { (_, it) -> it.sortedBy { action -> action.tick } }
    }
}

internal class SuccessCurve(val low: Int, val high: Int)

/** A roll in [min]..[max] plus [hpNumerator]/[hpDenominator] of the current hitpoints. */
internal class FallDamage(
    val min: Int,
    val max: Int,
    val hpNumerator: Int,
    val hpDenominator: Int,
)

/** A private [obj] dropped on one of [tiles] when the obstacle lands, as the Werewolf stick. */
internal class LandSpawn(val obj: String, val tiles: List<CoordGrid>)

/** Mirrors the `REQUIREMENT_*` ids of the pack's `AgilityObstaclesTable`. */
internal enum class WornRequirement(val id: Int) {
    NinjaGreegree(1),
    CrossbowGrapple(2);

    companion object {
        fun of(id: Int): WornRequirement = entries.first { it.id == id }
    }
}

internal class DiaryXp(val varbit: String, val xpTenths: Int)

internal class LaneMessage(val tick: Int, val text: String)

internal class ObstacleLane(
    val start: CoordGrid,
    val actions: List<ObstacleAction>,
    val messages: List<LaneMessage>,
) {
    val lastTick: Int = maxOf(actions.maxOf { it.tick }, messages.maxOfOrNull { it.tick } ?: 0)

    val end: CoordGrid = actions.lastOrNull { it.op.moves }?.coord ?: start

    val opMessages: List<LaneMessage> = messages.filter { it.tick == OP_TICK }

    /** Whether [coords] is on the start's level and within [MAX_START_DISTANCE] of it. */
    fun canStartFrom(coords: CoordGrid): Boolean =
        coords.level == start.level && coords.chebyshevDistance(start) <= MAX_START_DISTANCE

    private companion object {
        const val MAX_START_DISTANCE = 4
        const val OP_TICK = -1
    }
}

internal class ObstacleAction(
    val tick: Int,
    val op: ActionOp,
    val coord: CoordGrid,
    val ref: Int,
    val arg1: Int,
    val arg2: Int,
)

/** Mirrors the `ACTION_*` ids of the pack's `AgilityObstaclesTable`. */
internal enum class ActionOp(val id: Int, val moves: Boolean = false) {
    Step(0, moves = true),
    Face(1),
    Anim(2),
    ResetAnim(3),
    Sound(4),
    Balance(5),
    BalanceOff(6),
    ExactMove(7, moves = true),
    Telejump(8, moves = true),
    Land(9),
    Hit(10),
    ExactPath(11),
    FaceAngle(12);

    companion object {
        fun of(id: Int): ActionOp = entries.first { it.id == id }
    }
}
