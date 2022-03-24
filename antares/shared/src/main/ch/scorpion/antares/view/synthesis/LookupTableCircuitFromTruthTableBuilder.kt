package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.quinemccluskey.DNF
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.addressable.LookupTableView
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.net.ConcentratorView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.GraphStorable
import kotlin.math.max

class LookupTableCircuitFromTruthTableBuilder(
	truthTable: TruthTable,
	dnfs: List<DNF>,
	graphStorable: GraphStorable
) : AbstractCircuitFromTruthTableBuilder(truthTable, dnfs, graphStorable) {

	companion object {
		private const val CONCENTRATOR_Y = INPUT_Y + Look.SCALE * 14
		private const val CONCENTRATOR_DIST_X = Look.SCALE * 8
		private const val CONCENTRATOR_GAP_Y = Look.SCALE * 8
		private const val LOOKUP_TABLE_DIST_X = Look.SCALE * 20
	}

	private data class ExpressionView(
		val outputColumn: Int,
		val outputName: String,
		val dnf: DNF,
		var yPos: Int = 0,
		var concentrator: ConcentratorView? = null,
		var lut: LookupTableView? = null,
		var output: CircuitInOutView? = null
	)

	private var concentratorY = CONCENTRATOR_Y

	override fun build() {

		if (truthTable.inputColumnCount > 8) {
			throw CircuitFromTruthTableBuilderError(Translations.getString("antares.synthesis.maxLutBitWidthExceeded.error"))
		}

		val expressionViews = mutableListOf<ExpressionView>()

		with (truthTable) {
			for (col in inputColumnCount until inputColumnCount + outputColumnCount) {
				expressionViews.add(createExpressionView(col))
			}
		}

		x = 0
		buildInputs(addNotViews = false)

		// The stack of all ConcentratorViews and LookupTableViews determines how long the input wires
		// have to be, so build them first prior to building the wires
		x += CONCENTRATOR_DIST_X
		expressionViews.forEach { buildConcentratorAndLut(it) }

		buildInputWires(concentratorY)

		expressionViews.forEach {
			buildConcentratorWires(it)
			buildLutInputWires(it)
		}

		x += LOOKUP_TABLE_DIST_X + OUTPUT_DIST_X
		expressionViews.forEach {
			buildOutput(it)
			buildOutputWire(it)
		}
	}

	private fun createExpressionView(outputColumn: Int): ExpressionView {
		val dnf = dnfs[outputColumn - truthTable.inputColumnCount]
		return ExpressionView(outputColumn, truthTable.getColumnName(outputColumn), dnf)
	}

	private fun buildConcentratorAndLut(expressionView: ExpressionView) {
		val concentratorView = circuitBuilder.addConcentrator(
			BitWidth.of(truthTable.inputColumnCount),
			BranchCount.withCount(truthTable.inputColumnCount),
			Handedness.LEFT,
			Point2D(x, concentratorY)
		)
		expressionView.concentrator = concentratorView

		val lutView = circuitBuilder.addLookupTable(
			BitWidth.of(truthTable.inputColumnCount),
			BitWidth.BW_1,
			Point2D(x + LOOKUP_TABLE_DIST_X, concentratorY)
		)
		lutView.model.fillFromTruthTable(truthTable, expressionView.outputColumn)
		expressionView.lut = lutView

		expressionView.yPos = concentratorY
		concentratorY += max(concentratorView.bounds.heightInt, lutView.bounds.heightInt) + CONCENTRATOR_GAP_Y
	}

	private fun buildConcentratorWires(expressionView: ExpressionView) {
		for (index in 0 until expressionView.concentrator!!.model.inputCount) {
			// The first input of ConcentratorView has portId 2
			splitInputWire(expressionView.concentrator!!, 1 + truthTable.inputColumnCount - index, index, inverted = false)
		}
	}

	private fun buildLutInputWires(expressionView: ExpressionView) {
		circuitBuilder.connect(expressionView.concentrator!!, expressionView.lut!!)
	}

	private fun buildOutput(expressionView: ExpressionView) {
		expressionView.output = circuitBuilder.addOutput(expressionView.outputName, Point2D(x, expressionView.yPos))
	}

	private fun buildOutputWire(expressionView: ExpressionView) {
		circuitBuilder.connect(expressionView.lut!!, expressionView.output!!)
	}
}