package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class SplitterTest {

	private val signalHandler = ForwardSignalHandler(CurrentSystemSpeedCategory(SystemSpeed()))

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldSplitToBits() {
		val splitter = Splitter(BitWidth.BW_4, BranchCount.BC_4)

		splitter.getInput<Any>().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_4, 6L), signalHandler)
		splitter.act(signalHandler, splitter.createActorData(splitter.getInput<DigitalSignal>()))

		assertEquals(Bit.False, (splitter.getOutput<DigitalSignal>(2).getOutgoingSignal() as DigitalSignal).bitAt(0))
		assertEquals(Bit.True, (splitter.getOutput<Any>(3).getOutgoingSignal() as DigitalSignal).bitAt(0))
		assertEquals(Bit.True, (splitter.getOutput<Any>(4).getOutgoingSignal() as DigitalSignal).bitAt(0))
		assertEquals(Bit.False, (splitter.getOutput<Any>(5).getOutgoingSignal() as DigitalSignal).bitAt(0))
	}

	@Test
	fun shouldSplitToSubwords() {
		val splitter = Splitter(BitWidth.BW_8, BranchCount.BC_4)

		splitter.getInput<Any>().setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 255L), signalHandler)
		splitter.act(signalHandler, splitter.createActorData(splitter.getInput<DigitalSignal>()))

		assertEquals(BitWidth.BW_2, (splitter.getOutput<Any>(2).getOutgoingSignal() as DigitalSignal).bitWidth)
		assertEquals(3UL, (splitter.getOutput<Any>(2).getOutgoingSignal() as DigitalSignal).getValue())

		assertEquals(BitWidth.BW_2, (splitter.getOutput<Any>(3).getOutgoingSignal() as DigitalSignal).bitWidth)
		assertEquals(3UL, (splitter.getOutput<Any>(3).getOutgoingSignal() as DigitalSignal).getValue())

		assertEquals(BitWidth.BW_2, (splitter.getOutput<Any>(4).getOutgoingSignal() as DigitalSignal).bitWidth)
		assertEquals(3UL, (splitter.getOutput<Any>(4).getOutgoingSignal() as DigitalSignal).getValue())

		assertEquals(BitWidth.BW_2, (splitter.getOutput<Any>(5).getOutgoingSignal() as DigitalSignal).bitWidth)
		assertEquals(3UL, (splitter.getOutput<Any>(5).getOutgoingSignal() as DigitalSignal).getValue())
	}

	@Test
	fun shouldSetBitWidth28() {
		val splitter = Splitter(BitWidth.BW_8, BranchCount.BC_4)

		splitter.bitWidth = BitWidth.BW_28
	}
}