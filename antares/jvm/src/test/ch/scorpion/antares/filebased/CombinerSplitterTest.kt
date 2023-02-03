package ch.scorpion.antares.filebased

import ch.scorpion.antares.checkCombinedNetAccess
import ch.scorpion.antares.model.inout.DigitalCircuitInOut
import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class CombinerSplitterTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private lateinit var a0: DigitalCircuitInOut
	private lateinit var a3: DigitalCircuitInOut
	private lateinit var b0: DigitalCircuitInOut
	private lateinit var b3: DigitalCircuitInOut

	private lateinit var combinerA4: BidirectionalSplitter
	private lateinit var splitterA4: BidirectionalSplitter
	private lateinit var combinerB4: BidirectionalSplitter
	private lateinit var splitterB4: BidirectionalSplitter

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("90132379-7ec1-48bb-bfe3-ece4d86b6ee9"))

		a0 = openedCircuitView.graph!!.withId(13) as DigitalCircuitInOut
		a3 = openedCircuitView.graph!!.withId(16) as DigitalCircuitInOut

		combinerA4 = openedCircuitView.graph!!.withId(1) as BidirectionalSplitter
		splitterA4 = openedCircuitView.graph!!.withId(2) as BidirectionalSplitter
		combinerB4 = openedCircuitView.graph!!.withId(4) as BidirectionalSplitter
		splitterB4 = openedCircuitView.graph!!.withId(5) as BidirectionalSplitter

		b0 = openedCircuitView.graph!!.withId(17) as DigitalCircuitInOut
		b3 = openedCircuitView.graph!!.withId(20) as DigitalCircuitInOut

		startSimulation()
	}

	@Test
	fun shouldCreateCombinedNetsOfSplitterB4() {
		val combinedNets = splitterB4.createCombinedNetsFor(
			combinerB4.getOutput(1),
			splitterB4.getInput(1),
			scheduler)
		assertEquals(4, combinedNets.size)

		checkCombinedNetAccess(
			combinedNets.first { it.accessOf(b0.getOutput()) != null } as CombinedNet<DigitalSignal>,
			combinerB4.getOutput(1), BitWidth.BW_1, 0,
			b0.getOutput(), BitWidth.BW_1, 0
		)

		checkCombinedNetAccess(
			combinedNets.first { it.accessOf(b3.getOutput()) != null } as CombinedNet<DigitalSignal>,
			combinerB4.getOutput(1), BitWidth.BW_1, 3,
			b3.getOutput(), BitWidth.BW_1, 0
		)
	}

	@Test
	fun shouldCreateCombinedNetsOfCombinerB4Low() {
		val combinedNets = combinerB4.createCombinedNetsFor(
			splitterA4.getOutput(2),
			combinerB4.getInput(2),
			scheduler)
		assertEquals(2, combinedNets.size)

		checkCombinedNetAccess(
			combinedNets.first { it.accessOf(b0.getOutput()) != null } as CombinedNet<DigitalSignal>,
			splitterA4.getOutput(2), BitWidth.BW_1, 0,
			b0.getOutput(), BitWidth.BW_1, 0)
	}

	@Test
	fun shouldCreateCombinedNetsOfCombinerB4High() {
		val combinedNets = combinerB4.createCombinedNetsFor(
			splitterA4.getOutput(3),
			combinerB4.getInput(3),
			scheduler)
		assertEquals(2, combinedNets.size)

		checkCombinedNetAccess(
			combinedNets.first { it.accessOf(b3.getOutput()) != null } as CombinedNet<DigitalSignal>,
			splitterA4.getOutput(3), BitWidth.BW_1, 1,
			b3.getOutput(), BitWidth.BW_1, 0)
	}

	@Test
	fun shouldCreateCombinedNetsOfSplitterA4() {
		val combinedNets = splitterA4.createCombinedNetsFor(
			combinerA4.getOutput(1),
			splitterA4.getInput(1),
			scheduler)
		assertEquals(4, combinedNets.size)

		checkCombinedNetAccess(
			combinedNets.first { it.accessOf(b0.getOutput()) != null } as CombinedNet<DigitalSignal>,
			combinerA4.getOutput(1), BitWidth.BW_1, 0,
			b0.getOutput(), BitWidth.BW_1, 0)

		checkCombinedNetAccess(
			combinedNets.first { it.accessOf(b3.getOutput()) != null } as CombinedNet<DigitalSignal>,
			combinerA4.getOutput(1), BitWidth.BW_1, 3,
			b3.getOutput(), BitWidth.BW_1, 0)
	}

	@Test
	fun shouldFormCombinedNetOfA0() {
		assertEquals(1, a0.getOutput<DigitalSignal>().combinedNets.size)
		assertSame(
			b0.getOutput(),
			a0.getOutput<DigitalSignal>().combinedNets.iterator().next().accesses
				.first { it.port !== a0.getOutput<DigitalSignal>() }
				.port)
	}

	@Test
	fun shouldFormCombinedNetOfA3() {
		assertEquals(1, a3.getOutput<DigitalSignal>().combinedNets.size)
		assertSame(
			b3.getOutput(),
			a3.getOutput<DigitalSignal>().combinedNets.iterator().next().accesses
				.first { it.port !== a3.getOutput<DigitalSignal>() }
				.port)
	}
}