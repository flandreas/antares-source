package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.AbstractCircuitTest
import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.net.WireTap
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WireTapViewIntegrationTest : AbstractCircuitTest() {

	private lateinit var circuitView: GraphView
	private lateinit var builder: GraphViewBuilder<DigitalSignal>
	private lateinit var inoutView4A: DigitalCircuitInOutView
	private lateinit var inoutView4B: DigitalCircuitInOutView
	private lateinit var inoutView1A: DigitalCircuitInOutView
	private lateinit var inoutView1B: DigitalCircuitInOutView
	private lateinit var wireTapView: WireTapView
	private lateinit var edgeView: EdgeView<DigitalSignal>

	override fun getCircuitView(): GraphView = circuitView

	@BeforeTest
	fun setupCircuit() {
		builder = GraphViewBuilder("test")

		inoutView4A = builder.addVerticeView(DigitalCircuitInOutView(orientation = Direction.WEST, model = DigitalCircuitInOutImpl(portType = PortType.INOUT, bitWidth = BitWidth.BW_4)))
		inoutView4A.location = Point2D(0, 0)
		inoutView4A.signalRepresentation = DigitalSignalRepresentation.BINARY
		inoutView4B = builder.addVerticeView(DigitalCircuitInOutView(orientation = Direction.EAST, model = DigitalCircuitInOutImpl(portType = PortType.INOUT, bitWidth = BitWidth.BW_4)))
		inoutView4B.location = Point2D(200, 0)
		inoutView4B.signalRepresentation = DigitalSignalRepresentation.BINARY

		wireTapView = builder.addVerticeView(WireTapView(model = WireTap(narrowBitWidth = BitWidth.BW_1, bitWidth = BitWidth.BW_4, narrowPortCount = PortCount.TWO)))
		wireTapView.model.setTapPosition(1, 1)
		wireTapView.location = Point2D(200, 200)
		edgeView = builder.connect(inoutView4A, inoutView4B)
		builder.split(edgeView, 0, Point2D(100, 0), wireTapView)

		inoutView1A = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INOUT, bitWidth = BitWidth.BW_1)))
		builder.connect(wireTapView, wireTapView.model.getOutput(2), inoutView1A)

		inoutView1B = builder.addVerticeView(DigitalCircuitInOutView(model = DigitalCircuitInOutImpl(portType = PortType.INOUT, bitWidth = BitWidth.BW_1)))
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