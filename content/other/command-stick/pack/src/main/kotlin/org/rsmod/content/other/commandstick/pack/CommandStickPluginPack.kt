package org.rsmod.content.other.commandstick.pack

import dev.openrune.definition.dbtables.DBTable
import dev.openrune.pack.PluginPack

class CommandStickPluginPack : PluginPack() {
    override fun dbTables(): List<DBTable> = listOf(CommandStickTable.commands())
}
