package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.addressable.LookupTable
import ch.scorpion.antares.model.gate.AndGate
import ch.scorpion.antares.model.gate.OrGate
import ch.scorpion.antares.model.inout.CircuitInOutImpl
import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.net.Concentrator
import ch.scorpion.antares.model.net.Constant
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.addressable.LookupTableView
import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.antares.view.gate.NotGateView
import ch.scorpion.antares.view.gate.OrGateView
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.net.ConcentratorView
import ch.scorpion.antares.view.net.ConstantView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.VerticeView

/**
 * Allows to build [GraphStorable] programmatically.
 */
class CircuitBuilder(
	graphStorable: GraphStorable,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val eventBus: EventBus = BaseModule.eventBus
) : GraphViewBuilder<DigitalSignal>(graphStorable) {

	fun addInput(
		name: String,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	): CircuitInOutView = addInOut(name, PortType.INPUT, location, orientation)

	fun addOutput(
		name: String,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	): CircuitInOutView = addInOut(name, PortType.OUTPUT, location, orientation)

	fun addNot(
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	): NotGateView = NotGateView(styleProvider).also {
			it.orientation = orientation
			addVerticeView(it, location)
		}

	fun addAnd(
		inputCount: InputCount,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	): AndGateView = AndGateView(styleProvider, andGate = AndGate(inputCount)).also {
			it.orientation = orientation
			addVerticeView(it, location)
		}

	fun addConstant(
		value: DigitalSignal,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	) : ConstantView = ConstantView(styleProvider, Constant(value)).also {
		it.orientation = orientation
		addVerticeView(it, location)
	}

	fun addOr(
		inputCount: InputCount,
		location: Point2D = Point2D.ZERO,
		orientation: Direction = Direction.EAST
	): OrGateView = OrGateView(styleProvider, orGate = OrGate(inputCount)).also {
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

	private fun addInOut(
		name: String? = null,
		portType: PortType,
		location: Point2D,
		orientation: Direction
	): CircuitInOutView =
		CircuitInOutView(styleProvider, CircuitInOutImpl(eventBus, name, portType), eventBus, orientation).also {
			addVerticeView(it, location)
		}

	private fun <T: Vertice> addVerticeView(vv: VerticeView<T>, location: Point2D) {
		vv.location = location
		graphView.add(vv)
	}
}