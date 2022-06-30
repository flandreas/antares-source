package ch.scorpion.antares.view.net

import ch.scorpion.antares.AbstractCircuitTest
import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.net.WireTap
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WireTapViewIntegrationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var inoutView4A: CircuitInOutView
	private lateinit var inoutView4B: CircuitInOutView
	private lateinit var inoutView1A: CircuitInOutView
	private lateinit var inoutView1B: CircuitInOutView
	private lateinit var wireTapView: WireTapView
	private lateinit var edgeView: EdgeView<DigitalSignal>

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")

		inoutView4A = builder.addVerticeView(CircuitInOutView(orientation = Direction.WEST, model = CircuitInOutImpl(portType = PortType.INOUT, bitWidth = BitWidth.BW_4)))
		inoutView4A.location = Point2D(0, 0)
		inoutView4B = builder.addVerticeView(CircuitInOutView(orientation = Direction.EAST, model = CircuitInOutImpl(portType = PortType.INOUT, bitWidth = BitWidth.BW_4)))
		inoutView4B.location = Point2D(200, 0)

		wireTapView = builder.addVerticeView(WireTapView(model = WireTap(outputBitWidth = BitWidth.BW_1, bitWidth = BitWidth.BW_4, outputCount = PortCount.TWO)))
		wireTapView.model.setTapPositions(listOf(0, 1))
		wireTapView.location = Point2D(200, 200)
		edgeView = builder.connect(inoutView4A, inoutView4B)
		builder.split(edgeView, 0, Point2D(100, 0), wireTapView)

		inoutView1A = builder.addVerticeView(CircuitInOutView(model = CircuitInOutImpl(portType = PortType.INOUT, bitWidth = BitWidth.BW_1)))
		builder.connect(wireTapView, wireTapView.model.getOutput(2), inoutView1A)

		inoutView1B = builder.addVerticeView(CircuitInOutView(model = CircuitInOutImpl(portType = PortType.INOUT, bitWidth = BitWidth.BW_1)))
		builder.connect(wireTapView, wireTapView.model.getOutput(3), inoutView1B)

		circuitView = builder.build()
	}

	@Test
	fun shouldStartupAllUndefined() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_4, Bit.Undefined), edgeView.model.signal)

		(0..3).forEach {
			assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_1, Bit.Undefined), inoutView4A.getDigitSignal(it))
		}
		(0..3).forEach {
			assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_1, Bit.Undefined), inoutView4B.getDigitSignal(it))
		}
		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_1, Bit.Undefined), inoutView1A.getDigitSignal(0))
		assertEquals(DigitalSignalFactory.allOf(BitWidth.BW_1, Bit.Undefined), inoutView1B.getDigitSignal(0))
	}

	@Test
	fun shouldPropagateBit0() {
		startSimulation()
		proceedUntilQueueIsEmpty()

		inoutView4A.model.setIncomingSignal(DigitalSignalFactory.of(BitWidth.BW_4, 1), scheduler)
		proceedUntilQueueIsEmpty()

		assertEquals(DigitalSignalFactory.of(BitWidth.BW_4, 1), inoutView4B.model.signal)
		assertEquals(DigitalSignalFactory.of(BitWidth.BW_1, 1), inoutView1A.model.signal)
		assertEquals(DigitalSignalFactory.of(BitWidth.BW_1, 0), inoutView1B.model.signal)
	}
}