package org.rsmod.content.skills.agility

import dev.openrune.types.ObjectServerType
import dev.openrune.types.SequenceServerType
import org.rsmod.api.table.agility.AgilityCoursesRow
import org.rsmod.api.table.agility.AgilityObstaclesRow
import org.rsmod.map.CoordGrid

internal class AgilityObstacle(
    val course: AgilityCoursesRow,
    val step: Int,
    val locs: List<ObjectServerType>,
    val xp: Double,
    val lanes: List<ObstacleLane>,
    val startMessage: String?,
    val endMessage: String?,
) {
    fun laneFrom(coords: CoordGrid): ObstacleLane? =
        lanes.filter { it.canStartFrom(coords) }.minByOrNull { coords.chebyshevDistance(it.start) }

    companion object {
        fun loadAll(): List<AgilityObstacle> = AgilityObstaclesRow.all().map(::from)

        private fun from(row: AgilityObstaclesRow): AgilityObstacle {
            val moves =
                row.moves.groupBy(
                    keySelector = { it.t0 },
                    valueTransform = { ObstacleMove(MoveKind.of(it.t1), it.t2, it.t3, it.t4) },
                )
            val lanes = row.lanes.map { (index, start) -> ObstacleLane(start, moves.getValue(index)) }
            return AgilityObstacle(
                course = row.course,
                step = row.step,
                locs = row.locs,
                xp = row.xp / 10.0,
                lanes = lanes,
                startMessage = row.startMessage,
                endMessage = row.endMessage,
            )
        }
    }
}

internal class ObstacleLane(val start: CoordGrid, val moves: List<ObstacleMove>) {
    val end: CoordGrid
        get() = moves.last().dest

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

internal class ObstacleMove(
    val kind: MoveKind,
    val dest: CoordGrid,
    val seq: SequenceServerType,
    val ticks: Int,
)

internal enum class MoveKind(val id: Int) {
    Walk(0),
    Climb(1),
    Leap(2);

    companion object {
        fun of(id: Int): MoveKind = entries.first { it.id == id }
    }
}
