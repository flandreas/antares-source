package ch.scorpion.antares.model.arithmetic

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class BitExtenderTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHandler = ForwardSignalHandler(mockk())

	@Test
	fun shouldExtendSingle() {
		val extender = BitExtender(BitWidth.BW_1, BitWidth.BW_8)
		extender.digitalInput.setIncomingSignal(DigitalSignalFactory.of(Bit.False), signalHandler)

		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_8, Bit.False), extender.digitalOutput.getOutgoingSignal())
	}

	@Test
	fun shouldExtendSingle1() {
		val extender = BitExtender(BitWidth.BW_1, BitWidth.BW_8)
		extender.digitalInput.setIncomingSignal(DigitalSignalFactory.of(Bit.True), signalHandler)

		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_8, Bit.True), extender.digitalOutput.getOutgoingSignal())
	}

	@Test
	fun shouldExtendSigned0() {
		val extender = BitExtender(BitWidth.BW_4, BitWidth.BW_8)
		extender.digitalInput.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_4, 7L), signalHandler)

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_8,  7L), extender.digitalOutput.getOutgoingSignal())
	}

	@Test
	fun shouldExtendSigned1() {
		val extender = BitExtender(BitWidth.BW_4, BitWidth.BW_8)
		extender.digitalInput.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_4, 8L + 1L), signalHandler)

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_8, 249), extender.digitalOutput.getOutgoingSignal())
	}
}