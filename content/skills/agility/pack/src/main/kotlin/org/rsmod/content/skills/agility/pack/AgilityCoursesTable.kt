package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.dbTable
import dev.openrune.definition.util.VarType
import org.rsmod.map.CoordGrid

/**
 * One row per course. `name` is the course as the lap count message names it; `helper_course` is
 * its id in the client's agility helper (the keys of enum 3997, whose values list each course's
 * obstacle locs, e.g. `enum.gnome_stronghold_course_obstacles`).
 */
object AgilityCoursesTable {
    const val LEVEL = 0
    const val LAP_BONUS_XP = 1
    const val MARK_TILES = 2
    const val NAME = 3
    const val LAP_COUNT = 4
    const val HELPER_COURSE = 5

    fun courses() =
        dbTable("dbtable.agility_courses", serverOnly = true) {
            column("level", LEVEL, VarType.INT)
            column("lap_bonus_xp", LAP_BONUS_XP, VarType.INT)
            column("mark_tiles", MARK_TILES, VarType.COORDGRID)
            column("name", NAME, VarType.STRING)
            column("lap_count", LAP_COUNT, VarType.VARP)
            column("helper_course", HELPER_COURSE, VarType.INT)

            row("dbrow.agility_course_gnome_stronghold") {
                column(LEVEL, 1)
                column(LAP_BONUS_XP, 500)
                // UNVERIFIED: the wiki only says "before climbing down the tree branch".
                column(MARK_TILES, CoordGrid(2484, 3419, 2).packed)
                // UNVERIFIED: RuneLite's lap count name for the course; not seen in a capture.
                column(NAME, "Gnome Stronghold Agility")
                columnRSCM(LAP_COUNT, "varp.agility_laps_gnome_stronghold")
                column(HELPER_COURSE, 1)
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
                column(NAME, "Draynor Village Rooftop")
                columnRSCM(LAP_COUNT, "varp.agility_laps_draynor")
                column(HELPER_COURSE, 3)
            }
        }
}
