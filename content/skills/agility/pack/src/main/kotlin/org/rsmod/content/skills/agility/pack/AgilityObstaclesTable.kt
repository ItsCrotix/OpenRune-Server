package org.rsmod.content.skills.agility.pack

import dev.openrune.definition.dbtables.dbTable
import dev.openrune.definition.util.VarType

/**
 * Every course obstacle in lap order. A lane is one way across: the player is put on its start
 * tile and its actions play out tick by tick, `land` granting the obstacle's xp. An obstacle with a
 * success curve can fail, which plays the matching fail lane instead. Xp is in tenths.
 *
 * Lanes come from live captures of the courses, except where a course file says otherwise.
 */
object AgilityObstaclesTable {
    const val COURSE = 0
    const val STEP = 1
    const val LOCS = 2
    const val XP = 3
    const val LANES = 4
    const val ACTIONS = 5
    const val MESSAGES = 6
    const val SUCCESS = 7
    const val FAIL_ACTIONS = 8
    const val FAIL_DAMAGE = 9
    const val DIARY_XP = 10
    const val AP_RANGE = 11

    const val ACTION_STEP = 0
    const val ACTION_FACE = 1
    const val ACTION_ANIM = 2
    const val ACTION_RESET_ANIM = 3
    const val ACTION_SOUND = 4
    const val ACTION_BALANCE = 5
    const val ACTION_BALANCE_OFF = 6
    const val ACTION_EXACT_MOVE = 7
    const val ACTION_TELEJUMP = 8
    const val ACTION_LAND = 9
    const val ACTION_HIT = 10
    const val ACTION_EXACT_PATH = 11

    fun obstacles() =
        dbTable("dbtable.agility_obstacles", serverOnly = true) {
            column("course", COURSE, VarType.DBROW)
            column("step", STEP, VarType.INT)
            column("locs", LOCS, VarType.LOC)
            column("xp", XP, VarType.INT)
            column("lanes", LANES, VarType.INT, VarType.COORDGRID)
            column("actions", ACTIONS, *ACTION_TYPES)
            column("messages", MESSAGES, VarType.INT, VarType.INT, VarType.STRING)
            column("success", SUCCESS, VarType.INT, VarType.INT)
            column("fail_actions", FAIL_ACTIONS, *ACTION_TYPES)
            column("fail_damage", FAIL_DAMAGE, VarType.INT, VarType.INT, VarType.INT)
            column("diary_xp", DIARY_XP, VarType.INT, VarType.INT)
            column("ap_range", AP_RANGE, VarType.INT)

            gnomeStrongholdObstacles()
            draynorObstacles()
            alKharidObstacles()
            varrockObstacles()
            canifisObstacles()
            faladorObstacles()
            seersObstacles()
            pollnivneachObstacles()
            rellekkaObstacles()
            ardougneObstacles()
        }

    private val ACTION_TYPES =
        arrayOf(
            VarType.INT,
            VarType.INT,
            VarType.INT,
            VarType.COORDGRID,
            VarType.INT,
            VarType.INT,
            VarType.INT,
        )
}
