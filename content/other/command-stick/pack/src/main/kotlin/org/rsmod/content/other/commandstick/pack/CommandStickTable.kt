package org.rsmod.content.other.commandstick.pack

import dev.openrune.definition.dbtables.dbTable
import dev.openrune.definition.util.VarType

object CommandStickTable {
    const val COMMAND = 0
    const val NAME = 1
    const val CATEGORY = 2
    const val FIXED_ARGS = 3
    const val PROMPTS = 4
    const val ALIASES = 5
    const val DESCRIPTION = 6
    const val CONFIRM = 7

    private const val TELEPORT = 0
    private const val ITEMS = 1
    private const val PLAYER = 2
    private const val SPAWNING = 3
    private const val EFFECTS = 4
    private const val DEBUG = 5
    private const val BOSSES = 6
    private const val WORLD = 7
    private const val SERVER = 8

    private const val TEXT = 0
    private const val NUMBER = 1
    private const val ITEM = 2

    fun commands() =
        dbTable("dbtable.command_stick_commands", serverOnly = true) {
            column("command", COMMAND, VarType.STRING)
            column("name", NAME, VarType.STRING)
            column("category", CATEGORY, VarType.INT)
            column("fixed_args", FIXED_ARGS, VarType.STRING)
            column("prompts", PROMPTS, VarType.INT, VarType.STRING)
            column("aliases", ALIASES, VarType.STRING)
            column("description", DESCRIPTION, VarType.STRING)
            column("confirm", CONFIRM, VarType.BOOLEAN)

            fun command(
                row: String,
                command: String,
                name: String,
                category: Int,
                fixedArgs: String? = null,
                prompts: List<Pair<Int, String>> = emptyList(),
                aliases: String? = null,
                description: String? = null,
                confirm: Boolean = false,
            ) {
                row(row) {
                    column(COMMAND, command)
                    column(NAME, name)
                    column(CATEGORY, category)
                    fixedArgs?.let { column(FIXED_ARGS, it) }
                    if (prompts.isNotEmpty()) {
                        column(
                            PROMPTS,
                            *prompts.flatMap { listOf<Any>(it.first, it.second) }.toTypedArray(),
                        )
                    }
                    aliases?.let { column(ALIASES, it) }
                    description?.let { column(DESCRIPTION, it) }
                    column(CONFIRM, confirm)
                }
            }

            val tiles = listOf(NUMBER to "How many tiles?")
            val mechanic = listOf(TEXT to "Mechanic")
            val pluginName = listOf(TEXT to "Plugin name")

            command(
                "dbrow.command_stick_tele",
                "tele",
                "Teleport to coordinates",
                TELEPORT,
                prompts =
                    listOf(
                        NUMBER to "X coordinate",
                        NUMBER to "Y coordinate",
                        NUMBER to "Level (0-3)",
                    ),
            )
            command(
                "dbrow.command_stick_telezone",
                "telezone",
                "Teleport to zone",
                TELEPORT,
                prompts = listOf(NUMBER to "Zone X", NUMBER to "Zone Y", NUMBER to "Level (0-3)"),
            )
            command("dbrow.command_stick_up", "up", "Go up a level", TELEPORT)
            command("dbrow.command_stick_down", "down", "Go down a level", TELEPORT)
            command(
                "dbrow.command_stick_forward",
                "forward",
                "Step north",
                TELEPORT,
                prompts = tiles,
            )
            command(
                "dbrow.command_stick_backwards",
                "backwards",
                "Step south",
                TELEPORT,
                prompts = tiles,
                aliases = "back",
            )
            command("dbrow.command_stick_left", "left", "Step west", TELEPORT, prompts = tiles)
            command("dbrow.command_stick_right", "right", "Step east", TELEPORT, prompts = tiles)
            command("dbrow.command_stick_mypos", "mypos", "Show my position", TELEPORT)
            command(
                "dbrow.command_stick_instance_list",
                "instanceexit",
                "List instances",
                TELEPORT,
                description = "Lists the instance keys you can leave with Leave an instance.",
            )
            command(
                "dbrow.command_stick_instance_exit",
                "instanceexit",
                "Leave an instance",
                TELEPORT,
                prompts = listOf(TEXT to "Instance key"),
            )
            command("dbrow.command_stick_gauntlet", "gauntlet", "Go to the Gauntlet", TELEPORT)

            command(
                "dbrow.command_stick_item",
                "item",
                "Spawn item",
                ITEMS,
                prompts = listOf(ITEM to "Spawn which item?", NUMBER to "How many?"),
                aliases = "invadd",
            )
            command("dbrow.command_stick_spawn", "spawn", "Open the item spawner", ITEMS)
            command("dbrow.command_stick_spawnold", "spawnold", "Spawn items by search", ITEMS)
            command(
                "dbrow.command_stick_invclear",
                "invclear",
                "Clear inventory",
                ITEMS,
                confirm = true,
            )
            command("dbrow.command_stick_openbank", "openbank", "Open bank", ITEMS)
            command(
                "dbrow.command_stick_craftmat",
                "craftmat",
                "Get crafting materials",
                ITEMS,
                prompts = listOf(ITEM to "Materials for which item?", NUMBER to "How many?"),
            )
            command("dbrow.command_stick_pet", "pet", "Award a pet", ITEMS)
            command(
                "dbrow.command_stick_commandstick",
                "commandstick",
                "Get a command stick",
                ITEMS,
            )

            command("dbrow.command_stick_master", "master", "Max all stats", PLAYER)
            command("dbrow.command_stick_reset", "reset", "Reset all stats", PLAYER, confirm = true)
            command("dbrow.command_stick_god", "god", "Toggle god mode", PLAYER)
            command("dbrow.command_stick_maxhit", "maxhit", "Toggle always max hit", PLAYER)
            command(
                "dbrow.command_stick_spellbook_standard",
                "spellbook",
                "Standard spellbook",
                PLAYER,
                fixedArgs = "standard",
                aliases = "book",
            )
            command(
                "dbrow.command_stick_spellbook_ancients",
                "spellbook",
                "Ancient Magicks",
                PLAYER,
                fixedArgs = "ancients",
            )
            command(
                "dbrow.command_stick_spellbook_lunars",
                "spellbook",
                "Lunar spellbook",
                PLAYER,
                fixedArgs = "lunars",
            )
            command(
                "dbrow.command_stick_spellbook_arceuus",
                "spellbook",
                "Arceuus spellbook",
                PLAYER,
                fixedArgs = "arceuus",
            )
            command(
                "dbrow.command_stick_gamemode_normal",
                "gamemode",
                "Game mode: normal",
                PLAYER,
                fixedArgs = "normal",
                confirm = true,
            )
            command(
                "dbrow.command_stick_gamemode_ironman",
                "gamemode",
                "Game mode: ironman",
                PLAYER,
                fixedArgs = "ironman",
                confirm = true,
            )
            command(
                "dbrow.command_stick_gamemode_uim",
                "gamemode",
                "Game mode: ultimate ironman",
                PLAYER,
                fixedArgs = "uim",
                confirm = true,
            )
            command(
                "dbrow.command_stick_gamemode_hcim",
                "gamemode",
                "Game mode: hardcore ironman",
                PLAYER,
                fixedArgs = "hcim",
                confirm = true,
            )
            command(
                "dbrow.command_stick_transmog",
                "transmog",
                "Transform into an NPC",
                PLAYER,
                prompts = listOf(TEXT to "NPC name or id"),
            )
            command("dbrow.command_stick_transmog_reset", "transmog", "Undo transformation", PLAYER)
            command(
                "dbrow.command_stick_die_pvm",
                "die",
                "Die (PvM)",
                PLAYER,
                fixedArgs = "pvm",
                confirm = true,
            )
            command(
                "dbrow.command_stick_die_pvp",
                "die",
                "Die (PvP)",
                PLAYER,
                fixedArgs = "pvp",
                confirm = true,
            )
            command("dbrow.command_stick_poison", "poison", "Poison yourself", PLAYER)
            command("dbrow.command_stick_venom", "venom", "Envenom yourself", PLAYER)
            command("dbrow.command_stick_venomclear", "venomclear", "Cure venom", PLAYER)
            command("dbrow.command_stick_disease", "disease", "Give yourself disease", PLAYER)
            command("dbrow.command_stick_diseaseclear", "diseaseclear", "Cure disease", PLAYER)

            command(
                "dbrow.command_stick_npcadd",
                "npcadd",
                "Spawn NPC",
                SPAWNING,
                prompts = listOf(NUMBER to "Duration (ticks)", TEXT to "NPC name"),
                aliases = "npc",
            )
            command(
                "dbrow.command_stick_npcgrid",
                "npcgrid",
                "Spawn NPC grid",
                SPAWNING,
                prompts = listOf(TEXT to "NPC name"),
            )
            command(
                "dbrow.command_stick_locadd",
                "locadd",
                "Spawn object",
                SPAWNING,
                prompts = listOf(NUMBER to "Duration (ticks)", TEXT to "Object name"),
                aliases = "object",
            )
            command(
                "dbrow.command_stick_locdel",
                "locdel",
                "Remove object",
                SPAWNING,
                prompts = listOf(NUMBER to "Duration (ticks)"),
                aliases = "objectdel",
            )

            command(
                "dbrow.command_stick_anim",
                "anim",
                "Play animation",
                EFFECTS,
                prompts = listOf(TEXT to "Animation name"),
            )
            command(
                "dbrow.command_stick_spot",
                "spot",
                "Play spotanim",
                EFFECTS,
                prompts = listOf(TEXT to "Spotanim name"),
            )
            command(
                "dbrow.command_stick_synth",
                "synth",
                "Play sound",
                EFFECTS,
                prompts = listOf(TEXT to "Sound name or id"),
            )
            command(
                "dbrow.command_stick_npcanim",
                "npcanim",
                "Animate nearest NPC",
                EFFECTS,
                prompts = listOf(NUMBER to "Animation id"),
                aliases = "nanim",
            )
            command(
                "dbrow.command_stick_npcspot",
                "npcspot",
                "Spotanim on nearest NPC",
                EFFECTS,
                prompts = listOf(NUMBER to "Spotanim id"),
                aliases = "nspot",
            )
            command(
                "dbrow.command_stick_npcproj",
                "npcproj",
                "Projectile from nearest NPC",
                EFFECTS,
                prompts = listOf(NUMBER to "Spotanim id"),
                aliases = "nproj",
            )

            command(
                "dbrow.command_stick_varp",
                "varp",
                "Set varp",
                DEBUG,
                prompts = listOf(TEXT to "Varp name", NUMBER to "Value"),
            )
            command(
                "dbrow.command_stick_varbit",
                "varbit",
                "Set varbit",
                DEBUG,
                prompts = listOf(TEXT to "Varbit name", NUMBER to "Value"),
            )
            command(
                "dbrow.command_stick_getvarp",
                "getvarp",
                "Get varp",
                DEBUG,
                prompts = listOf(TEXT to "Varp name"),
            )
            command(
                "dbrow.command_stick_getvarbit",
                "getvarbit",
                "Get varbit",
                DEBUG,
                prompts = listOf(TEXT to "Varbit name"),
            )
            command(
                "dbrow.command_stick_interface",
                "interface",
                "Open interface",
                DEBUG,
                prompts = listOf(TEXT to "Interface name or id"),
                aliases = "ifopen",
            )
            command(
                "dbrow.command_stick_componentdebug",
                "componentdebug",
                "Toggle component click debug",
                DEBUG,
            )
            command(
                "dbrow.command_stick_testloot",
                "testloot",
                "Test NPC loot",
                DEBUG,
                prompts = listOf(TEXT to "NPC name", NUMBER to "How many kills?"),
            )

            command("dbrow.command_stick_toalobby", "toalobby", "ToA: go to the lobby", BOSSES)
            command("dbrow.command_stick_toababa", "toababa", "ToA: enter Ba-Ba's room", BOSSES)
            command(
                "dbrow.command_stick_toawardens",
                "toawardens",
                "ToA: enter the Wardens' room",
                BOSSES,
            )
            command(
                "dbrow.command_stick_toacomplete",
                "toacomplete",
                "ToA: complete the current room",
                BOSSES,
            )
            command("dbrow.command_stick_toanext", "toanext", "ToA: go to the next room", BOSSES)
            command("dbrow.command_stick_toapoints", "toapoints", "ToA: show reward points", BOSSES)
            command(
                "dbrow.command_stick_toareward",
                "toareward",
                "ToA: enter the reward room",
                BOSSES,
                prompts = listOf(NUMBER to "Loot points"),
            )
            command(
                "dbrow.command_stick_baba",
                "baba",
                "ToA: Ba-Ba mechanic",
                BOSSES,
                prompts = mechanic,
                description = "Roll, rockfall, shockwave, stomp or discharge, in Ba-Ba's room.",
            )
            command(
                "dbrow.command_stick_apmeken",
                "apmeken",
                "ToA: Apmeken mechanic",
                BOSSES,
                prompts = mechanic,
                description = "Wave, kill, roof, vents or sight, in the Apmeken puzzle room.",
            )
            command(
                "dbrow.command_stick_zebak",
                "zebak",
                "ToA: Zebak mechanic",
                BOSSES,
                prompts = mechanic,
                description = "Clouds, barrage, roar, waves or enrage, in Zebak's room.",
            )
            command(
                "dbrow.command_stick_akkha",
                "akkha",
                "ToA: Akkha mechanic",
                BOSSES,
                prompts = mechanic,
                description =
                    "Blast or slam with a quadrant (blast ne), or tiles, memory, trail, " +
                        "detonate, final.",
            )
            command(
                "dbrow.command_stick_het",
                "het",
                "ToA: Het mechanic",
                BOSSES,
                prompts = mechanic,
                description = "Beam, weaken or redraft, in the Het puzzle room.",
            )
            command(
                "dbrow.command_stick_kephri",
                "kephri",
                "ToA: Kephri mechanic",
                BOSSES,
                prompts = mechanic,
                description =
                    "Break, shield, final, dung, eggs, bomb, swarm, " +
                        "agile, spitting, soldier or arcane.",
            )
            command(
                "dbrow.command_stick_scabaras",
                "scabaras",
                "ToA: Scabaras mechanic",
                BOSSES,
                prompts = mechanic,
                description = "Layout, slot, final, goal or order, in the Scabaras puzzle room.",
            )
            command(
                "dbrow.command_stick_wardens",
                "wardens",
                "ToA: Wardens mechanic",
                BOSSES,
                prompts = mechanic,
                description =
                    "Divine, entomb, special, hp <percent>, p3 or siphon, in the Wardens' rooms.",
            )
            command(
                "dbrow.command_stick_doomsim",
                "doomsim",
                "Doom of Mokhaiotl: simulate delves",
                BOSSES,
                prompts = listOf(NUMBER to "How many delves?"),
            )
            command(
                "dbrow.command_stick_gemstonecrab",
                "gemstonecrab",
                "Gemstone crab: force burrow",
                BOSSES,
            )
            command(
                "dbrow.command_stick_gauntletkit",
                "gauntletkit",
                "Gauntlet: enter with tier 3 kit",
                BOSSES,
            )
            command(
                "dbrow.command_stick_gauntletkithm",
                "gauntletkithm",
                "Corrupted Gauntlet: enter with tier 3 kit",
                BOSSES,
            )

            command("dbrow.command_stick_star", "star", "Spawn a shooting star", WORLD)
            command(
                "dbrow.command_stick_randomevent",
                "randomevent",
                "Spawn the Genie random event",
                WORLD,
            )
            command(
                "dbrow.command_stick_bots_count",
                "bots",
                "Bots: show status",
                WORLD,
                fixedArgs = "count",
            )
            command("dbrow.command_stick_bots_on", "bots", "Bots: turn on", WORLD, fixedArgs = "on")
            command(
                "dbrow.command_stick_bots_off",
                "bots",
                "Bots: turn off",
                WORLD,
                fixedArgs = "off",
            )
            command(
                "dbrow.command_stick_bots_reset",
                "bots",
                "Bots: remove all",
                WORLD,
                fixedArgs = "reset",
            )
            command(
                "dbrow.command_stick_bots_here",
                "bots",
                "Bots: spawn here",
                WORLD,
                fixedArgs = "here",
                prompts = listOf(TEXT to "Activity", NUMBER to "How many?"),
                description = "Woodcutting, mining, fishing, questing, wandering or idling.",
            )
            command("dbrow.command_stick_echo", "echo", "List league relics", WORLD)
            command("dbrow.command_stick_worldtype", "worldtype", "Choose world type", WORLD)

            command("dbrow.command_stick_reboot", "reboot", "Reboot server", SERVER, confirm = true)
            command(
                "dbrow.command_stick_slowreboot",
                "slowreboot",
                "Reboot server with timer",
                SERVER,
                prompts = listOf(NUMBER to "Ticks until reboot"),
                confirm = true,
            )
            command("dbrow.command_stick_plugins", "plugins", "Manage plugins", SERVER)
            command(
                "dbrow.command_stick_loadplugin",
                "loadplugin",
                "Load a plugin",
                SERVER,
                prompts = pluginName,
            )
            command(
                "dbrow.command_stick_pluginenable",
                "pluginenable",
                "Enable a plugin",
                SERVER,
                prompts = pluginName,
            )
            command(
                "dbrow.command_stick_plugindisable",
                "plugindisable",
                "Disable a plugin",
                SERVER,
                prompts = pluginName,
            )
            command(
                "dbrow.command_stick_pluginreload",
                "pluginreload",
                "Reload a plugin",
                SERVER,
                prompts = pluginName,
            )
            command(
                "dbrow.command_stick_discordlink",
                "discordlink",
                "Link your Discord account",
                SERVER,
            )
        }
}
