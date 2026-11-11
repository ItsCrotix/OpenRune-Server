package org.rsmod.content.other.commandstick

import dev.openrune.definition.type.widget.IfEvent
import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import dev.openrune.types.ItemServerType
import dev.or2.central.account.Rights
import jakarta.inject.Inject
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.net.central.logging.CentralActivityLogWriter
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onCommand
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onPlayerSoftQueueWithArgs
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
    private val rows by lazy { CommandStickRows.rows() }
    private val categoryNames by lazy { CommandStickRows.categoryNames() }

    override fun ScriptContext.startup() {
        onOpHeld1(STICK) { openStick() }
        onIfModalButton(CATEGORIES) { selectCategory(it.comsub / 2) }
        onIfModalButton(LIST) { runEntry(it.comsub) }
        onPlayerSoftQueueWithArgs<CommandRun>(RUN_QUEUE) { runCommand(player, args) }
        onCommand("commandstick") {
            desc = "Get a command stick"
            requiredRights = Rights.ADMINISTRATOR
            cheat { giveStick() }
        }
    }

    private fun catalog(): List<CommandCategory> {
        val registered = commands.commands.mapValues { it.value.desc }
        return CommandStickCatalog.build(rows, categoryNames, registered)
    }

    private fun ProtectedAccess.openStick() {
        if (!player.isAdmin()) {
            mes(NOT_ADMIN)
            return
        }
        player.commandStickCategory = 0
        ifOpenMainModal(INTERFACE)
        ifSetEvents(CATEGORIES, 0 until MAX_CATEGORY_SUBS, IfEvent.Op1)
        ifSetEvents(LIST, 0 until MAX_ENTRIES, IfEvent.Op1)
        showCategory(catalog())
    }

    private fun ProtectedAccess.selectCategory(index: Int) {
        if (!player.isAdmin()) {
            refuse()
            return
        }
        val categories = catalog()
        if (index !in categories.indices) {
            return
        }
        player.commandStickCategory = index
        showCategory(categories)
    }

    private fun ProtectedAccess.showCategory(categories: List<CommandCategory>) {
        val last = categories.lastIndex.coerceAtLeast(0)
        val selected = player.commandStickCategory.coerceAtMost(last)
        runClientScript(
            CATEGORIES_SCRIPT.asRSCM(RSCMType.CLIENTSCRIPT),
            CATEGORIES.asRSCM(RSCMType.COMPONENT),
            CommandStickCatalog.encodeNames(categories),
            selected,
        )
        runClientScript(
            LIST_SCRIPT.asRSCM(RSCMType.CLIENTSCRIPT),
            LIST.asRSCM(RSCMType.COMPONENT),
            SCROLLBAR.asRSCM(RSCMType.COMPONENT),
            DESCRIPTION.asRSCM(RSCMType.COMPONENT),
            CommandStickCatalog.encodeEntries(categories.getOrNull(selected)?.entries.orEmpty()),
        )
    }

    private suspend fun ProtectedAccess.runEntry(index: Int) {
        if (!player.isAdmin()) {
            refuse()
            return
        }
        val categories = catalog()
        val entry = categories.getOrNull(player.commandStickCategory)?.entries?.getOrNull(index)
        if (entry == null) {
            mes(UNAVAILABLE)
            showCategory(categories)
            return
        }
        ifClose()
        if (entry.confirm && !choice2("Yes", true, "No", false, title = "${entry.name}?")) {
            return
        }
        val answers = mutableListOf<String>()
        for (prompt in entry.prompts) {
            answers +=
                when (prompt.type) {
                    PromptType.Number -> numberDialog(prompt.text).toString()
                    PromptType.Text ->
                        stringDialog(prompt.text).takeIf { it.isNotBlank() } ?: return
                    PromptType.Item ->
                        objDialog(prompt.text, stockMarketRestriction = false).gamevalName()
                }
        }
        softQueue(RUN_QUEUE, 1, CommandStickCatalog.run(entry, answers))
    }

    private fun ProtectedAccess.refuse() {
        ifClose()
        mes(NOT_ADMIN)
    }

    private fun runCommand(player: Player, run: CommandRun) {
        if (!player.isAdmin()) {
            return
        }
        activityLog.logCommand(player, run.command, run.args)
        commands.execute(player, run.command, run.args)
    }

    private fun Cheat.giveStick() {
        val bank = player.invMap.getOrPut("inv.bank")
        when {
            STICK in player.inv || STICK in bank -> player.mes("You already have a command stick.")
            player.inv.isFull() -> player.mes("You need a free inventory slot.")
            else -> player.invAdd(player.inv, STICK)
        }
    }

    private fun Player.isAdmin(): Boolean = modLevel.isAtLeast(Rights.ADMINISTRATOR)

    private fun ItemServerType.gamevalName(): String =
        runCatching { RSCM.getReverseMapping(RSCMType.OBJ, id) }
            .getOrNull()
            ?.removePrefix("obj.")
            ?: id.toString()

    private companion object {
        const val STICK = "obj.command_stick"
        const val INTERFACE = "interface.command_stick"
        const val CATEGORIES = "component.command_stick:categories"
        const val LIST = "component.command_stick:list"
        const val SCROLLBAR = "component.command_stick:scrollbar"
        const val DESCRIPTION = "component.command_stick:description"
        const val CATEGORIES_SCRIPT = "clientscript.command_stick_categories"
        const val LIST_SCRIPT = "clientscript.command_stick_list"
        const val RUN_QUEUE = "queue.command_stick_run"
        const val MAX_CATEGORY_SUBS = 32
        const val MAX_ENTRIES = 256
        const val NOT_ADMIN = "Only administrators can use the command stick."
        const val UNAVAILABLE = "That command is no longer available."
    }
}
