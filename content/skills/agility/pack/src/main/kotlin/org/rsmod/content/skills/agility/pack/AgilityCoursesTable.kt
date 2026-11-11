package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.dbTable
import dev.openrune.definition.util.VarType
import org.rsmod.map.CoordGrid

object AgilityCoursesTable {
    const val LEVEL = 0
    const val LAP_BONUS_XP = 1
    const val MARK_TILES = 2

    fun courses() =
        dbTable("dbtable.agility_courses", serverOnly = true) {
            column("level", LEVEL, VarType.INT)
            column("lap_bonus_xp", LAP_BONUS_XP, VarType.INT)
            column("mark_tiles", MARK_TILES, VarType.COORDGRID)

            row("dbrow.agility_course_gnome_stronghold") {
                column(LEVEL, 1)
                column(LAP_BONUS_XP, 500)
                // UNVERIFIED: the wiki only says "before climbing down the tree branch".
                column(MARK_TILES, CoordGrid(2484, 3419, 2).packed)
            }

            row("dbrow.agility_course_draynor") {
                column(LEVEL, 1)
                column(LAP_BONUS_XP, 0)
                // UNVERIFIED: one tile on each roof the wiki names (start, Diango's workshop,
                // Slayer shop, Ned's rope shop).
                column(
                    MARK_TILES,
                    CoordGrid(3100, 3279, 3).packed,
                    CoordGrid(3089, 3274, 3).packed,
                    CoordGrid(3090, 3255, 3).packed,
                    CoordGrid(3098, 3259, 3).packed,
                )
            }
        }
}
