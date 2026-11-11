package org.rsmod.content.other.commandstick

import dev.or2.central.account.Rights
import jakarta.inject.Inject
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.net.central.logging.CentralActivityLogWriter
import org.rsmod.api.player.output.mes
import org.rsmod.api.script.onCommand
import org.rsmod.game.cheat.Cheat
import org.rsmod.game.cheat.CheatCommandMap
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class CommandStickScript
@Inject
constructor(
    private val commands: CheatCommandMap,
    private val activityLog: CentralActivityLogWriter,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onCommand("commandstick") {
            desc = "Get a command stick"
            requiredRights = Rights.ADMINISTRATOR
            cheat { giveStick() }
        }
    }

    private fun Cheat.giveStick() {
        val bank = player.invMap.getOrPut("inv.bank")
        when {
            STICK in player.inv || STICK in bank -> player.mes("You already have a command stick.")
            player.inv.isFull() -> player.mes("You need a free inventory slot.")
            else -> player.invAdd(player.inv, STICK)
        }
    }

    private companion object {
        const val STICK = "obj.command_stick"
    }
}
