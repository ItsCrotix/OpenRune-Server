package org.rsmod.content.skills.agility

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Werewolf course's lap bonus: the stick the Agility Boss throws past the hurdles, picked up
 * after the pipes and handed to the Agility Trainer at the bottom of the zip line
 * (https://oldschool.runescape.wiki/w/Werewolf_Agility_Course).
 */
class WerewolfStickScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.werewolf_trainer_stick") { giveStick() }
    }

    private fun ProtectedAccess.giveStick() {
        if (invDel(inv, STICK, count = 1).failure) {
            // UNVERIFIED: wording guessed.
            mes("You don't have a stick to give the trainer.")
            return
        }
        statAdvance(AGILITY, STICK_XP)
    }

    private companion object {
        const val STICK = "obj.waa_stick"
        const val AGILITY = "stat.agility"
        const val STICK_XP = 380.0
    }
}
