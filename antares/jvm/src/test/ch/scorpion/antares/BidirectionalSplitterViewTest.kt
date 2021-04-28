package ch.scorpion.antares

import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.net.BidirectionalSplitter
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.net.DigitalCombinedNetAccess
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.net.BidirectionalSplitterView
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BidirectionalSplitterViewTest : AbstractJvmCircuitTest() {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private lateinit var circuitView: GraphView
	private val bidiSplitterView = BidirectionalSplitterView(model = BidirectionalSplitter(BitWidth.BW_2, BranchCount.BC_2))
	private val a = CircuitInOutView(model = CircuitInOutImpl(name = "A", bitWidth = BitWidth.BW_2, portType = PortType.INOUT))
	private val in0 = CircuitInOutView(model = CircuitInOutImpl(name = "I0", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))
	private val in1 = CircuitInOutView(model = CircuitInOutImpl(name = "I1", bitWidth = BitWidth.BW_1, portType = PortType.INOUT))

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		val builder = TestCircuitBuilder("test", styleProvider, eventBus)

		builder.addVerticeView(a)
		builder.addVerticeView(bidiSplitterView)
		builder.addVerticeView(in0)
		builder.addVerticeView(in1)

		builder.connect(a, bidiSplitterView, bidiSplitterView.model.getInput(1))
		builder.connect(bidiSplitterView, bidiSplitterView.model.getOutput(2), in0)
		builder.connect(bidiSplitterView, bidiSplitterView.model.getOutput(3), in1)
		circuitView = builder.build()
	}

	@Test
	fun shouldCreateNarrowCombinedNets() {
		val combinedNets = bidiSplitterView.model.createCombinedNetsFor(
			a.model.getOutput<DigitalSignal>(),
			bidiSplitterView.model.getInput(1),
			scheduler)

		assertEquals(2, combinedNets.size)

		// A to I0
		assertNotNull(
			combinedNets
				.filter { it.accessOf(in0.model.getOutput()) != null }
				.mapNotNull { it.accessOf(a.model.getOutput()) as DigitalCombinedNetAccess? }
				.find { it.index == 0 && it.width == BitWidth.BW_1 })

		assertNotNull(
			combinedNets
				.filter { it.accessOf(a.model.getOutput()) != null }
				.mapNotNull { it.accessOf(in0.model.getOutput()) as DigitalCombinedNetAccess? }
				.find { it.index == 0 && it.width == BitWidth.BW_1 })

		// A to I1
		assertNotNull(
			combinedNets
				.filter { it.accessOf(in1.model.getOutput()) != null }
				.mapNotNull { it.accessOf(a.model.getOutput()) as DigitalCombinedNetAccess? }
				.find { it.index == 1 && it.width == BitWidth.BW_1 })

		assertNotNull(
			combinedNets
				.filter { it.accessOf(a.model.getOutput()) != null }
				.mapNotNull { it.accessOf(in1.model.getOutput()) as DigitalCombinedNetAccess? }
				.find { it.index == 0 && it.width == BitWidth.BW_1 })
	}

	@Test
	fun shouldCreateWideCombinedNets() {
		val combinedNets = bidiSplitterView.model.createCombinedNetsFor(
			in1.model.getOutput<DigitalSignal>(),
			bidiSplitterView.model.getInput(3),
			scheduler)

		assertEquals(1, combinedNets.size)

		assertNotNull(
			combinedNets
				.filter { it.accessOf(in1.model.getOutput()) != null }
				.mapNotNull { it.accessOf(a.model.getOutput()) as DigitalCombinedNetAccess? }
				.find { it.index == 1 && it.width == BitWidth.BW_1 })

		assertNotNull(
			combinedNets
				.filter { it.accessOf(a.model.getOutput()) != null }
				.mapNotNull { it.accessOf(in1.model.getOutput()) as DigitalCombinedNetAccess? }
				.find { it.index == 0 && it.width == BitWidth.BW_1 })
	}
}