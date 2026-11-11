package org.rsmod.content.skills.agility

import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseAgilityLvl
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.table.agility.AgilityCoursesRow
import org.rsmod.game.entity.Player

/**
 * Rolls a mark of grace for a finished lap (https://oldschool.runescape.wiki/w/Mark_of_Grace). A
 * roll only happens once the timer from the last spawn has run out; it counts real minutes, so it
 * survives logging out and changing worlds. A course's diary can reroll a miss or shorten the
 * timer. The mark is private to the player.
 */
class MarksOfGrace @Inject constructor(private val objRepo: ObjRepository) {
    internal fun rollLap(access: ProtectedAccess, course: AgilityCoursesRow) {
        val tiles = course.markTiles
        if (tiles.isEmpty()) {
            return
        }
        val player = access.player
        val now = currentMinute()
        if (now < player.markReadyMinute) {
            return
        }
        if (!rolls(access, course)) {
            return
        }
        objRepo.add(MARK, access.random.pick(tiles), DESPAWN_TICKS, player, reveal = NEVER_REVEAL)
        player.markReadyMinute = now + timerMinutes(access, course)
    }

    private fun rolls(access: ProtectedAccess, course: AgilityCoursesRow): Boolean {
        val (numerator, denominator) = course.markChance
        val penalty =
            course.markLevelPenalty && access.player.baseAgilityLvl >= course.level + PENALTY_GAP
        if (access.random.of(denominator * PENALTY_FACTOR) < chance(numerator, penalty)) {
            return true
        }
        val reroll =
            course.markRerolls
                .chunked(2)
                .filter { (varbit, _) -> access.player.hasCompleted(varbit) }
                .maxOfOrNull { (_, percent) -> percent } ?: return false
        return access.random.of(100) < reroll
    }

    private fun timerMinutes(access: ProtectedAccess, course: AgilityCoursesRow): Int {
        val shortTimer = course.markShortTimer
        if (shortTimer.size != 2 || !access.player.hasCompleted(shortTimer[0])) {
            return TIMER_MINUTES
        }
        return if (access.random.of(shortTimer[1]) == 0) SHORT_TIMER_MINUTES else TIMER_MINUTES
    }

    private fun Player.hasCompleted(varbit: Int): Boolean =
        vars[RSCM.getReverseMapping(RSCMType.VARBIT, varbit)] != 0

    internal companion object {
        const val MARK = "obj.grace"
        const val TIMER_MINUTES = 3
        const val SHORT_TIMER_MINUTES = 2
        const val DESPAWN_TICKS = 1000
        const val PENALTY_GAP = 20
        const val PENALTY_FACTOR = 5
        private const val NEVER_REVEAL = DESPAWN_TICKS + 1
        private const val MINUTE_MILLIS = 60_000L

        private var Player.markReadyMinute by intVarp("varp.agility_mark_ready_minute")

        /**
         * A roll succeeds below this out of `denominator * PENALTY_FACTOR`: the course chance, or
         * a fifth of it (80% lower) under the level penalty.
         */
        fun chance(numerator: Int, penalty: Boolean): Int =
            if (penalty) numerator else numerator * PENALTY_FACTOR

        fun currentMinute(): Int = (System.currentTimeMillis() / MINUTE_MILLIS).toInt()
    }
}
