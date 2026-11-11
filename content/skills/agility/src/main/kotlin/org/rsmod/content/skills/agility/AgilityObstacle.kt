package org.rsmod.content.skills.agility

import dev.openrune.types.ObjectServerType
import org.rsmod.api.table.agility.AgilityCoursesRow
import org.rsmod.api.table.agility.AgilityObstaclesRow
import org.rsmod.map.CoordGrid

internal class AgilityObstacle(
    val course: AgilityCoursesRow,
    val step: Int,
    val locs: List<ObjectServerType>,
    val xpTenths: Int,
    val lanes: List<ObstacleLane>,
    val startMessage: String?,
    val endMessage: String?,
) {
    fun laneFrom(coords: CoordGrid): ObstacleLane? =
        lanes.filter { it.canStartFrom(coords) }.minByOrNull { coords.chebyshevDistance(it.start) }

    companion object {
        fun loadAll(): List<AgilityObstacle> = AgilityObstaclesRow.all().map(::from)

        private fun from(row: AgilityObstaclesRow): AgilityObstacle {
            val actions =
                row.actions.groupBy(
                    keySelector = { it.t0 },
                    valueTransform = {
                        ObstacleAction(it.t1, ActionOp.of(it.t2), it.t3, it.t4, it.t5, it.t6)
                    },
                )
            val lanes =
                row.lanes.map { (index, start) ->
                    ObstacleLane(start, actions.getValue(index).sortedBy { it.tick })
                }
            return AgilityObstacle(
                course = row.course,
                step = row.step,
                locs = row.locs,
                xpTenths = row.xp,
                lanes = lanes,
                startMessage = row.startMessage,
                endMessage = row.endMessage,
            )
        }
    }
}

internal class ObstacleLane(val start: CoordGrid, val actions: List<ObstacleAction>) {
    val lastTick: Int = actions.maxOf { it.tick }

    val end: CoordGrid = actions.lastOrNull { it.op.moves }?.coord ?: start

    /**
     * Whether [coords] is on this lane's start side: on its level, within [MAX_START_DISTANCE] of
     * the start and, when the landing is on the same level, nearer the start than the landing. This
     * keeps every obstacle one way, so a click from a landing tile beside the loc does nothing.
     */
    fun canStartFrom(coords: CoordGrid): Boolean {
        if (coords.level != start.level) {
            return false
        }
        val toStart = coords.chebyshevDistance(start)
        if (toStart > MAX_START_DISTANCE) {
            return false
        }
        return end.level != coords.level || toStart < coords.chebyshevDistance(end)
    }

    private companion object {
        const val MAX_START_DISTANCE = 2
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
    Land(9);

    companion object {
        fun of(id: Int): ActionOp = entries.first { it.id == id }
    }
}
