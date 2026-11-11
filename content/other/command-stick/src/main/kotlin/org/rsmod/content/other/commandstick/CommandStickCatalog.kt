package org.rsmod.content.other.commandstick

enum class PromptType(val id: Int) {
    Text(0),
    Number(1),
    Item(2);

    companion object {
        fun of(id: Int): PromptType =
            entries.firstOrNull { it.id == id } ?: error("Unknown command stick prompt type: $id")
    }
}

data class CommandPrompt(val type: PromptType, val text: String)

data class CommandStickRow(
    val command: String,
    val name: String,
    val category: Int,
    val fixedArgs: String,
    val prompts: List<CommandPrompt>,
    val aliases: List<String>,
    val description: String?,
    val confirm: Boolean,
)

data class CommandEntry(
    val command: String,
    val name: String,
    val description: String,
    val fixedArgs: String,
    val prompts: List<CommandPrompt>,
    val confirm: Boolean,
)

data class CommandCategory(val name: String, val entries: List<CommandEntry>)

data class CommandRun(val command: String, val args: List<String>)

object CommandStickCatalog {
    const val OTHER_CATEGORY = 9

    private const val ENTRY_SEPARATOR = "|"
    private const val FIELD_SEPARATOR = "~"

    fun build(
        rows: List<CommandStickRow>,
        categoryNames: Map<Int, String>,
        registered: Map<String, String?>,
    ): List<CommandCategory> {
        val listed = rows.flatMapTo(HashSet()) { listOf(it.command) + it.aliases }
        val entries =
            rows
                .filter { it.command in registered }
                .groupBy({ if (it.category in categoryNames) it.category else OTHER_CATEGORY }) {
                    it.toEntry(registered[it.command])
                }
        val other =
            registered.keys
                .filter { it !in listed }
                .sorted()
                .map {
                    CommandEntry(it, "::$it", registered[it].orEmpty(), "", emptyList(), false)
                }
        return categoryNames.keys.sorted().mapNotNull { key ->
            val list = entries[key].orEmpty() + if (key == OTHER_CATEGORY) other else emptyList()
            if (list.isEmpty()) null else CommandCategory(categoryNames.getValue(key), list)
        }
    }

    fun run(entry: CommandEntry, answers: List<String>): CommandRun {
        val args =
            (listOf(entry.fixedArgs) + answers)
                .joinToString(" ")
                .lowercase()
                .split(" ")
                .filter { it.isNotEmpty() }
        return CommandRun(entry.command, args)
    }

    fun encodeNames(categories: List<CommandCategory>): String =
        categories.joinToString(ENTRY_SEPARATOR) { it.name.clean() }

    fun encodeEntries(entries: List<CommandEntry>): String =
        entries.joinToString(ENTRY_SEPARATOR) {
            "${it.name.clean()}$FIELD_SEPARATOR${it.description.clean()}"
        }

    private fun CommandStickRow.toEntry(commandDescription: String?): CommandEntry =
        CommandEntry(
            command = command,
            name = name,
            description = description ?: commandDescription.orEmpty(),
            fixedArgs = fixedArgs,
            prompts = prompts,
            confirm = confirm,
        )

    private fun String.clean(): String =
        replace(ENTRY_SEPARATOR, "/").replace(FIELD_SEPARATOR, "-")
}
