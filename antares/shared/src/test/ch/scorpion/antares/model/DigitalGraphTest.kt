package ch.scorpion.antares.model

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestTranslationsBuilder
import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import com.nhaarman.mockitokotlin2.mock
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [DigitalGraph].
 */
class DigitalGraphTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    @Before
    fun setup() {
        TestTranslationsBuilder().withAnyKey()
    }

    @Test
    fun shouldForwardTunnelSignal() {
        val signalHandler = ForwardSignalHandler()
        val testGraph = DigitalGraph(eventBus = mock<EventBus>())

        val tunnel1 = Tunnel("Test")
        testGraph.add(tunnel1)
        val tunnel2 = Tunnel("Test")
        testGraph.add(tunnel2)

        tunnel1.getInput<DigitalSignal>().setIncomingSignal(Word.of(true), signalHandler)

        assertThat(tunnel2.getOutput<DigitalSignal>().getOutgoingSignal() as Word, CoreMatchers.`is`(Word.of(true)))
    }
}