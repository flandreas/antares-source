package io.antarescircuit.antares.view.synthesis

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.net.tunnel.TunnelFlowDirection
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.graph.GraphStorable
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.VerticeView
import kotlin.math.abs

class CircuitFromTruthTableBuilderError(msg: String) : Error(msg)

/** Fills a [GraphStorable] with a circuit for a [TruthTable], using two-input gates only if requested. */
typealias CircuitFromTruthTableBuilder = (
	truthTable: TruthTable,
	twoInputGatesOnly: Boolean,
	graphStorable: GraphStorable,
) -> Unit

/**
 * Fills a [GraphStorable] with logic representing the output columns of a [TruthTable].
 */
abstract class AbstractCircuitFromTruthTableBuilder(
	protected val truthTable: TruthTable,
	graphStorable: GraphStorable
) {

	companion object {
		const val INPUT_Y = 0
		const val INPUT_DIST_X = Look.SCALE * 12
		const val NOT_WIRE_Y = INPUT_Y + Look.SCALE * 2
		const val NOT_Y = INPUT_Y + Look.SCALE * 14
		const val FLIP_FLOP_DIST_X = Look.SCALE * 14
		const val OUTPUT_DIST_X = Look.SCALE * 4
	}

	protected val circuitBuilder = CircuitBuilder(graphStorable)

	private val inputViews = mutableListOf<VerticeView<*>>()
	private val inputEdgeViews = mutableListOf<EdgeView<DigitalSignal>>()

	private val notViews = mutableListOf<LogicGateView>()
	private val notEdgeViews = mutableListOf<EdgeView<DigitalSignal>>()

	protected var x = 0

	/**
	 * Fills [GraphStorable] with a circuit according to [truthTable].
	 * @throws CircuitFromTruthTableBuilderError if the required gate input counts exceed the system limit
	 */
	abstract fun build()

	/**
	 * Builds the inputs and, if [addNotViews], an inverter per input: a NOT gate, or a
	 * [inverterType] gate with tied inputs.
	 */
	protected fun buildInputs(addNotViews: Boolean, inverterType: NonUnaryLogicGateType? = null) {
		with (truthTable) {
			for (col in 0 until inputColumnCount) {
				if (truthTable.isStateColumn(col)) {
					inputViews.add(circuitBuilder.addTunnel(getColumnName(col), Point2D(x, INPUT_Y), Direction.NORTH, TunnelFlowDirection.Out))
				} else {
					inputViews.add(circuitBuilder.addInput(getColumnName(col), Point2D(x, INPUT_Y), Direction.SOUTH))
				}
				if (addNotViews) {
					val location = Point2D(x + INPUT_DIST_X / 2, NOT_Y)
					notViews.add(
						if (inverterType == null) {
							circuitBuilder.addNot(location, Direction.SOUTH)
						} else {
							circuitBuilder.addGate(inverterType, PortCount.TWO, location, Direction.SOUTH)
						}
					)
				}
				x += INPUT_DIST_X
			}
		}
	}

	protected fun buildInputWires(endY: Int) {
		inputViews.forEachIndexed { index, inputView ->
			val inputCP = inputView.getPortConnectionPoint(inputView.model.getOutput<DigitalSignal>())
			val ev = circuitBuilder.connectOutputOpen(inputView, Point2D(inputCP.xInt, endY))

			if (notViews.isEmpty()) {
				inputEdgeViews.add(ev)
			} else {
				val notView = notViews[index]
				// Tap the input port farther away first, so that the wires of tied inputs don't cross
				var tailEv = ev
				notView.model.getInputs()
					.map { notView.model.getInput<DigitalSignal>(it.portId) }
					.sortedByDescending { abs(notView.getPortConnectionPoint(it).x - inputCP.x) }
					.forEachIndexed { i, port ->
						tailEv = circuitBuilder
							.split(tailEv, 0, Point2D(inputCP.xInt, NOT_WIRE_Y + i * Look.SCALE), notView.getPortView(port))
							.tailEdgeView
					}
				inputEdgeViews.add(tailEv)

				val notCP = notView.getPortConnectionPoint(notView.model.getOutput<DigitalSignal>())
				notEdgeViews.add(circuitBuilder.connectOutputOpen(notView, Point2D(notCP.xInt, endY)))
			}
		}
	}

	protected fun splitInputWire(
		destVerticeView: VerticeView<*>,
		destPortId: Int,
		inputIndex: Int,
		inverted: Boolean
	) {
		val hasNotViews: Boolean = notEdgeViews.isNotEmpty()

		val ev = if (hasNotViews && inverted) {
			notEdgeViews[inputIndex]
		} else {
			inputEdgeViews[inputIndex]
		}
		val destPort = destVerticeView.model.getInput<DigitalSignal>(destPortId)
		val destPortView = destVerticeView.getPortView(destPort)
		val splitX = ev.polyline.getFirstPoint().x
		val splitY = destVerticeView.getPortConnectionPoint(destPort).y

		val tailEv = circuitBuilder
			.split(ev, 0, Point2D(splitX, splitY), destPortView)
			.tailEdgeView

		// Replace vertical EdgeView with tail of current split result
		if (hasNotViews && inverted) {
			notEdgeViews[inputIndex] = tailEv
		} else {
			inputEdgeViews[inputIndex] = tailEv
		}
	}
}