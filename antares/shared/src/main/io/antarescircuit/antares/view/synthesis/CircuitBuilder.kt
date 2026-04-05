package io.antarescircuit.antares.view.synthesis

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.addressable.LookupTable
import io.antarescircuit.antares.model.gate.NonUnaryLogicGate
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType
import io.antarescircuit.antares.model.gate.UnaryLogicGate
import io.antarescircuit.antares.model.inout.DigitalCircuitInOutImpl
import io.antarescircuit.antares.model.net.BranchCount
import io.antarescircuit.antares.model.net.Concentrator
import io.antarescircuit.antares.model.net.Constant
import io.antarescircuit.antares.model.net.Tunnel
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.antares.view.addressable.LookupTableView
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.net.ConcentratorView
import io.antarescircuit.antares.view.net.ConstantView
import io.antarescircuit.antares.view.net.tunnel.TunnelFlowDirection
import io.antarescircuit.antares.view.net.tunnel.TunnelView
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.GraphStorable
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.model.PortType
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Allows to build [GraphStorable] programmatically.
 */
class CircuitBuilder(
	graphStorable: GraphStorable,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val eventBus: EventBus = BaseModule.eventBus
) : GraphViewBuilder<DigitalSignal>(graphStorable) {

	companion object {
		private val D_FLIP_FLOP_UUID = UUID("d6ab312a-a94b-403a-9291-642295ff29c5")
	}

	fun addInput(
		name: String,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	): DigitalCircuitInOutView = addInOut(name, PortType.INPUT, location, orientation)

	fun addOutput(
		name: String,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	): DigitalCircuitInOutView = addInOut(name, PortType.OUTPUT, location, orientation)

	fun addNot(
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	): LogicGateView = LogicGateView(styleProvider, gate = UnaryLogicGate.notGate()).also {
			it.orientation = orientation
			addVerticeView(it, location)
		}

	fun addAnd(
		inputCount: PortCount,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	): LogicGateView = LogicGateView(styleProvider, gate = NonUnaryLogicGate(NonUnaryLogicGateType.And, inputCount)).also {
			it.orientation = orientation
			addVerticeView(it, location)
		}

	fun addConstant(
		value: DigitalSignal,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	) : ConstantView = ConstantView(styleProvider, Constant(LongValueImpl(value.getValue().toLong()))).also {
		it.orientation = orientation
		addVerticeView(it, location)
	}

	fun addOr(
		inputCount: PortCount,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	): LogicGateView = LogicGateView(styleProvider, gate = NonUnaryLogicGate(NonUnaryLogicGateType.Or, inputCount)).also {
		it.orientation = orientation
		addVerticeView(it, location)
	}

	fun addConcentrator(
		bitWidth: BitWidth,
		branchCount: BranchCount,
		handedness: Handedness = Handedness.RIGHT,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	) : ConcentratorView {
		return ConcentratorView(styleProvider, Concentrator(bitWidth, branchCount), handedness).also {
			it.orientation = orientation
			addVerticeView(it, location)
		}
	}

	fun addLookupTable(
		addressBitWidth: BitWidth,
		dataBitWidth: BitWidth,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	) : LookupTableView = LookupTableView(styleProvider, LookupTable(addressBitWidth, dataBitWidth)).also {
		it.orientation = orientation
		addVerticeView(it, location)
	}

	fun addTunnel(
		name: String,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST,
		flowDirection: TunnelFlowDirection = TunnelFlowDirection.Undefined
	): TunnelView = TunnelView(styleProvider, Tunnel(name)).also {
		it.orientation = orientation
		it.flowDirection = flowDirection
		addVerticeView(it, location)
	}

	fun addDFlipFlop(
		location: Point2D
	): SubGraphVerticeView<SubGraphVerticeRef>? {
		val libElem = LibraryModule.libraryHolder.library.getContainerLibraryElement(D_FLIP_FLOP_UUID) ?: return null
		return (libElem.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<SubGraphVerticeRef>).also {
			addVerticeView(it, location)
		}
	}

	private fun addInOut(
		name: String? = null,
		portType: PortType,
		location: Point2D,
		orientation: Direction
	): DigitalCircuitInOutView =
		DigitalCircuitInOutView(styleProvider, DigitalCircuitInOutImpl(eventBus, name, portType), eventBus, orientation).also {
			addVerticeView(it, location)
		}

	private fun <T: Vertice> addVerticeView(vv: VerticeView<T>, location: Point2D) {
		vv.location = location
		graphView.add(vv)
	}
}