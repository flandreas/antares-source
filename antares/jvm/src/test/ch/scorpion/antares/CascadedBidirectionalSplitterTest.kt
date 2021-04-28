package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOut
import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.net.DigitalCombinedNetAccess
import ch.scorpion.antares.model.net.PullResistor
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import org.junit.Test
import kotlin.test.*

class CascadedBidirectionalSplitterTest : AbstractFileBasedTest() {

	companion object {
		init {
			configure()
		}
	}

	private lateinit var inoutA: CircuitInOut
	private lateinit var pullResistor: PullResistor
	private lateinit var inoutIO1: CircuitInOut
	private lateinit var inoutIO2: CircuitInOut
	private lateinit var splitter: BidirectionalSplitter

	@BeforeTest
	fun openAndStartCircuit() {
		openCircuit(UUID("37f1ed5f-b729-49a8-9650-51ee785d82c3"))

		inoutA = openedCircuitView.graph!!.withId(1) as CircuitInOut
		pullResistor = openedCircuitView.graph!!.withId(12) as PullResistor
		inoutIO1 = openedCircuitView.graph!!.withId(5) as CircuitInOut
		inoutIO2 = openedCircuitView.graph!!.withId(6) as CircuitInOut
		splitter = openedCircuitView.graph!!.withId(3) as BidirectionalSplitter

		startSimulation()
		proceedUntilQueueIsEmpty()
	}

	@Test
	fun shouldFormSplitCombinedNets() {
		val combinedNets = inoutA.getOutput<DigitalSignal>().combinedNets
		assertEquals(3, combinedNets.size)

		checkCombinedNetAccess(
			combinedNets.first { it.accessOf(pullResistor.getOutput()) != null },
			inoutA.getOutput(), BitWidth.BW_1, 0,
			pullResistor.getOutput(), BitWidth.BW_1, 0
		)

		checkCombinedNetAccess(
			combinedNets.first { it.accessOf(inoutIO1.getOutput()) != null },
			inoutA.getOutput(), BitWidth.BW_1, 1,
			inoutIO1.getOutput(), BitWidth.BW_1, 0
		)

		checkCombinedNetAccess(
			combinedNets.first { it.accessOf(inoutIO2.getOutput()) != null },
			inoutA.getOutput(), BitWidth.BW_2, 1,
			inoutIO2.getOutput(), BitWidth.BW_2, 0
		)
	}

	@Test
	fun shouldFormConcentratedCombinedNets() {
		assertEquals(1, pullResistor.getOutput<DigitalSignal>().combinedNets.size)
		assertEquals(1, inoutIO1.getOutput<DigitalSignal>().combinedNets.size)
		assertEquals(1, inoutIO2.getOutput<DigitalSignal>().combinedNets.size)

		checkCombinedNetAccess(
			pullResistor.getOutput<DigitalSignal>().combinedNets.iterator().next(),
			pullResistor.getOutput(), BitWidth.BW_1, 0,
			inoutA.getOutput(), BitWidth.BW_1, 0
		)

		checkCombinedNetAccess(
			inoutIO1.getOutput<DigitalSignal>().combinedNets.iterator().next(),
			inoutIO1.getOutput(), BitWidth.BW_1, 0,
			inoutA.getOutput(), BitWidth.BW_1, 1
		)

		checkCombinedNetAccess(
			inoutIO2.getOutput<DigitalSignal>().combinedNets.iterator().next(),
			inoutIO2.getOutput(), BitWidth.BW_2, 0,
			inoutA.getOutput(), BitWidth.BW_2, 1
		)
	}

	private fun checkCombinedNetAccess(
		combinedNet: CombinedNet<DigitalSignal>,
		port1: OutputPort<DigitalSignal>,
		width1: BitWidth,
		index1: Int,
		port2: OutputPort<DigitalSignal>,
		width2: BitWidth,
		index2: Int,
	) {
		assertEquals(width1, (combinedNet.accessOf(port1) as DigitalCombinedNetAccess).width)
		assertEquals(index1, (combinedNet.accessOf(port1) as DigitalCombinedNetAccess).index)

		assertEquals(width2, (combinedNet.accessOf(port2) as DigitalCombinedNetAccess).width)
		assertEquals(index2, (combinedNet.accessOf(port2) as DigitalCombinedNetAccess).index)
	}

	@Test
	fun shouldForwardSignalFromWideToNarrow() {
		inoutA.setIncomingSignal(Word(listOf(False, False, True, Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(False), inoutIO1.signal)
		assertEquals(Word(listOf(True, Undefined)), inoutIO2.signal)
	}

	@Test
	fun shouldForwardSignalFromNarrowToWide() {
		inoutIO1.setIncomingSignal(Word.of(True), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word(listOf(False, True, Undefined, Undefined)), inoutA.signal)
	}

	@Test
	fun shouldNotSetExecutionErrorForDefinedButEqualSignals() {
		inoutIO1.setIncomingSignal(Word.of(True), scheduler)
		proceedUntilQueueIsEmpty()

		inoutA.setIncomingSignal(Word(listOf(Undefined, True, Undefined, Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		assertNull(inoutA.getOutput<DigitalSignal>().net?.executionError)
		assertEquals(Word.of(True), inoutIO1.signal)
	}

	@Test
	fun shouldSetExecutionErrorForConflictingSignals() {
		inoutIO1.setIncomingSignal(Word.of(True), scheduler)
		proceedUntilQueueIsEmpty()

		inoutA.setIncomingSignal(Word(listOf(Undefined, False, Undefined, Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		assertNotNull(inoutA.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldWithdrawOtherWeakSignal() {
		inoutA.setIncomingSignal(Word(listOf(True, Undefined, Undefined, Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(true), splitter.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertNull(inoutA.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldWithdrawOwnUndefinedSignal() {
		inoutA.setIncomingSignal(Word(listOf(True, Undefined, Undefined, Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		inoutA.setIncomingSignal(Word(listOf(Undefined, Undefined, Undefined, Undefined)), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(Word.of(false), splitter.getOutput<DigitalSignal>(2).getOutgoingSignal())
		assertNull(inoutA.getOutput<DigitalSignal>().net?.executionError)
	}

	@Test
	fun shouldApplyWeakSignalAtStartup() {
		assertEquals(Word(listOf(False, Undefined, Undefined, Undefined)), inoutA.signal)
	}

	@AfterTest
	fun cleanup() {
		stopSimulation()
	}
}