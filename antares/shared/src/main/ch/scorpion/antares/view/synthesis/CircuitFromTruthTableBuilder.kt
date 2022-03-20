package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.expression.BooleanExpression
import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.quinemccluskey.DnfToDigitalGateStructure
import ch.scorpion.antares.model.quinemccluskey.DnfToDigitalGateStructure.Companion.AndTerm
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.antares.view.gate.NotGateView
import ch.scorpion.antares.view.gate.OrGateView
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.VerticeView

class CircuitFromTruthTableBuilderError(msg: String) : Error(msg)

/**
 * Fills a [GraphStorable] with logic representing the DNF [BooleanExpression]s defined by a [TruthTable].
 */
class CircuitFromTruthTableBuilder(
	private val truthTable: TruthTable,
	private val dnfs: List<DNF>,
	graphStorable: GraphStorable
) {
	companion object {

		private val LOG by logger(CircuitFromTruthTableBuilder::class)

		private const val INPUT_Y = 0
		private const val INPUT_DIST_X = SCALE * 12
		private const val NOT_WIRE_Y = INPUT_Y + SCALE * 2
		private const val NOT_Y = INPUT_Y + SCALE * 14
		private const val AND_Y = NOT_Y + SCALE * 8
		private const val AND_DIST_X = SCALE * 12
		private const val AND_GAP_Y = SCALE * 4
		private const val EMPTY_AND_GATE_HEIGHT = SCALE * 12
		private const val OR_WIRE_DIST = SCALE * 2
		private const val OR_MIN_DIST_X = SCALE * 12
		private const val OUTPUT_DIST_X = SCALE * 4
	}

	private data class AndTermView(
		val andTerm: AndTerm,
		var yPos: Int = 0,
		var verticeView: VerticeView<*>? = null
	)

	private data class OrTermView(
		val outputName: String,
		val andTermViews: MutableList<AndTermView> = mutableListOf(),
		var yPos: Int = 0,
		var orView: OrGateView? = null
	)

	private val circuitBuilder = CircuitBuilder(graphStorable)

	private var x = 0
	private var andY = AND_Y

	private val inputViews = mutableListOf<CircuitInOutView>()
	private val inputEdgeViews = mutableListOf<EdgeView<DigitalSignal>>()
	private val notViews = mutableListOf<NotGateView>()
	private val notEdgeViews = mutableListOf<EdgeView<DigitalSignal>>()

	/**
	 * Fills [GraphStorable] with a circuit according to [truthTable].
	 * @throws CircuitFromTruthTableBuilderError if the required gate input counts exceed the system limit
	 */
	fun build() {
		val orTerms = mutableListOf<OrTermView>()

		with (truthTable) {
			for (col in inputColumnCount until inputColumnCount + outputColumnCount) {
				orTerms.add(createOrTerm(col))
			}
		}

		x = 0
		buildInputs()

		// The stack of all AND gates determines how long the input wires have to be,
		// so build the AND gates prior to building the wires
		x += AND_DIST_X
		orTerms.forEach { buildAndGates(it) }

		buildInputWires()

		orTerms.forEach { buildAndWires(it) }

		// Calculate the location of the OR stack such that there is enough space for
		// wiring AND outputs to OR inputs
		val maxOrInputCount = orTerms.maxOf { it.andTermViews.size }
		x += OR_MIN_DIST_X + maxOrInputCount / 2 * OR_WIRE_DIST

		orTerms.forEach {
			buildOrGate(it)
			buildOrInputWires(it)
		}

		x += OUTPUT_DIST_X

		orTerms.forEach { buildOutput(it) }
	}

	private fun createOrTerm(outputColumn: Int): OrTermView {
		val dnf = dnfs[outputColumn - truthTable.inputColumnCount]
		val andTerms = DnfToDigitalGateStructure(dnf).build()
		return OrTermView(truthTable.getColumnName(outputColumn), andTerms.map { AndTermView(it) }.toMutableList())
	}

	private fun buildInputs() {
		with (truthTable) {
			for (col in 0 until inputColumnCount) {
				inputViews.add(circuitBuilder.addInput(getColumnName(col), Point2D(x, INPUT_Y), Direction.SOUTH))
				notViews.add(circuitBuilder.addNot(Point2D(x + INPUT_DIST_X / 2, NOT_Y), Direction.SOUTH))
				x += INPUT_DIST_X
			}
		}
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
				if (andTermView.andTerm.factors.size > InputCount.values().last().count) {
					throw CircuitFromTruthTableBuilderError(Translations.getString("antares.synthesis.maxAndInputCountExceeded.error"))
				}
				val andGateView = circuitBuilder.addAnd(InputCount.of(andTermView.andTerm.factors.size), Point2D(x, andY))
				andTermView.yPos = andY
				andTermView.verticeView = andGateView
				andY += andGateView.bounds.heightInt + AND_GAP_Y
			}
		}
	}

	private fun buildInputWires() {
		inputViews.forEachIndexed { index, inputView ->
			val notView = notViews[index]

			val inputCP = inputView.getPortConnectionPoint(inputView.model.getOutput<DigitalSignal>())
			val ev = circuitBuilder.connectOutputOpen(inputView, Point2D(inputCP.xInt, andY))

			inputEdgeViews.add(circuitBuilder
				.split(ev, 0, Point2D(inputCP.xInt, NOT_WIRE_Y), notView)
				.tailEdgeView)

			val notCP = notView.getPortConnectionPoint(notView.model.getOutput<DigitalSignal>())
			notEdgeViews.add(circuitBuilder.connectOutputOpen(notView, Point2D(notCP.xInt, andY)))
		}
	}

	private fun buildAndWires(orTerm: OrTermView) {
		orTerm.andTermViews.forEachIndexed { index, andTermView ->
			if (andTermView.verticeView is AndGateView) {
				// vv is either and AndGateView (for multi-factor terms) or a ConstantView (for constant terms).
				// If vv is empty, andTerm is a single-factor term (no AND gate) that doesn't need wiring
				// in the "AND stack".
				andTermView.andTerm.factors.forEachIndexed { factorIndex, factor ->
					splitInputWire(factor, andTermView.verticeView!!, factorIndex + 1)
				}
			}
		}
	}

	private fun splitInputWire(
		factor: DnfToDigitalGateStructure.Companion.Factor,
		destVerticeView: VerticeView<*>,
		destPortId: Int
	) {
		val ev = if (factor.inverted!!) {
			notEdgeViews[factor.inputIndex!!]
		} else {
			inputEdgeViews[factor.inputIndex!!]
		}
		val destPort = destVerticeView.model.getInput<DigitalSignal>(destPortId)
		val destPortView = destVerticeView.getPortView(destPort)
		val splitX = ev.polyline.getFirstPoint().x
		val splitY = destVerticeView.getPortConnectionPoint(destPort).y

		val tailEv = circuitBuilder
			.split(ev, 0, Point2D(splitX, splitY), destPortView)
			.tailEdgeView

		// Replace vertical EdgeView with tail of current split result
		if (factor.inverted) {
			notEdgeViews[factor.inputIndex] = tailEv
		} else {
			inputEdgeViews[factor.inputIndex] = tailEv
		}
	}

	private fun buildOrGate(orTerm: OrTermView) {
		if (orTerm.andTermViews.size > InputCount.values().last().count) {
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

		orTerm.orView = circuitBuilder.addOr(InputCount.of(orTerm.andTermViews.size), Point2D(x, orTerm.yPos))
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
			for (i in orTerm.andTermViews.size downTo orTerm.andTermViews.size / 2) {
				buildOrInputWire(orTerm, i, lowerX)
				lowerX -= OR_WIRE_DIST
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
			splitInputWire(factor, orTerm.orView!!, portId)
		}
	}

	private fun buildOutput(orTerm: OrTermView) {
		val outputView = circuitBuilder.addOutput(orTerm.outputName, Point2D(x, orTerm.yPos))

		if (orTerm.orView != null) {
			// Wire from OR gate to GraphOutput
			circuitBuilder.connect(orTerm.orView!!, outputView)
		} else if (orTerm.andTermViews[0].verticeView != null) {
			// Wire from AndGateView or Constant to GraphOutput
			circuitBuilder.connect(orTerm.andTermViews[0].verticeView!!, outputView)
		} else {
			// Wire from input wire to GraphOutput
			splitInputWire(orTerm.andTermViews[0].andTerm.factors[0], outputView, 1)
		}
	}
}