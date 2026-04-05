package io.antarescircuit.antares.view.synthesis

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.gate.LogicGateType
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.And
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Or
import io.antarescircuit.antares.model.gate.UnaryLogicGateType.Not
import io.antarescircuit.antares.model.signal.Bit.*
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.model.truthtable.TruthTableService
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.net.ConstantView
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.view.VerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AndOrCircuitFromTruthTableBuilderTest {

	private val truthTableService = TruthTableService()

	init {
		AntaresTestRule.configure()
	}

	private fun getLogicGateViews(metaGraph: MetaGraph, type: LogicGateType): Collection<VerticeView<*>> =
		metaGraph.graph.graphView.getVerticeViews().filter { it is LogicGateView && it.model.gateType == type }

	@Test
	fun shouldBuildXorCircuit() {
		// O = A'B + AB'
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("O"))
		truthTable.setColumnValues(2, False, True, True, False)

		val metaGraph = MetaGraph.create(TranslatableText("Test"), AntaresGraphTypes.Digital)
		AndOrCircuitFromTruthTableBuilder(truthTable, truthTableService.generateDnfs(truthTable), metaGraph.graph).build()

		assertEquals(3, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<DigitalCircuitInOutView>().size)
		assertEquals(2, getLogicGateViews(metaGraph, Not).size)
		assertEquals(2, getLogicGateViews(metaGraph, And).size)
		assertEquals(1, getLogicGateViews(metaGraph, Or).size)
	}

	@Test
	fun shouldBuildCircuitWithConstantTrue() {
		// Y = A'B + AB'
		// Z = 1
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("Y", "Z"))
		truthTable.setColumnValues(2, False, True, True, False)
		truthTable.setColumnValues(3, True, True, True, True)

		val metaGraph = MetaGraph.create(TranslatableText("Test"), AntaresGraphTypes.Digital)
		AndOrCircuitFromTruthTableBuilder(truthTable, truthTableService.generateDnfs(truthTable), metaGraph.graph).build()

		assertEquals(4, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<DigitalCircuitInOutView>().size)
		assertEquals(1, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<ConstantView>().size)
		assertEquals(2, getLogicGateViews(metaGraph, And).size)
		// No OR gate necessary for the Y expression
		assertEquals(1, getLogicGateViews(metaGraph, Or).size)
	}

	@Test
	fun shouldBuildCircuitWithConstantFalse() {
		// Y = A'B + AB'
		// Z = 0
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("Y", "Z"))
		truthTable.setColumnValues(2, False, True, True, False)
		truthTable.setColumnValues(3, False, False, False, False)

		val metaGraph = MetaGraph.create(TranslatableText("Test"), AntaresGraphTypes.Digital)
		AndOrCircuitFromTruthTableBuilder(truthTable, truthTableService.generateDnfs(truthTable), metaGraph.graph).build()

		assertEquals(4, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<DigitalCircuitInOutView>().size)
		assertEquals(1, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<ConstantView>().size)
		assertEquals(2, getLogicGateViews(metaGraph, And).size)
		// No OR gate necessary for the Y expression
		assertEquals(1, getLogicGateViews(metaGraph, Or).size)
	}

	@Test
	fun shouldBuildSingleFactorAndTerm() {
		// Y = C' + AB
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B", "C"), outputColumnNames = listOf("Y"))
		truthTable.setColumnValues(3, True, False, True, False, Error, False, True, True)

		val metaGraph = MetaGraph.create(TranslatableText("Test"), AntaresGraphTypes.Digital)
		AndOrCircuitFromTruthTableBuilder(truthTable, truthTableService.generateDnfs(truthTable), metaGraph.graph).build()

		assertEquals(4, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<DigitalCircuitInOutView>().size)
		assertEquals(0, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<ConstantView>().size)
		// No AND gate necessary for the single-factor AND term
		assertEquals(1, getLogicGateViews(metaGraph, And).size)
		assertEquals(1, getLogicGateViews(metaGraph, Or).size)

		assertTrue(getLogicGateViews(metaGraph, Or)
			.first()
			.model.getInputs().all { it.isConnected }
		)
	}

	@Test
	fun shouldBuildSingleFactorOrTerm() {
		// Y = A
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("Y"))
		truthTable.setColumnValues(2, False, False, True, True)

		val metaGraph = MetaGraph.create(TranslatableText("Test"), AntaresGraphTypes.Digital)
		AndOrCircuitFromTruthTableBuilder(truthTable, truthTableService.generateDnfs(truthTable), metaGraph.graph).build()

		assertEquals(3, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<DigitalCircuitInOutView>().size)
		assertEquals(0, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<ConstantView>().size)
		assertEquals(0, getLogicGateViews(metaGraph, And).size)
		assertEquals(0, getLogicGateViews(metaGraph, Or).size)

		val inputA = metaGraph.graph.model!!.getGraphInput<DigitalSignal>("A")!!
		val outputY = metaGraph.graph.model!!.getGraphOutput<DigitalSignal>("Y")!!
		assertSame(inputA.getOutput<DigitalSignal>().net, outputY.getInput<DigitalSignal>().net)
	}

	/**
	 * Regression test for GitHub #1077.
	 */
	@Test
	fun shouldBuildSubjunction() {
		// O = A' + B
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("O"))
		truthTable.setColumnValues(2, True, True, False, True)

		val metaGraph = MetaGraph.create(TranslatableText("Test"), AntaresGraphTypes.Digital)
		AndOrCircuitFromTruthTableBuilder(truthTable, truthTableService.generateDnfs(truthTable), metaGraph.graph).build()

		assertEquals(3, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<DigitalCircuitInOutView>().size)
		assertEquals(2, getLogicGateViews(metaGraph, Not).size)
		assertEquals(0, getLogicGateViews(metaGraph, And).size)
		assertEquals(1, getLogicGateViews(metaGraph, Or).size)

		assertEquals(4, metaGraph.graph.graphView.getNodeViews().size)
		assertEquals(13, metaGraph.graph.graphView.getEdgeViews().size)
	}
}