package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.DBTableBuilder
import dev.openrune.definition.dbtables.dbTable
import dev.openrune.definition.util.VarType
import dev.openrune.rscm.RSCM.asRSCM
import org.rsmod.map.CoordGrid

/**
 * One row per course. `name` is the course as its lap count message names it; `helper_course` is
 * its id in the client's agility helper (the keys of enum 3997, whose values list each course's
 * obstacle locs, e.g. `enum.gnome_stronghold_course_obstacles`).
 *
 * Mark of grace rules are the wiki's (https://oldschool.runescape.wiki/w/Mark_of_Grace): the
 * chance per lap, whether it drops 80% at 20 levels over the course, the diary rerolls of a missed
 * roll and the diary chance (one in n) of a two-minute timer instead of three.
 *
 * A course can also need a quest (completed, or only started), refuse boosted levels and pay part
 * of its lap bonus in another stat.
 */
object AgilityCoursesTable {
    const val LEVEL = 0
    const val LAP_BONUS_XP = 1
    const val MARK_TILES = 2
    const val NAME = 3
    const val LAP_COUNT = 4
    const val HELPER_COURSE = 5
    const val MARK_CHANCE = 6
    const val MARK_LEVEL_PENALTY = 7
    const val MARK_REROLLS = 8
    const val MARK_SHORT_TIMER = 9
    const val QUEST = 10
    const val QUEST_STARTED = 11
    const val LEVEL_UNBOOSTABLE = 12
    const val LAP_STAT_XP = 13

