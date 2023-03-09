package ch.scorpion.antares.model.arithmetic

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class RandomTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val valueProvider = RandomProvider()
	private val signalHandler = ForwardSignalHandler(mockk())

	@Test
	fun shouldProduceRandom() {
		val random = Random(valueProvider::provide)
		valueProvider.nextValue = 42UL

		random.getInput<DigitalSignal>().setIncomingSignal(DigitalSignalFactory.of(Bit.True), signalHandler)
		random.act(signalHandler, random.createActorData(random.getInput<DigitalSignal>()))

		assertEquals(DigitalSignalFactory.of(random.bitWidth, 42), random.getOutput<DigitalSignal>().getOutgoingSignal())
	}

	@Test
	fun shouldProduceOnlyOnRaisingEdge() {
		val random = Random(valueProvider::provide)
		valueProvider.nextValue = 42UL
		random.getInput<DigitalSignal>().setIncomingSignal(DigitalSignalFactory.of(Bit.True), signalHandler)
		random.act(signalHandler, random.createActorData(random.getInput<DigitalSignal>()))

		valueProvider.nextValue = 99UL
		random.getInput<DigitalSignal>().setIncomingSignal(DigitalSignalFactory.of(Bit.False), signalHandler)
		random.act(signalHandler, random.createActorData(random.getInput<DigitalSignal>()))

		assertEquals(DigitalSignalFactory.of(random.bitWidth, 42), random.getOutput<DigitalSignal>().getOutgoingSignal())
	}

	private class RandomProvider {
		var nextValue: ULong = 0UL

		fun provide (@Suppress("UNUSED_PARAMETER") max: ULong): ULong {
			return nextValue
		}
	}
}