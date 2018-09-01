package ch.scorpion.antares.model.arithmetic

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.MathJvm
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.ClassRule
import org.junit.Test

class RandomTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = AntaresTestRule()
	}

	private val signalHandler = ForwardSignalHandler()

	@Test
	fun shouldProduceRandom() {
		val random = Random()
		withNextRandom(42)

		random.getInput<DigitalSignal>().setIncomingSignal(Word.of(Bit.True), signalHandler)
		random.act(signalHandler, random.createActorData(random.getInput<DigitalSignal>()))

		assertThat(random.getOutput<DigitalSignal>().getOutgoingSignal() as Word, `is`(Word.of(random.bitWidth, 42)))
	}

	@Test
	fun shouldProduceOnlyOnRaisingEdge() {
		val random = Random()
		withNextRandom(42)
		random.getInput<DigitalSignal>().setIncomingSignal(Word.of(Bit.True), signalHandler)
		random.act(signalHandler, random.createActorData(random.getInput<DigitalSignal>()))

		withNextRandom(99)
		random.getInput<DigitalSignal>().setIncomingSignal(Word.of(Bit.False), signalHandler)
		random.act(signalHandler, random.createActorData(random.getInput<DigitalSignal>()))

		assertThat(random.getOutput<DigitalSignal>().getOutgoingSignal() as Word, `is`(Word.of(random.bitWidth, 42)))
	}

	private fun withNextRandom(value: Int) {
		Math = MathJvm { value / 255.0}
	}
}