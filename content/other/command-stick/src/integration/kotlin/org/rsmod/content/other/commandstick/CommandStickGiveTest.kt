package org.rsmod.content.other.commandstick

import dev.or2.central.account.Rights
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.cheat.CheatCommandMap
import org.rsmod.game.inv.InvObj

@Execution(ExecutionMode.SAME_THREAD)
class CommandStickGiveTest {
    @Test
    fun GameTestState.`gives exactly one stick`() =
        runStickTest { deps ->
            deps.cheats.execute(player, COMMAND, emptyList())

            assertEquals(1, player.inv.count(STICK))
        }

    @Test
    fun GameTestState.`a full inventory gets nothing and keeps every item`() =
        runStickTest { deps ->
            player.fillInv()
            val before = player.inv.objs.toList()

            deps.cheats.execute(player, COMMAND, emptyList())

            assertEquals(before, player.inv.objs.toList())
            assertEquals(0, player.inv.count(STICK))
        }

    @Test
    fun GameTestState.`no second stick while one is carried`() =
        runStickTest { deps ->
            player.inv[0] = InvObj(STICK, 1)

            deps.cheats.execute(player, COMMAND, emptyList())

            assertEquals(1, player.inv.count(STICK))
        }

    @Test
    fun GameTestState.`no second stick while one is banked`() =
        runStickTest { deps ->
            val bank = player.invMap.getOrPut("inv.bank")
            bank[0] = InvObj(STICK, 1)

            deps.cheats.execute(player, COMMAND, emptyList())

            assertEquals(0, player.inv.count(STICK))
            assertEquals(1, bank.count(STICK))
        }

    @Test
    fun GameTestState.`repeated use in one tick gives one stick`() =
        runStickTest { deps ->
            repeat(3) { deps.cheats.execute(player, COMMAND, emptyList()) }

            assertEquals(1, player.inv.count(STICK))
        }

    @Test
    fun GameTestState.`non administrators get nothing`() =
        runStickTest { deps ->
            player.modLevel = Rights.NONE

            deps.cheats.execute(player, COMMAND, emptyList())

            assertEquals(0, player.inv.count(STICK))
        }

    private fun GameTestState.runStickTest(body: GameTestScope.(Deps) -> Unit) =
        runInjectedGameTest(Deps::class, null, CommandStickScript::class) { deps ->
            player.clearAllInvs()
            player.modLevel = Rights.ADMINISTRATOR
            body(deps)
        }

    class Deps @Inject constructor(val cheats: CheatCommandMap)

    private companion object {
        const val COMMAND = "commandstick"
        const val STICK = "obj.command_stick"
    }
}
