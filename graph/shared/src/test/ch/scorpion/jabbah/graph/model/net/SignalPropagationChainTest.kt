package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphModelTestRule
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.model.TestVerticeString
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue

class SignalPropagationChainTest {

	companion object {
		init {
			GraphModelTestRule.configure()
		}
	}

	private val signalHandler = mockk<SignalHandler>()
	private val vertice = TestVerticeString()
	private val dummyTransmitter = TestVerticeString()
	private val chain = SignalPropagationChain<String>(vertice.getOutput())

	@Test
	fun shouldConvertSignal() {
		vertice.getOutput<String>().setOutgoingSignalBuffered("ABC", signalHandler)
		chain.extendHead(TestConverter("C"), dummyTransmitter.getInput(), dummyTransmitter.getOutput())
		chain.extendHead(TestConverter("B"), dummyTransmitter.getInput(), dummyTransmitter.getOutput())

		assertTrue(chain.isConsistentWith("A"))
	}

	private class TestConverter(private val appendix: String) : SignalConverter<String> {
		override fun convert(signal: String?): String = "$signal$appendix"
	}
}