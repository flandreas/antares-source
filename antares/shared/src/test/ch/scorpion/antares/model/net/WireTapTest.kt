package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_1
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_2
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_4
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_6
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.ForwardSignalHandler
import ch.scorpion.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WireTapTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val signalHandler = ForwardSignalHandler()

	@Test
	fun shouldInstantiate() {
		val wireTap = WireTap(BW_4, BW_1, PortCount.TWO)

		assertEquals(3, wireTap.portsCount)
		assertEquals("0", wireTap.getPort<DigitalSignal>(2).name)
		assertEquals("1", wireTap.getPort<DigitalSignal>(3).name)
	}

	@Test
	fun shouldSetTapPosition() {
		val wireTap = WireTap(BW_4, BW_1, PortCount.TWO)

		wireTap.setTapPosition(1, 2)

		assertEquals(2, wireTap.getTapPosition(1))
		assertEquals("0", wireTap.getPort<DigitalSignal>(2).name)
		assertEquals("2", wireTap.getPort<DigitalSignal>(3).name)
	}

	@Test
	fun shouldRejectIntersectingTapPosition() {
		val wireTap = WireTap(BW_4, BW_1, PortCount.TWO)

		assertFailsWith(IllegalArgumentException::class) {
			wireTap.setTapPosition(1, 0)
		}
	}

	@Test
	fun shouldBeStorable() {
		val wireTap = WireTap(BW_4, BW_1, PortCount.TWO)

		val clone = StorableCloner.clone(wireTap)

		assertEquals(3, clone.portsCount)
		assertEquals("0", clone.getPort<DigitalSignal>(2).name)
		assertEquals("1", clone.getPort<DigitalSignal>(3).name)
	}

	@Test
	fun shouldResetTapPositionsWhenChangingWideBitWidth() {
		val wireTap = WireTap(BW_4, BW_1, PortCount.TWO)
		wireTap.setTapPosition(1, 2)

		wireTap.bitWidth = BW_6

		assertEquals(BW_6, wireTap.bitWidth)
		assertEquals(3, wireTap.portsCount)
		assertEquals("0", wireTap.getPort<DigitalSignal>(2).name)
		assertEquals("1", wireTap.getPort<DigitalSignal>(3).name)
	}

	@Test
	fun shouldResetTapPositionsWhenChangingNarrowSideBitWidth() {
		val wireTap = WireTap(BW_4, BW_1, PortCount.TWO)

		wireTap.narrowSideBitWidth = BW_2

		assertEquals(BW_2, wireTap.narrowSideBitWidth)
		assertEquals("0..1", wireTap.getPort<DigitalSignal>(2).name)
		assertEquals("2..3", wireTap.getPort<DigitalSignal>(3).name)
	}

	@Test
	fun shouldAdjustTapsWhenReducingWideBitWidth() {
		val wireTap = WireTap(BW_4, BW_2, PortCount.TWO)
		wireTap.setTapPosition(1, 2)

		wireTap.bitWidth = BW_2

		assertEquals(BW_2, wireTap.bitWidth)
		assertEquals(BW_1, wireTap.narrowSideBitWidth)
		assertEquals(PortCount.ONE, wireTap.tapCount)
		assertEquals("0", wireTap.getPort<DigitalSignal>(2).name)
	}

	@Test
	fun shouldResetTapPositionWhenAddingNarrowPort() {
		val wireTap = WireTap(BW_4, BW_1, PortCount.TWO)

		wireTap.addNarrowPorts(1)

		assertEquals(4, wireTap.portsCount)
		assertEquals(3, wireTap.tapCount.count)
		assertEquals(BW_1, wireTap.narrowSideBitWidth)
		assertEquals("0", wireTap.getPort<DigitalSignal>(2).name)
		assertEquals("1", wireTap.getPort<DigitalSignal>(3).name)
		assertEquals("2", wireTap.getPort<DigitalSignal>(4).name)
	}

	@Test
	fun shouldRemoveLastNarrowPort() {
		val wireTap = WireTap(BW_4, BW_1, PortCount.TWO)
		wireTap.setTapPosition(0, 2)

		wireTap.removePort(wireTap.getPort<DigitalSignal>(3))

		assertEquals(2, wireTap.portsCount)
		assertEquals(1, wireTap.tapCount.count)
		assertEquals(BW_1, wireTap.narrowSideBitWidth)
		assertEquals("2", wireTap.getPort<DigitalSignal>(2).name)
	}

	@Test
	fun shouldConcentrateSingleBit() {
		val wireTap = WireTap(BW_4, BW_1, PortCount.TWO)
		wireTap.setTapPosition(1, 2)

		wireTap.getInput<Any>(2).setIncomingSignal(DigitalSignalFactory.of(false), signalHandler)
		wireTap.getInput<Any>(3).setIncomingSignal(DigitalSignalFactory.of(true), signalHandler)
		wireTap.act(signalHandler, wireTap.createActorData(wireTap.getInput<DigitalSignal>(3)))

		assertEquals(DigitalSignalFactory.ofBits(listOf(False, Undefined, True, Undefined)), wireTap.getOutput<DigitalSignal>(1).getOutgoingSignal())
	}

	@Test
	fun shouldConcentrateMultiBit() {
		val wireTap = WireTap(BW_8, BW_2, PortCount.TWO)
		wireTap.setTapPosition(1, 4)

		wireTap.getInput<Any>(2).setIncomingSignal(DigitalSignalFactory.ofBits(listOf(False, False)), signalHandler)
		wireTap.getInput<Any>(3).setIncomingSignal(DigitalSignalFactory.ofBits(listOf(True, True)), signalHandler)
		wireTap.act(signalHandler, wireTap.createActorData(wireTap.getInput<DigitalSignal>(3)))

		assertEquals(DigitalSignalFactory.ofBits(listOf(False, False, Undefined, Undefined,
			True, True, Undefined, Undefined)), wireTap.getOutput<DigitalSignal>(1).getOutgoingSignal())
	}

	@Test
	fun shouldSplitSingleBit() {
		val wireTap = WireTap(BW_4, BW_1, PortCount.TWO)
		wireTap.setTapPosition(1, 2)

		wireTap.getInput<Any>(1).setIncomingSignal(DigitalSignalFactory.of(BW_4, 4), signalHandler)

		assertEquals(DigitalSignalFactory.of(False), wireTap.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertEquals(DigitalSignalFactory.of(True), wireTap.getOutput<DigitalSignal>(3).getOutgoingSignal())
	}

	@Test
	fun shouldSplitMultiBit() {
		val wireTap = WireTap(BW_8, BW_2, PortCount.TWO)
		wireTap.setTapPosition(1, 4)

		wireTap.getInput<Any>(1).setIncomingSignal(DigitalSignalFactory.of(BW_8, 2 + 16), signalHandler)

		assertEquals(DigitalSignalFactory.of(BW_2, 2), wireTap.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertEquals(DigitalSignalFactory.of(BW_2, 1), wireTap.getOutput<DigitalSignal>(3).getOutgoingSignal())
	}
}