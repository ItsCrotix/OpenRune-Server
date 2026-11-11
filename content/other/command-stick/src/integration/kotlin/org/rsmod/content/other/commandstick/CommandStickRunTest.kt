package org.rsmod.content.other.commandstick

import dev.or2.central.account.Rights
import jakarta.inject.Inject
import java.util.concurrent.atomic.AtomicInteger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.cheat.CheatCommandMap
import org.rsmod.game.cheat.CheatHandler

@Execution(ExecutionMode.SAME_THREAD)
class CommandStickRunTest {
    @Test
    fun GameTestState.`a queued run executes once for an administrator`() =
        runProbeTest(Rights.ADMINISTRATOR, "stickprobe_admin") { runs -> assertEquals(1, runs) }

    @Test
    fun GameTestState.`a queued run never executes for other players`() =
        runProbeTest(Rights.NONE, "stickprobe_player") { runs -> assertEquals(0, runs) }

    private fun GameTestState.runProbeTest(
        rights: Rights,
        probe: String,
        verify: GameTestScope.(Int) -> Unit,
    ) = runInjectedGameTest(Deps::class, null, CommandStickScript::class) { deps ->
        val runs = AtomicInteger()
        deps.cheats.put(probe, CheatHandler("Probe", { runs.incrementAndGet() }, null))
        player.modLevel = rights

        player.softQueue("queue.command_stick_run", 1, CommandRun(probe, emptyList()))
        advance(ticks = 3)

        verify(runs.get())
    }

    class Deps @Inject constructor(val cheats: CheatCommandMap)
}
