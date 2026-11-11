package org.rsmod.content.other.commandstick

import org.rsmod.api.enums.NamedEnums
import org.rsmod.api.table.CommandStickCommandsRow

internal object CommandStickRows {
    fun rows(): List<CommandStickRow> =
        CommandStickCommandsRow.all().map { row ->
            CommandStickRow(
                command = row.command,
                name = row.name,
                category = row.category,
                fixedArgs = row.fixedArgs.orEmpty(),
                prompts = row.prompts.map { CommandPrompt(PromptType.of(it.t0), it.t1) },
                aliases = row.aliases?.split(' ').orEmpty(),
                description = row.description,
                confirm = row.confirm,
            )
        }

    fun categoryNames(): Map<Int, String> =
        NamedEnums.command_stick_categories
            .backing
            .mapNotNull { (key, name) -> name?.let { key to it } }
            .toMap()
}