    fun courses() =
        dbTable("dbtable.agility_courses", serverOnly = true) {
            column("level", LEVEL, VarType.INT)
            column("lap_bonus_xp", LAP_BONUS_XP, VarType.INT)
            column("mark_tiles", MARK_TILES, VarType.COORDGRID)
            column("name", NAME, VarType.STRING)
            column("lap_count", LAP_COUNT, VarType.VARP)
            column("helper_course", HELPER_COURSE, VarType.INT)
            column("mark_chance", MARK_CHANCE, VarType.INT, VarType.INT)
            column("mark_level_penalty", MARK_LEVEL_PENALTY, VarType.BOOLEAN)
            column("mark_rerolls", MARK_REROLLS, VarType.INT, VarType.INT)
            column("mark_short_timer", MARK_SHORT_TIMER, VarType.INT, VarType.INT)
            column("quest", QUEST, VarType.DBROW)
            column("quest_started", QUEST_STARTED, VarType.BOOLEAN)
            column("level_unboostable", LEVEL_UNBOOSTABLE, VarType.BOOLEAN)
            column("lap_stat_xp", LAP_STAT_XP, VarType.INT, VarType.INT)

            // Rellekka's and Ardougne's mark tiles are where the rsprox.net captures show marks
            // spawning. UNVERIFIED: the rest; the Gnome tile follows the wiki's "before climbing
            // down the tree branch", the other rooftop ones are tiles on the roofs the wiki names,
            // from another implementation.
            course(
                "dbrow.agility_course_gnome_stronghold",
                level = 1,
                name = "Gnome Stronghold Agility",
                helperCourse = 1,
                lapCount = "varp.agility_laps_gnome_stronghold",
                lapBonusXp = 500,
                markTiles = listOf(tile(2484, 3419, 2)),
            )
            course(
                "dbrow.agility_course_draynor",
                level = 1,
                name = "Draynor Village Rooftop",
                helperCourse = 3,
                lapCount = "varp.agility_laps_draynor",
                markTiles =
                    listOf(
                        tile(3100, 3279, 3),
                        tile(3089, 3274, 3),
                        tile(3090, 3255, 3),
                        tile(3098, 3259, 3),
                    ),
            )
            course(
                "dbrow.agility_course_al_kharid",
                level = 20,
                name = "Al Kharid Rooftop",
                helperCourse = 4,
                lapCount = "varp.agility_laps_al_kharid",
                markTiles =
                    listOf(
                        tile(3274, 3190, 3),
                        tile(3270, 3170, 3),
                        tile(3290, 3165, 3),
                        tile(3316, 3162, 1),
                        tile(3316, 3176, 2),
                        tile(3316, 3183, 3),
                        tile(3301, 3190, 3),
                    ),
            )
            course(
                "dbrow.agility_course_varrock",
                level = 30,
                name = "Varrock Rooftop",
                helperCourse = 7,
                lapCount = "varp.agility_laps_varrock",
                markTiles =
                    listOf(
                        tile(3219, 3417, 3),
                        tile(3208, 3400, 3),
                        tile(3195, 3404, 3),
                        tile(3218, 3399, 3),
                        tile(3225, 3402, 3),
                        tile(3237, 3406, 3),
                        tile(3236, 3413, 3),
                    ),
            )
            course(
                "dbrow.agility_course_canifis",
                level = 40,
                name = "Canifis Rooftop",
                helperCourse = 10,
                lapCount = "varp.agility_laps_canifis",
                markChance = 2 to 3,
                markLevelPenalty = false,
                markTiles =
                    listOf(
                        tile(3507, 3495, 2),
                        tile(3499, 3505, 2),
                        tile(3489, 3500, 2),
                        tile(3477, 3495, 3),
                        tile(3480, 3486, 2),
                        tile(3495, 3475, 3),
                        tile(3511, 3479, 2),
                    ),
            )
            course(
                "dbrow.agility_course_falador",
                level = 50,
                name = "Falador Rooftop",
                helperCourse = 12,
                lapCount = "varp.agility_laps_falador",
                markTiles =
                    listOf(
                        tile(3038, 3342, 3),
                        tile(3048, 3346, 3),
                        tile(3049, 3357, 3),
                        tile(3038, 3363, 3),
                        tile(3012, 3356, 3),
                        tile(3018, 3348, 3),
                        tile(3010, 3340, 3),
                        tile(3015, 3333, 3),
                        tile(3022, 3334, 3),
                    ),
            )
            course(
                "dbrow.agility_course_seers",
                level = 60,
                name = "Seers' Village Rooftop",
                helperCourse = 15,
                lapCount = "varp.agility_laps_seers",
                markRerolls =
                    listOf(
                        "varbit.kandarin_diary_easy_complete" to 5,
                        "varbit.kandarin_diary_medium_complete" to 10,
                        "varbit.kandarin_diary_hard_complete" to 15,
                    ),
                markTiles =
                    listOf(
                        tile(2725, 3492, 3),
                        tile(2708, 3494, 2),
                        tile(2713, 3479, 2),
                        tile(2702, 3473, 3),
                        tile(2700, 3462, 2),
                    ),
            )
            course(
                "dbrow.agility_course_pollnivneach",
                level = 70,
                name = "Pollnivneach Rooftop",
                helperCourse = 17,
                lapCount = "varp.agility_laps_pollnivneach",
                markShortTimer = "varbit.desert_diary_hard_complete" to 3,
                markTiles =
                    listOf(
                        tile(3348, 2966, 1),
                        tile(3354, 2975, 1),
                        tile(3361, 2980, 1),
                        tile(3368, 2975, 1),
                        tile(3357, 2983, 2),
                        tile(3364, 2993, 2),
                        tile(3357, 3002, 2),
                    ),
            )
            course(
                "dbrow.agility_course_rellekka",
                level = 80,
                name = "Rellekka Rooftop",
                helperCourse = 19,
                lapCount = "varp.agility_laps_rellekka",
                markChance = 2 to 5,
                markShortTimer = "varbit.fremennik_diary_hard_complete" to 3,
                markTiles =
                    listOf(
                        tile(2623, 3675, 3),
                        tile(2628, 3652, 3),
                        tile(2629, 3653, 3),
                        tile(2642, 3651, 3),
                    ),
            )
            course(
                "dbrow.agility_course_ardougne",
                level = 90,
                name = "Ardougne Rooftop",
                helperCourse = 20,
                lapCount = "varp.agility_laps_ardougne",
                markChance = 2 to 3,
                markRerolls = listOf("varbit.ardougne_diary_elite_complete" to 25),
                markShortTimer = "varbit.ardougne_diary_elite_complete" to 2,
                markTiles = listOf(tile(2657, 3318, 3)),
            )

            // UNVERIFIED: lap count names (only Wilderness and Prifddinas are captured), the mark
            // tiles (the wiki only describes where they fall) and the non-rooftop mark chance,
            // which the wiki gives as marks an hour, not a chance per lap.
            course(
                "dbrow.agility_course_barbarian",
                level = 35,
                name = "Barbarian Outpost",
                helperCourse = 9,
                lapCount = "varp.agility_laps_barbarian",
                lapBonusXp = 463,
                lapStatXp = "stat.strength" to 413,
                quest = "dbrow.miniquest_barcrawl",
                markTiles = listOf(tile(2532, 3546, 1)),
            )
            course(
                "dbrow.agility_course_wilderness",
                level = 52,
                name = "Wilderness Agility",
                helperCourse = 13,
                lapCount = "varp.agility_laps_wilderness",
                lapBonusXp = 4989,
            )
            course(
                "dbrow.agility_course_prifddinas",
                level = 75,
                name = "Prifddinas Agility Course",
                helperCourse = 18,
                lapCount = "varp.agility_laps_prifddinas",
                quest = "dbrow.quest_songoftheelves",
            )
            course(
                "dbrow.agility_course_ape_atoll",
                level = 48,
                name = "Ape Atoll",
                helperCourse = 11,
                lapCount = "varp.agility_laps_ape_atoll",
                lapBonusXp = 3000,
                levelUnboostable = true,
                quest = "dbrow.quest_monkeymadness1",
                questStarted = true,
                markTiles = listOf(tile(2743, 2738, 0)),
            )
            course(
                "dbrow.agility_course_werewolf",
                level = 60,
                name = "Werewolf Agility",
                helperCourse = 14,
                lapCount = "varp.agility_laps_werewolf",
                quest = "dbrow.quest_creatureoffenkenstrain",
                markTiles = listOf(tile(3540, 9888, 0)),
            )
            course(
                "dbrow.agility_course_shayzien_basic",
                level = 1,
                name = "Shayzien Basic Agility Course",
                helperCourse = 21,
                lapCount = "varp.agility_laps_shayzien_basic",
                markTiles = listOf(tile(1530, 3632, 2)),
            )
            course(
                "dbrow.agility_course_shayzien_advanced",
                level = 45,
                name = "Shayzien Advanced Agility Course",
                helperCourse = 21,
                lapCount = "varp.agility_laps_shayzien_advanced",
                markTiles = listOf(tile(1530, 3632, 2)),
            )
            course(
                "dbrow.agility_course_wyrm_basic",
                level = 50,
                name = "Colossal Wyrm Basic Agility Course",
                helperCourse = 22,
                lapCount = "varp.agility_laps_wyrm_basic",
                quest = "dbrow.quest_childrenofthesun",
            )
            course(
                "dbrow.agility_course_wyrm_advanced",
                level = 62,
                name = "Colossal Wyrm Advanced Agility Course",
                helperCourse = 22,
                lapCount = "varp.agility_laps_wyrm_advanced",
                quest = "dbrow.quest_childrenofthesun",
            )
            course(
                "dbrow.agility_course_penguin",
                level = 30,
                name = "Penguin Agility",
                helperCourse = 8,
                lapCount = "varp.agility_laps_penguin",
                quest = "dbrow.quest_coldwar",
                questStarted = true,
            )
        }

