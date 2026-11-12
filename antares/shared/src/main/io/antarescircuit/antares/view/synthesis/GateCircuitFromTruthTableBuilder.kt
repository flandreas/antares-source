package io.antarescircuit.antares.view.synthesis

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.net.tunnel.TunnelFlowDirection
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.graph.GraphStorable
import io.antarescircuit.jabbah.graph.model.InputPort
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.math.max

/**
 * Builds a gate circuit from the [GateNetworkFactory] networks of a [TruthTable].
 *
 * Gates are arranged in columns by their distance from the inputs. Gates fed by inputs only are
 * stacked below each other in the first column, every other gate is placed at the average height
 * of its inputs. Inputs are inverted by NOT gates, or by tied gates of the universal gate type.
 */
class GateCircuitFromTruthTableBuilder(
	truthTable: TruthTable,
	private val networkFactory: GateNetworkFactory,
	graphStorable: GraphStorable,
) : AbstractCircuitFromTruthTableBuilder(truthTable, graphStorable) {

	companion object {
		private const val FIRST_COLUMN_Y = NOT_Y + Look.SCALE * 8
		private const val FIRST_COLUMN_DIST_X = Look.SCALE * 12
		private const val GATE_GAP_Y = Look.SCALE * 4
		private const val EMPTY_SLOT_HEIGHT = Look.SCALE * 12
		private const val WIRE_DIST = Look.SCALE * 2
		private const val COLUMN_MIN_DIST_X = Look.SCALE * 12
	}

	private class OutputTerm(
		val column: Int,
		val outputName: String,
		val root: GateSignal,
	) {
		var yPos = 0
		/** The gate or constant driving the output, `null` if driven by an input wire. */
		var rootView: VerticeView<*>? = null
		var flipFlopView: SubGraphVerticeView<SubGraphVerticeRef>? = null
	}

	private val gateViews = LinkedHashMap<GateNode, LogicGateView>()

	/** x location of each gate column; index 0 is unused (inputs) */
	private val columnX = mutableListOf<Int>()

	private var stackY = FIRST_COLUMN_Y

	private var clkX = 0

	override fun build() {
		val roots = networkFactory.create(truthTable)
		if (roots.isEmpty()) {
			throw CircuitFromTruthTableBuilderError(Translations.getString("antares.synthesis.noOrTerms.error"))
		}
		val outputTerms = roots.mapIndexed { index, root ->
			val column = truthTable.inputColumnCount + index
			OutputTerm(column, truthTable.getColumnName(column), root)
		}

		x = 0
		buildInputs(addNotViews = true, inverterType = networkFactory.universalGate)

		// The stack of gates determines how long the input wires have to be,
		// so build the gates prior to building the wires
		x += FIRST_COLUMN_DIST_X
		calculateColumnX(roots)
		outputTerms.forEach { placeRoot(it) }

		buildInputWires(stackY)
		gateViews.forEach { (gate, view) -> buildGateWires(gate, view) }

		x = columnX.last()
		clkX = x + OUTPUT_DIST_X / 2
		x += OUTPUT_DIST_X

		if (truthTable.stateColumnCount > 0) {
			outputTerms
				.filter { truthTable.isStateColumn(it.column) }
				.forEach { buildFlipFlop(it) }

			x += FLIP_FLOP_DIST_X

			buildClkWires(buildClkAndWire(stackY), outputTerms)
		}

		outputTerms.forEach { buildOutput(it) }
	}

	/** Returns 0 for inputs and constants, otherwise 1 + the max. column of the gate's inputs. */
	private fun columnOf(signal: GateSignal): Int = when (signal) {
		is GateNode -> 1 + signal.inputs.maxOf { columnOf(it) }
		else -> 0
	}

	private fun calculateColumnX(roots: List<GateSignal>) {
		val maxInputCounts = mutableMapOf<Int, Int>()
		fun visit(signal: GateSignal) {
			if (signal is GateNode) {
				val column = columnOf(signal)
				maxInputCounts[column] = max(maxInputCounts[column] ?: 0, signal.inputs.size)
				signal.inputs.forEach { visit(it) }
			}
		}
		roots.forEach { visit(it) }

		// The first column is always present, as it holds constants and leaves room for input wires
		val columnCount = max(1, maxInputCounts.keys.maxOrNull() ?: 0)
		columnX.add(0)
		columnX.add(x)
		for (column in 2..columnCount) {
			// Leave enough space for wiring the outputs of the previous columns to the inputs
			columnX.add(columnX.last() + COLUMN_MIN_DIST_X + (maxInputCounts[column] ?: 0) / 2 * WIRE_DIST)
		}
	}

	/** Reserves [height] at the bottom of the stack and returns its y location. */
	private fun reserve(height: Int): Int = stackY.also { stackY += height }

	private fun placeRoot(term: OutputTerm) {
		when (val root = term.root) {
			is ConstantSignal -> {
				val constantView = circuitBuilder.addConstant(DigitalSignalFactory.of(root.value), Point2D(columnX[1], stackY))
				term.yPos = reserve(constantView.bounds.heightInt + GATE_GAP_Y)
				term.rootView = constantView
			}
			is LiteralSignal -> term.yPos = reserve(EMPTY_SLOT_HEIGHT)
			is GateNode -> {
				term.yPos = placeGate(root).toInt()
				term.rootView = gateViews[root]
			}
		}
	}

	/** Places [signal] and the gates feeding it, and returns its y location. */
	private fun place(signal: GateSignal): Double = when (signal) {
		// Leave room for the wire of an input that skips the first column
		is LiteralSignal, is ConstantSignal -> reserve(EMPTY_SLOT_HEIGHT).toDouble()
		is GateNode -> placeGate(signal)
	}

	private fun placeGate(gate: GateNode): Double {
		val column = columnOf(gate)
		val view = circuitBuilder.addGate(gate.type, PortCount.of(max(2, gate.inputs.size)), Point2D(columnX[column], 0))
		gateViews[gate] = view

		val y = when {
			gate.inputs.all { it is LiteralSignal } -> reserve(view.bounds.heightInt + GATE_GAP_Y).toDouble()
			gate.inputs.size == 1 -> {
				// Inverter: align its first input with the output of the gate feeding it
				val source = gate.inputs.first() as GateNode
				place(source)
				val sourceView = gateViews.getValue(source)
				val sourceY = sourceView.getPortConnectionPoint(sourceView.model.getOutput<DigitalSignal>()).y
				sourceY - view.getPortConnectionPoint(view.model.getInput<DigitalSignal>(1)).y
			}
			else -> gate.inputs.map { place(it) }.average()
		}
		view.location = Point2D(columnX[column].toDouble(), y)
		return y
	}

	private fun buildGateWires(gate: GateNode, view: LogicGateView) {
		if (gate.inputs.size == 1) {
			buildTiedInputWires(gate.inputs.first() as GateNode, view)
			return
		}

		// Wires of the upper half are nested with the ones of the lower half so that they don't cross
		val inputCount = gate.inputs.size
		val firstPortX = view.getPortConnectionPoint(view.model.getInput<DigitalSignal>(1)).x
		val upperPortIds = 1..inputCount / 2
		val lowerPortIds = inputCount downTo inputCount / 2 + 1
		listOf(upperPortIds, lowerPortIds).forEach { portIds ->
			portIds.forEachIndexed { i, portId ->
				buildGateInputWire(gate.inputs[portId - 1], view, portId, firstPortX - (i + 1) * WIRE_DIST)
			}
		}
	}

	private fun buildGateInputWire(source: GateSignal, dest: LogicGateView, portId: Int, wireX: Double) {
		when (source) {
			is LiteralSignal -> splitInputWire(dest, portId, source.inputIndex, source.inverted)
			is ConstantSignal -> error("Constants are only supported as outputs")
			is GateNode -> {
				val sourceView = gateViews.getValue(source)
				val destPort = dest.model.getInput<DigitalSignal>(portId)
				val destPoint = dest.getPortConnectionPoint(destPort)
				val origPoint = sourceView.getPortConnectionPoint(sourceView.model.getOutput<DigitalSignal>())
				circuitBuilder.connect(
					sourceView,
					sourceView.model.getOutput(),
					dest,
					destPort,
					listOf(origPoint, Point2D(wireX, origPoint.y), Point2D(wireX, destPoint.y), destPoint),
				)
			}
		}
	}

	/** Connects both inputs of an inverter to [source], which is aligned with the first input. */
	private fun buildTiedInputWires(source: GateNode, inverter: LogicGateView) {
		val sourceView = gateViews.getValue(source)
		val firstPort = inverter.model.getInput<DigitalSignal>(1)
		val firstPoint = inverter.getPortConnectionPoint(firstPort)
		val edgeView = circuitBuilder.connect(
			sourceView,
			sourceView.model.getOutput(),
			inverter,
			firstPort,
			listOf(sourceView.getPortConnectionPoint(sourceView.model.getOutput<DigitalSignal>()), firstPoint),
		)
		circuitBuilder.split(
			edgeView,
			0,
			Point2D(firstPoint.x - WIRE_DIST, firstPoint.y),
			inverter.getPortView(inverter.model.getInput<DigitalSignal>(2)),
		)
	}

	private fun buildFlipFlop(term: OutputTerm) {
		val ff = circuitBuilder.addDFlipFlop(Point2D(x, term.yPos))
			?: throw CircuitFromTruthTableBuilderError("antares.fsm.flipFlopNotFound.error")

		connectOutput(term, ff, ff.model.getInput("D"))
		term.flipFlopView = ff
	}

	private fun buildOutput(term: OutputTerm) {
		val outputView = if (truthTable.isStateColumn(term.column)) {
			// Overwrite n+1 output name with name from input Tunnel
			val name = truthTable.getColumnName(term.column - truthTable.inputColumnCount)
			circuitBuilder.addTunnel(name, Point2D(x, term.yPos), Direction.EAST, TunnelFlowDirection.In)
		} else {
			circuitBuilder.addOutput(term.outputName, Point2D(x, term.yPos))
		}

		val flipFlopView = term.flipFlopView
		if (flipFlopView != null) {
			circuitBuilder.connect(flipFlopView, flipFlopView.model.getOutput("Q"), outputView)
		} else {
			connectOutput(term, outputView, outputView.model.getInput())
		}
	}

	private fun buildClkAndWire(endY: Int): EdgeView<DigitalSignal> {
		val clk = circuitBuilder.addInput(calculateClkName(), Point2D(clkX, INPUT_Y), Direction.SOUTH)
		return circuitBuilder.connectOutputOpen(clk, Point2D(clkX, endY))
	}

	private fun buildClkWires(clkWire: EdgeView<DigitalSignal>, terms: List<OutputTerm>) {
		var edgeView = clkWire
		terms.forEach { term ->
			val flipFlopView = term.flipFlopView ?: return@forEach
			val clkPortView = flipFlopView.getPortView<DigitalSignal>(flipFlopView.model.getPort("C"))!!
			val p = Point2D(clkX.toDouble(), flipFlopView.location.y + clkPortView.connectionPoint.y)
			edgeView = circuitBuilder
				.split(edgeView, 0, p, clkPortView)
				.tailEdgeView
		}
	}

	private fun connectOutput(term: OutputTerm, outputView: VerticeView<*>, outputViewPort: InputPort<DigitalSignal>) {
		val rootView = term.rootView
		if (rootView != null) {
			circuitBuilder.connect(rootView, outputView, outputViewPort)
		} else {
			// Wire from input wire to output
			val literal = term.root as LiteralSignal
			splitInputWire(outputView, outputViewPort.portId, literal.inputIndex, literal.inverted)
		}
	}

	private fun calculateClkName(): String {
		if (!truthTable.hasName("CLK")) {
			return "CLK"
		}
		if (!truthTable.hasName("C")) {
			return "C"
		}
		var name = "C"
		var i = 0
		while (truthTable.hasName(name)) {
			name = "$name${i++}"
		}
		return name
	}
}
