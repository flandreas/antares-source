package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class WireTapTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHandler = ForwardSignalHandler()

	@Test
	fun shouldConcentrateSingleBit() {
		val wireTap = WireTap(PortCount.TWO, BitWidth.BW_4, BitWidth.BW_1)
		wireTap.setTapPositions(listOf(0, 2))

		wireTap.getInput<Any>(2).setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		wireTap.getInput<Any>(3).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		wireTap.act(signalHandler, wireTap.createActorData(wireTap.getInput<DigitalSignal>(3)))

		assertEquals(DigitalSignalFactory.ofBits(listOf(False, Undefined, True, Undefined)), wireTap.getOutput<DigitalSignal>(1).getOutgoingSignal())
	}

	@Test
	fun shouldConcentrateMultiBit() {
		val wireTap = WireTap(PortCount.TWO, BitWidth.BW_8, BitWidth.BW_2)
		wireTap.setTapPositions(listOf(0, 4))

		wireTap.getInput<Any>(2).setIncomingSignal(DigitalSignalFactory.ofBits(listOf(False, False)), signalHandler)
		wireTap.getInput<Any>(3).setIncomingSignal(DigitalSignalFactory.ofBits(listOf(True, True)), signalHandler)
		wireTap.act(signalHandler, wireTap.createActorData(wireTap.getInput<DigitalSignal>(3)))

		assertEquals(DigitalSignalFactory.ofBits(listOf(False, False, Undefined, Undefined, True, True, Undefined, Undefined)), wireTap.getOutput<DigitalSignal>(1).getOutgoingSignal())
	}

	@Test
	fun shouldSplitSingleBit() {
		val wireTap = WireTap(PortCount.TWO, BitWidth.BW_4, BitWidth.BW_1)
		wireTap.setTapPositions( listOf(0, 2))

		wireTap.getInput<Any>(1).setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_4, 4), signalHandler)

		assertEquals(DigitalSignalFactory.of(False), wireTap.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertEquals(DigitalSignalFactory.of(True), wireTap.getOutput<DigitalSignal>(3).getOutgoingSignal())
	}

	@Test
	fun shouldSplitMultiBit() {
		val wireTap = WireTap(PortCount.TWO, BitWidth.BW_8, BitWidth.BW_2)
		wireTap.setTapPositions(listOf(0, 4))

		wireTap.getInput<Any>(1).setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_8, 2 + 16), signalHandler)

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_2, 2), wireTap.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertEquals(DigitalSignalFactory.of(BitWidth.BW_2, 1), wireTap.getOutput<DigitalSignal>(3).getOutgoingSignal())
	}
}