    private fun DBTableBuilder.course(
        row: String,
        level: Int,
        name: String,
        helperCourse: Int,
        lapCount: String,
        markTiles: List<CoordGrid> = emptyList(),
        lapBonusXp: Int = 0,
        lapStatXp: Pair<String, Int>? = null,
        quest: String? = null,
        questStarted: Boolean = false,
        levelUnboostable: Boolean = false,
        markChance: Pair<Int, Int> = 2 to 6,
        markLevelPenalty: Boolean = true,
        markRerolls: List<Pair<String, Int>> = emptyList(),
        markShortTimer: Pair<String, Int>? = null,
    ) {
        row(row) {
            column(LEVEL, level)
            column(LAP_BONUS_XP, lapBonusXp)
            if (markTiles.isNotEmpty()) {
                column(MARK_TILES, markTiles.map { it.packed })
            }
            if (lapStatXp != null) {
                column(LAP_STAT_XP, lapStatXp.first.asRSCM(), lapStatXp.second)
            }
            if (quest != null) {
                columnRSCM(QUEST, quest)
                column(QUEST_STARTED, questStarted)
            }
            if (levelUnboostable) {
                column(LEVEL_UNBOOSTABLE, true)
            }
            column(NAME, name)
            columnRSCM(LAP_COUNT, lapCount)
            column(HELPER_COURSE, helperCourse)
            column(MARK_CHANCE, markChance.first, markChance.second)
            column(MARK_LEVEL_PENALTY, markLevelPenalty)
            if (markRerolls.isNotEmpty()) {
                val rerolls =
                    markRerolls.flatMap { (varbit, percent) -> listOf(varbit.asRSCM(), percent) }
                column(MARK_REROLLS, rerolls)
            }
            if (markShortTimer != null) {
                column(MARK_SHORT_TIMER, markShortTimer.first.asRSCM(), markShortTimer.second)
            }
        }
    }
}
