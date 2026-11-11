package org.rsmod.content.skills.agility

import jakarta.inject.Inject
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseAgilityLvl
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.table.agility.AgilityCoursesRow
import org.rsmod.game.entity.Player

/**
 * Rolls a mark of grace for a finished lap (https://oldschool.runescape.wiki/w/Mark_of_Grace). A
 * roll only happens once [TIMER_MINUTES] real minutes have started since the last mark spawned, so
 * the timer survives logging out and changing worlds. The mark is private to the player.
 */
class MarksOfGrace @Inject constructor(private val objRepo: ObjRepository) {
    internal fun rollLap(access: ProtectedAccess, course: AgilityCoursesRow) {
        val tiles = course.markTiles
        if (tiles.isEmpty()) {
            return
        }
        val player = access.player
        val now = currentMinute()
        if (!isTimerExpired(player.markMinute, now)) {
            return
        }
        val chance = chance(player.baseAgilityLvl, course.level)
        if (access.random.of(CHANCE_DENOMINATOR) >= chance) {
            return
        }
        val tile = access.random.pick(tiles)
        objRepo.add(MARK, tile, DESPAWN_TICKS, receiver = player, reveal = NEVER_REVEAL)
        player.markMinute = now
    }

    internal companion object {
        const val MARK = "obj.grace"
        const val TIMER_MINUTES = 3
        const val DESPAWN_TICKS = 1000
        const val CHANCE_DENOMINATOR = 15
        const val BASE_CHANCE = 5
        const val REDUCED_CHANCE = 1
        const val REDUCED_LEVEL_GAP = 20
        private const val NEVER_REVEAL = DESPAWN_TICKS + 1
        private const val MINUTE_MILLIS = 60_000L

        private var Player.markMinute by intVarp("varp.agility_mark_minute")

        fun isTimerExpired(lastMinute: Int, nowMinute: Int): Boolean =
            nowMinute - lastMinute >= TIMER_MINUTES

        fun chance(baseLevel: Int, courseLevel: Int): Int =
            if (baseLevel >= courseLevel + REDUCED_LEVEL_GAP) REDUCED_CHANCE else BASE_CHANCE

        fun currentMinute(): Int = (System.currentTimeMillis() / MINUTE_MILLIS).toInt()
    }
}
