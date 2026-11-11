package org.rsmod.content.other.commandstick

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CommandStickCatalogTest {
    @Test
    fun `rows whose command is not registered are hidden`() {
        val rows = listOf(row("tele", "Teleport", TELEPORT), row("gone", "Gone", TELEPORT))

        val categories =
            CommandStickCatalog.build(rows, NAMES, mapOf("tele" to "Teleport to coordgrid"))

        assertEquals(listOf("Teleport"), categories.single().entries.map { it.name })
    }

    @Test
    fun `unlisted commands go to other with their own description`() {
        val categories =
            CommandStickCatalog.build(emptyList(), NAMES, mapOf("zzz" to "Does zzz", "aaa" to null))

        val other = categories.single()
        assertEquals("Other", other.name)
        assertEquals(listOf("::aaa", "::zzz"), other.entries.map { it.name })
        assertEquals(listOf("", "Does zzz"), other.entries.map { it.description })
    }

    @Test
    fun `aliases stay out of other`() {
        val rows = listOf(row("item", "Spawn item", ITEMS, aliases = listOf("invadd")))

        val categories =
            CommandStickCatalog.build(rows, NAMES, mapOf("item" to "Spawn", "invadd" to "Spawn"))

        assertEquals(listOf("Items and bank"), categories.map { it.name })
    }

    @Test
    fun `empty categories are dropped and row order is kept`() {
        val rows =
            listOf(
                row("down", "Go down a level", TELEPORT),
                row("up", "Go up a level", TELEPORT),
                row("item", "Spawn item", ITEMS),
            )
        val registered = mapOf("down" to null, "up" to null, "item" to null)

        val categories = CommandStickCatalog.build(rows, NAMES, registered)

        assertEquals(listOf("Teleport", "Items and bank"), categories.map { it.name })
        assertEquals(
            listOf("Go down a level", "Go up a level"),
            categories[0].entries.map { it.name },
        )
    }

    @Test
    fun `row description wins over the command description`() {
        val rows =
            listOf(row("god", "Toggle god mode", ITEMS, description = "Makes you invincible."))

        val category = CommandStickCatalog.build(rows, NAMES, mapOf("god" to "Toggle")).single()
        val entry = category.entries.single()

        assertEquals("Makes you invincible.", entry.description)
    }

    @Test
    fun `run args are fixed args then answers split like a typed command`() {
        val entry = entry(fixedArgs = "here", command = "bots")

        val run = CommandStickCatalog.run(entry, listOf("Woodcutting", "3"))

        assertEquals(CommandRun("bots", listOf("here", "woodcutting", "3")), run)
    }

    @Test
    fun `a command without args runs with an empty list`() {
        assertEquals(
            CommandRun("god", emptyList()),
            CommandStickCatalog.run(entry(command = "god"), emptyList()),
        )
    }

    @Test
    fun `encoders strip the separators from names and descriptions`() {
        val categories =
            listOf(CommandCategory("A|B", listOf(entry(name = "x~y", description = "1|2"))))

        assertEquals("A/B", CommandStickCatalog.encodeNames(categories))
        assertEquals("x-y~1/2", CommandStickCatalog.encodeEntries(categories.single().entries))
    }

    @Test
    fun `rows with an unknown category land in other before unlisted commands`() {
        val rows = listOf(row("god", "Toggle god mode", OTHER), row("tele", "Teleport", 42))
        val registered = mapOf("god" to null, "tele" to null, "zzz" to null)

        val other = CommandStickCatalog.build(rows, NAMES, registered).single()

        assertEquals(listOf("Toggle god mode", "Teleport", "::zzz"), other.entries.map { it.name })
    }

    @Test
    fun `an unknown prompt type id fails naming the id`() {
        val error = assertThrows<IllegalStateException> { PromptType.of(7) }

        assertEquals("Unknown command stick prompt type: 7", error.message)
    }

    @Test
    fun `padded and blank answers never become empty args`() {
        val run = CommandStickCatalog.run(entry(command = "up"), listOf("  5 ", ""))

        assertEquals(CommandRun("up", listOf("5")), run)
    }

    private fun row(
        command: String,
        name: String,
        category: Int,
        aliases: List<String> = emptyList(),
        description: String? = null,
    ) = CommandStickRow(command, name, category, "", emptyList(), aliases, description, false)

    private fun entry(
        command: String = "cmd",
        name: String = "Name",
        description: String = "",
        fixedArgs: String = "",
    ) = CommandEntry(command, name, description, fixedArgs, emptyList(), false)

    private companion object {
        const val TELEPORT = 0
        const val ITEMS = 1
        const val OTHER = 9
        val NAMES = mapOf(0 to "Teleport", 1 to "Items and bank", 9 to "Other")
    }
}
