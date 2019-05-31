package ch.scorpion.antares.model.arithmetic

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class RandomTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val valueProvider = RandomProvider()
	private val signalHandler = ForwardSignalHandler()

	@Test
	fun shouldProduceRandom() {
		val random = Random(valueProvider::provide)
		valueProvider.nextValue = 42

		random.getInput<DigitalSignal>().setIncomingSignal(Word.of(Bit.True), signalHandler)
		random.act(signalHandler, random.createActorData(random.getInput<DigitalSignal>()))

		assertEquals(Word.of(random.bitWidth, 42), random.getOutput<DigitalSignal>().getOutgoingSignal() as Word)
	}

	@Test
	fun shouldProduceOnlyOnRaisingEdge() {
		val random = Random(valueProvider::provide)
		valueProvider.nextValue = 42
		random.getInput<DigitalSignal>().setIncomingSignal(Word.of(Bit.True), signalHandler)
		random.act(signalHandler, random.createActorData(random.getInput<DigitalSignal>()))

		valueProvider.nextValue = 99
		random.getInput<DigitalSignal>().setIncomingSignal(Word.of(Bit.False), signalHandler)
		random.act(signalHandler, random.createActorData(random.getInput<DigitalSignal>()))

		assertEquals(Word.of(random.bitWidth, 42), random.getOutput<DigitalSignal>().getOutgoingSignal() as Word)
	}

	private class RandomProvider {
		var nextValue: Long = 0

		fun provide (max: Long): Long {
			return nextValue
		}
	}
}