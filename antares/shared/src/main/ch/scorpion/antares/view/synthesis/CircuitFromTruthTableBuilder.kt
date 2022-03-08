package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.expression.BooleanExpression
import ch.scorpion.antares.model.quinemccluskey.DnfToDigitalGateStructure
import ch.scorpion.antares.model.quinemccluskey.DnfToDigitalGateStructure.Companion.AndTerm
import ch.scorpion.antares.model.quinemccluskey.minimizeToDNF
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.view.Look.SCALE
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
		private const val OR_WIRE_DIST = SCALE * 2
		private const val OR_MIN_DIST_X = SCALE * 12
		private const val OUTPUT_DIST_X = SCALE * 4
	}

	private data class OrTerm(
		val outputName: String,
		val andTerms: List<AndTerm>,
		val andOrConstantViews: MutableList<VerticeView<*>> = mutableListOf(),
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
		val orTerms = mutableListOf<OrTerm>()

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

		orTerms.forEachIndexed { index, orTerm ->
			buildAndWires(orTerm)
		}

		// Calculate the location of the OR stack such that there is enough space for
		// wiring AND outputs to OR inputs
		val maxOrInputCount = orTerms.maxOf { it.andTerms.size }
		x += OR_MIN_DIST_X + maxOrInputCount / 2 * OR_WIRE_DIST

		orTerms.forEach {
			buildOrGate(it)
			buildOrWires(it)
		}

		x += OUTPUT_DIST_X

		orTerms.forEach { buildOutput(it) }
	}

	private fun createOrTerm(outputColumn: Int): OrTerm {
		with (truthTable) {
			val andTerms = DnfToDigitalGateStructure(
				minimizeToDNF(getMinTerms(outputColumn), getDontCares(outputColumn), inputColumnCount)
			).build()
			return OrTerm(getColumnName(outputColumn), andTerms)
		}
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

	private fun buildAndGates(orTerm: OrTerm) {
		for (andTerm in orTerm.andTerms) {
			if (andTerm.factors.size == 1 && andTerm.factors[0].constant != null) {
				val constantView = circuitBuilder.addConstant(DigitalSignalFactory.of(true), Point2D(x, andY))
				LOG.trace("Adding Constant ${orTerm.andOrConstantViews.size}")
				orTerm.andOrConstantViews.add(constantView)
				andY += constantView.bounds.heightInt + AND_GAP_Y
			} else {
				if (andTerm.factors.size > InputCount.values().last().count) {
					throw CircuitFromTruthTableBuilderError(Translations.getString("antares.synthesis.maxAndInputCountExceeded.error"))
				}
				val andGateView = circuitBuilder.addAnd(InputCount.of(andTerm.factors.size), Point2D(x, andY))
				LOG.trace("Adding AND gate ${orTerm.andOrConstantViews.size}")
				orTerm.andOrConstantViews.add(andGateView)
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

	private fun buildAndWires(orTerm: OrTerm) {
		orTerm.andTerms.forEachIndexed { index, andTerm ->
			if (andTerm.factors.size > 1 || andTerm.factors[0].inputIndex != null) {
				val vv = orTerm.andOrConstantViews[index]
				andTerm.factors.forEachIndexed { factorIndex, factor ->


					val ev = if (factor.inverted!!) {
						notEdgeViews[factor.inputIndex!!]
					} else {
						inputEdgeViews[factor.inputIndex!!]
					}
					val destPort = vv.model.getInput<DigitalSignal>(factorIndex + 1)
					LOG.trace("Wiring Port ${destPort.portId} of AND ${index}: factorIndex=$factorIndex")

					val destPortView = vv.getPortView(destPort)
					val splitX = ev.polyline.getFirstPoint().x
					val splitY = vv.getPortConnectionPoint(destPort).y

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
			}
		}
	}

	private fun buildOrGate(orTerm: OrTerm) {
		if (orTerm.andTerms.size == 1) {
			// Constant, not OR gate necessary
			return
		}

		if (orTerm.andTerms.size > InputCount.values().last().count) {
			throw CircuitFromTruthTableBuilderError(Translations.getString("antares.synthesis.maxOrInputCountExceeded.error"))
		}

		val orY = orTerm
			.andOrConstantViews
			.map { it.getPortConnectionPoint(it.model.getOutput<DigitalSignal>()).yInt }
			.average()
			.toInt()

		orTerm.orView = circuitBuilder.addOr(InputCount.of(orTerm.andTerms.size), Point2D(x, orY))
	}

	private fun buildOrWires(orTerm: OrTerm) {
		if (orTerm.orView != null) {
			val firstPort = orTerm.orView!!.getPortConnectionPoint(orTerm.orView!!.getPort(1))

			var upperX = firstPort.x - OR_WIRE_DIST
			for (i in 1..orTerm.andTerms.size / 2) {
				buildOrWire(orTerm, i, upperX)
				upperX -= OR_WIRE_DIST
			}

			var lowerX = firstPort.x - OR_WIRE_DIST
			for (i in orTerm.andTerms.size downTo orTerm.andTerms.size / 2) {
				buildOrWire(orTerm, i, lowerX)
				lowerX -= OR_WIRE_DIST
			}
		}
	}

	private fun buildOrWire(orTerm: OrTerm, portId: Int, wireX: Double) {
		LOG.trace("Build wire for OR input port $portId")
		val vv = orTerm.andOrConstantViews[portId - 1]
		val destPort = orTerm.orView!!.model.getInput<DigitalSignal>(portId)
		val origPoint = vv.getPortConnectionPoint(vv.model.getOutput<DigitalSignal>())
		val destPoint = orTerm.orView!!.getPortConnectionPoint(destPort)
		val points = mutableListOf<Point2D>()

		points.add(origPoint)
		points.add(Point2D(wireX, origPoint.y))
		points.add(Point2D(wireX, destPoint.y))
		points.add(destPoint)

		if (!destPort.isConnected) {
			circuitBuilder.connect(vv, vv.model.getOutput(), orTerm.orView!!, destPort, points)
		}
	}

	private fun buildOutput(orTerm: OrTerm) {
		val vv = if (orTerm.orView == null) {
			// Wire from Constant to output
			orTerm.andOrConstantViews[0]
		} else {
			// Wire from OR gate to output
			orTerm.orView!!
		}

		val p = Point2D(x, vv.getPortConnectionPoint(vv.model.getOutput<DigitalSignal>()).yInt)
		val outputView = circuitBuilder.addOutput(orTerm.outputName, p)
		circuitBuilder.connect(vv, outputView)
	}
}