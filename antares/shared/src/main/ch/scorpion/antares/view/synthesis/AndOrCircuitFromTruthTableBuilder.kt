package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.gate.NonUnaryLogicGateType
import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.DnfToDigitalGateStructure
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.gate.LogicGateView
import ch.scorpion.antares.view.net.tunnel.TunnelFlowDirection
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

class AndOrCircuitFromTruthTableBuilder(
	truthTable: TruthTable,
	dnfs: List<DNF>,
	graphStorable: GraphStorable
) : AbstractCircuitFromTruthTableBuilder(truthTable, dnfs, graphStorable) {

	companion object {

		private val LOG by logger(AndOrCircuitFromTruthTableBuilder::class)

		private const val AND_Y = NOT_Y + Look.SCALE * 8
		private const val AND_DIST_X = Look.SCALE * 12
		private const val AND_GAP_Y = Look.SCALE * 4
		private const val EMPTY_AND_GATE_HEIGHT = Look.SCALE * 12
		private const val OR_WIRE_DIST = Look.SCALE * 2
		private const val OR_MIN_DIST_X = Look.SCALE * 12
	}

	private data class AndTermView(
		val andTerm: DnfToDigitalGateStructure.Companion.AndTerm,
		var yPos: Int = 0,
		var verticeView: VerticeView<*>? = null
	)

	private data class OrTermView(
		val column: Int,
		val outputName: String,
		val andTermViews: MutableList<AndTermView> = mutableListOf(),
		var yPos: Int = 0,
		var orView: LogicGateView? = null,
		var flipFlopView: SubGraphVerticeView<SubGraphVerticeRef>? = null
	)

	private var andY = AND_Y

	private var clkX = 0

	override fun build() {
		val orTerms = mutableListOf<OrTermView>()

		with (truthTable) {
			for (col in inputColumnCount until inputColumnCount + outputColumnCount) {
				orTerms.add(createOrTerm(col))
			}
		}

		x = 0
		buildInputs(addNotViews = true)

		// The stack of all AND gates determines how long the input wires have to be,
		// so build the AND gates prior to building the wires
		x += AND_DIST_X
		orTerms.forEach { buildAndGates(it) }

		buildInputWires(andY)

		orTerms.forEach { buildAndWires(it) }

		// Calculate the location of the OR stack such that there is enough space for
		// wiring AND outputs to OR inputs
		val maxOrInputCount = orTerms.maxOf { it.andTermViews.size }
		x += OR_MIN_DIST_X + maxOrInputCount / 2 * OR_WIRE_DIST

		orTerms.forEach {
			buildOrGate(it)
			buildOrInputWires(it)
		}

		clkX = x + OUTPUT_DIST_X / 2
		x += OUTPUT_DIST_X

		if (truthTable.stateColumnCount > 0) {

			orTerms
				.filter { truthTable.isStateColumn(it.column) }
				.forEach { buildFlipFlop(it) }

			x += FLIP_FLOP_DIST_X

			buildClkWires(
				buildClkAndWire(andY),
				orTerms
			)
		}

		orTerms.forEach { buildOutput(it) }
	}

	private fun createOrTerm(outputColumn: Int): OrTermView {
		val dnf = dnfs[outputColumn - truthTable.inputColumnCount]
		val andTerms = DnfToDigitalGateStructure(dnf).build()
		return OrTermView(outputColumn, truthTable.getColumnName(outputColumn), andTerms.map { AndTermView(it) }.toMutableList())
	}

	private fun buildAndGates(orTerm: OrTermView) {
		for (andTermView in orTerm.andTermViews) {
			if (andTermView.andTerm.factors.size == 1) {
				if (andTermView.andTerm.factors[0].constant == null) {
					LOG.trace("Omit AND gate for single-factor AND term")
					andTermView.yPos = andY
					andTermView.verticeView = null
					andY += EMPTY_AND_GATE_HEIGHT
				} else {
					val constantView = circuitBuilder.addConstant(DigitalSignalFactory.of(true), Point2D(x, andY))
					andTermView.yPos = andY
					andTermView.verticeView = constantView
					andY += constantView.bounds.heightInt + AND_GAP_Y
				}
			} else {
				if (andTermView.andTerm.factors.size > PortCount.entries.last().count) {
					throw CircuitFromTruthTableBuilderError(Translations.getString("antares.synthesis.maxAndInputCountExceeded.error"))
				}
				val andGateView = circuitBuilder.addAnd(PortCount.of(andTermView.andTerm.factors.size), Point2D(x, andY))
				andTermView.yPos = andY
				andTermView.verticeView = andGateView
				andY += andGateView.bounds.heightInt + AND_GAP_Y
			}
		}
	}

	private fun buildAndWires(orTerm: OrTermView) {
		orTerm.andTermViews.forEach { andTermView ->
			if (andTermView.verticeView is LogicGateView && (andTermView.verticeView as LogicGateView).model.gateType == NonUnaryLogicGateType.And) {
				// vv is either an AND gate (for multi-factor terms) or a ConstantView (for constant terms).
				// If vv is empty, andTerm is a single-factor term (no AND gate) that doesn't need wiring
				// in the "AND stack".
				andTermView.andTerm.factors.forEachIndexed { factorIndex, factor ->
					splitInputWire(andTermView.verticeView!!, factorIndex + 1, factor.inputIndex!!, factor.inverted!!)
				}
			}
		}
	}

	private fun buildOrGate(orTerm: OrTermView) {
		if (orTerm.andTermViews.size > PortCount.entries.last().count) {
			throw CircuitFromTruthTableBuilderError(Translations.getString("antares.synthesis.maxOrInputCountExceeded.error"))
		}

		orTerm.yPos = orTerm
			.andTermViews
			.map { it.yPos }
			.average()
			.toInt()

		if (orTerm.andTermViews.size == 1) {
			// Constant or direct input value, not OR gate necessary
			orTerm.orView = null
			return
		}

		orTerm.orView = circuitBuilder.addOr(PortCount.of(orTerm.andTermViews.size), Point2D(x, orTerm.yPos))
	}

	private fun buildOrInputWires(orTerm: OrTermView) {
		if (orTerm.orView != null) {
			val firstPort = orTerm.orView!!.getPortConnectionPoint(orTerm.orView!!.getPort(1))

			var upperX = firstPort.x - OR_WIRE_DIST
			for (i in 1..orTerm.andTermViews.size / 2) {
				buildOrInputWire(orTerm, i, upperX)
				upperX -= OR_WIRE_DIST
			}

			var lowerX = firstPort.x - OR_WIRE_DIST
			var portId = orTerm.andTermViews.size
			while (orTerm.orView!!.model.getPort<DigitalSignal>(portId).net == null) {
				buildOrInputWire(orTerm, portId, lowerX)
				lowerX -= OR_WIRE_DIST
				portId--
			}
		}
	}

	private fun buildOrInputWire(orTerm: OrTermView, portId: Int, wireX: Double) {
		LOG.trace("Build wire for OR input port $portId")
		val andTermView = orTerm.andTermViews[portId - 1]

		if (andTermView.verticeView != null) {
			// Connect OrGateView input with output of AndGateView or ConstantView
			andTermView.verticeView?.let { vv ->
				val destPort = orTerm.orView!!.model.getInput<DigitalSignal>(portId)
				val destPoint = orTerm.orView!!.getPortConnectionPoint(destPort)
				val origPoint = vv.getPortConnectionPoint(vv.model.getOutput<DigitalSignal>())
				val points = mutableListOf<Point2D>()

				points.add(origPoint)
				points.add(Point2D(wireX, origPoint.y))
				points.add(Point2D(wireX, destPoint.y))
				points.add(destPoint)

				if (!destPort.isConnected) {
					circuitBuilder.connect(vv, vv.model.getOutput(), orTerm.orView!!, destPort, points)
				}
			}
		} else {
			// Connect OrGateView input with input wire
			val factor = andTermView.andTerm.factors[0]
			splitInputWire(orTerm.orView!!, portId, factor.inputIndex!!, factor.inverted!!)
		}
	}

	private fun buildFlipFlop(orTerm: OrTermView) {
		val ff = circuitBuilder.addDFlipFlop(Point2D(x, orTerm.yPos))
			?: throw CircuitFromTruthTableBuilderError("antares.fsm.flipFlopNotFound.error")

		connectOutput(orTerm, ff, ff.model.getInput("D"))
		orTerm.flipFlopView = ff
	}

	private fun buildOutput(orTerm: OrTermView) {
		val outputView = if (truthTable.isStateColumn(orTerm.column)) {
			// Overwrite n+1 output name with name from input Tunnel
			val name = truthTable.getColumnName(orTerm.column - truthTable.inputColumnCount)
			circuitBuilder.addTunnel(name, Point2D(x, orTerm.yPos), Direction.EAST, TunnelFlowDirection.In)
		} else {
			circuitBuilder.addOutput(orTerm.outputName, Point2D(x, orTerm.yPos))
		}

		if (orTerm.flipFlopView != null) {
			circuitBuilder.connect(orTerm.flipFlopView!!, orTerm.flipFlopView!!.model.getOutput("Q"), outputView)
		} else {
			connectOutput(orTerm, outputView, outputView.model.getInput())
		}
	}

	private fun buildClkAndWire(endY: Int): EdgeView<DigitalSignal> {
		val clk = circuitBuilder.addInput(calculateClkName(), Point2D(clkX, INPUT_Y), Direction.SOUTH)
		return circuitBuilder.connectOutputOpen(clk, Point2D(clkX, endY))
	}

	private fun buildClkWires(clkWire: EdgeView<DigitalSignal>, orTerms: List<OrTermView>) {
		var edgeView = clkWire
		orTerms
			.filter { it.flipFlopView != null }
			.forEach {
				val clkPortView = it.flipFlopView!!.getPortView<DigitalSignal>(it.flipFlopView!!.model.getPort("C"))!!
				val p = Point2D(clkX.toDouble(), it.flipFlopView!!.location.y + clkPortView.connectionPoint.y)
				edgeView = circuitBuilder
					.split(edgeView, 0, p, clkPortView)
					.tailEdgeView
			}
	}

	private fun connectOutput(orTerm: OrTermView, outputView: VerticeView<*>, outputViewPort: InputPort<DigitalSignal>) {
		if (orTerm.orView != null) {
			// Wire from OR gate to GraphOutput
			circuitBuilder.connect(orTerm.orView!!, outputView, outputViewPort)
		} else if (orTerm.andTermViews[0].verticeView != null) {
			// Wire from AndGateView or Constant to GraphOutput
			circuitBuilder.connect(orTerm.andTermViews[0].verticeView!!, outputView, outputViewPort)
		} else {
			// Wire from input wire to GraphOutput
			val factor = orTerm.andTermViews[0].andTerm.factors[0]
			splitInputWire(outputView, outputViewPort.portId, factor.inputIndex!!, factor.inverted!!)
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