package io.antarescircuit.antares.view.synthesis

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.analysis.CircuitAnalysisService
import io.antarescircuit.antares.model.gate.LogicGateType
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.And
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Nand
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Nor
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Or
import io.antarescircuit.antares.model.gate.UnaryLogicGateType.Not
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Synthesizes circuits from truth tables and analyses them to verify the resulting truth tables. */
class CircuitSynthesisRoundTripTest {

	init {
		AntaresTestRule.configure()
	}

	private val gateTypes = listOf(
		CircuitSynthesisType.AndOrGate,
		CircuitSynthesisType.OrAndGate,
		CircuitSynthesisType.NandGate,
		CircuitSynthesisType.NorGate,
	)

	private val allowedGateTypes = mapOf(
		CircuitSynthesisType.AndOrGate to setOf(And, Or, Not),
		CircuitSynthesisType.OrAndGate to setOf(And, Or, Not),
		CircuitSynthesisType.NandGate to setOf(Nand),
		CircuitSynthesisType.NorGate to setOf(Nor),
	)

	private fun bit(value: Boolean): Bit = if (value) Bit.True else Bit.False

	/** Creates a [TruthTable] whose output columns are calculated from the input values of each row. */
	private fun truthTable(inputs: List<String>, outputs: Map<String, (List<Boolean>) -> Bit>): TruthTable {
		val truthTable = TruthTable(inputColumnNames = inputs, outputColumnNames = outputs.keys.toList())
		outputs.values.forEachIndexed { index, function ->
			for (row in 0 until truthTable.rowsCount) {
				val values = inputs.indices.map { truthTable.getValue(row, it) == Bit.True }
				truthTable.setValue(row, inputs.size + index, function(values))
			}
		}
		return truthTable
	}

	private fun synthesize(truthTable: TruthTable, type: CircuitSynthesisType, twoInputGatesOnly: Boolean): MetaGraph {
		val metaGraph = MetaGraph.create(TranslatableText("Test"), AntaresGraphTypes.Digital)
		type.build(truthTable, twoInputGatesOnly, metaGraph.graph)
		return metaGraph
	}

	private fun getLogicGateViews(metaGraph: MetaGraph): List<LogicGateView> =
		metaGraph.graph.graphView.getVerticeViews().filterIsInstance<LogicGateView>()

	/** Asserts that the synthesized circuit matches [truthTable] on all rows that aren't don't cares. */
	private fun assertRoundTrip(truthTable: TruthTable, type: CircuitSynthesisType, twoInputGatesOnly: Boolean) {
		val metaGraph = synthesize(truthTable, type, twoInputGatesOnly)
		val analysed = CircuitAnalysisService().analyse(metaGraph.graph.model as DigitalGraph)

		for (column in truthTable.inputColumnCount until truthTable.columnCount) {
			for (row in 0 until truthTable.rowsCount) {
				val expected = truthTable.getValue(row, column)
				if (expected != Bit.Error) {
					assertEquals(
						expected,
						analysed.getValue(row, column),
						"$type (two-input: $twoInputGatesOnly), ${truthTable.getColumnName(column)}, row $row",
					)
				}
			}
		}

		val usedGateTypes: Set<LogicGateType> = getLogicGateViews(metaGraph).map { it.model.gateType }.toSet()
		assertTrue(allowedGateTypes.getValue(type).containsAll(usedGateTypes), "$type uses $usedGateTypes")
		if (twoInputGatesOnly) {
			assertTrue(getLogicGateViews(metaGraph).all { it.model.inputCount <= 2 }, "$type has gates with more than 2 inputs")
		}
	}

	private fun assertRoundTripForAllGateTypes(truthTable: TruthTable) {
		for (type in gateTypes) {
			for (twoInputGatesOnly in listOf(false, true)) {
				assertRoundTrip(truthTable, type, twoInputGatesOnly)
			}
		}
	}

	@Test
	fun shouldSynthesizeXorAndMajority() {
		assertRoundTripForAllGateTypes(truthTable(
			listOf("A", "B", "C"),
			mapOf(
				"X" to { v -> bit(v[0] xor v[1] xor v[2]) },
				"M" to { v -> bit(v.count { it } >= 2) },
			),
		))
	}

	@Test
	fun shouldSynthesizeConstantsAndSingleLiterals() {
		assertRoundTripForAllGateTypes(truthTable(
			listOf("A", "B"),
			mapOf(
				"Zero" to { _ -> Bit.False },
				"One" to { _ -> Bit.True },
				"A1" to { v -> bit(v[0]) },
				"NotB" to { v -> bit(!v[1]) },
				// Single term with more than one literal
				"Nand" to { v -> bit(!(v[0] && v[1])) },
				"And" to { v -> bit(v[0] && v[1]) },
			),
		))
	}

	@Test
	fun shouldSynthesizeFunctionsWithDontCares() {
		assertRoundTripForAllGateTypes(truthTable(
			listOf("A", "B", "C", "D"),
			mapOf(
				// Prime number detector for BCD digits, codes above 9 are don't cares
				"P" to { v ->
					val n = v.fold(0) { acc, it -> acc * 2 + if (it) 1 else 0 }
					if (n > 9) Bit.Error else bit(n in setOf(2, 3, 5, 7))
				},
				// Only don't cares apart from a single 1 => constant 1
				"One" to { v -> if (v.all { it }) Bit.True else Bit.Error },
				// Only don't cares apart from a single 0 => constant 0
				"Zero" to { v -> if (v.all { it }) Bit.False else Bit.Error },
			),
		))
	}

	@Test
	fun shouldSplitWideTermsWithTwoInputGatesOnly() {
		// Terms with five literals, i.e. trees of uneven depth
		assertRoundTripForAllGateTypes(truthTable(
			listOf("A", "B", "C", "D", "E"),
			mapOf(
				"Y" to { v -> bit(v.all { it } || v.none { it }) },
			),
		))
	}

	@Test
	fun shouldSynthesizeMoreThanSixteenTermsWithTwoInputGatesOnly() {
		// XOR of 6 inputs has 32 terms
		val truthTable = truthTable(
			listOf("A", "B", "C", "D", "E", "F"),
			mapOf("Y" to { v -> bit(v.count { it } % 2 == 1) }),
		)

		val error = assertFailsWith<CircuitFromTruthTableBuilderError> {
			synthesize(truthTable, CircuitSynthesisType.NandGate, twoInputGatesOnly = false)
		}
		assertEquals(Translations.getString("antares.synthesis.maxOrInputCountExceeded.error"), error.message)

		assertRoundTrip(truthTable, CircuitSynthesisType.NandGate, twoInputGatesOnly = true)
		assertRoundTrip(truthTable, CircuitSynthesisType.NorGate, twoInputGatesOnly = true)
	}

	@Test
	fun shouldBuildNandNandCircuit() {
		// Y = AB + C' needs one NAND per multi-literal term, one output NAND and NAND inverters for the inputs
		val truthTable = truthTable(
			listOf("A", "B", "C"),
			mapOf("Y" to { v -> bit(v[0] && v[1] || !v[2]) }),
		)

		val metaGraph = synthesize(truthTable, CircuitSynthesisType.NandGate, twoInputGatesOnly = false)

		val gates = getLogicGateViews(metaGraph)
		assertTrue(gates.all { it.model.gateType == Nand })
		assertEquals(3 + 2, gates.size)
	}
}
