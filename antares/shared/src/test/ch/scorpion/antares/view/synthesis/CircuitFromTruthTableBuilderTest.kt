package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableService
import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.antares.view.gate.NotGateView
import ch.scorpion.antares.view.gate.OrGateView
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.net.ConstantView
import ch.scorpion.jabbah.graph.MetaGraph
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CircuitFromTruthTableBuilderTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	private val truthTableService = TruthTableService()

	@Test
	fun shouldBuildXorCircuit() {
		// O = A'B + AB'
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("O"))
		truthTable.setColumnValues(2, False, True, True, False)

		val metaGraph = MetaGraph.withName("Test")
		CircuitFromTruthTableBuilder(truthTable, truthTableService.generateDnfs(truthTable), metaGraph.graph).build()

		assertEquals(3, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<CircuitInOutView>().size)
		assertEquals(2, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<NotGateView>().size)
		assertEquals(2, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<AndGateView>().size)
		assertEquals(1, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<OrGateView>().size)
	}

	@Test
	fun shouldBuildCircuitWithConstantTrue() {
		// Y = A'B + AB'
		// Z = 1
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("Y", "Z"))
		truthTable.setColumnValues(2, False, True, True, False)
		truthTable.setColumnValues(3, True, True, True, True)

		val metaGraph = MetaGraph.withName("Test")
		CircuitFromTruthTableBuilder(truthTable, truthTableService.generateDnfs(truthTable), metaGraph.graph).build()

		assertEquals(4, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<CircuitInOutView>().size)
		assertEquals(1, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<ConstantView>().size)
		assertEquals(2, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<AndGateView>().size)
		// No OR gate necessary for the Y expression
		assertEquals(1, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<OrGateView>().size)
	}

	@Test
	fun shouldBuildCircuitWithConstantFalse() {
		// Y = A'B + AB'
		// Z = 0
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("Y", "Z"))
		truthTable.setColumnValues(2, False, True, True, False)
		truthTable.setColumnValues(3, False, False, False, False)

		val metaGraph = MetaGraph.withName("Test")
		CircuitFromTruthTableBuilder(truthTable, truthTableService.generateDnfs(truthTable), metaGraph.graph).build()

		assertEquals(4, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<CircuitInOutView>().size)
		assertEquals(1, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<ConstantView>().size)
		assertEquals(2, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<AndGateView>().size)
		// No OR gate necessary for the Y expression
		assertEquals(1, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<OrGateView>().size)
	}

	@Test
	fun shouldBuildSingleFactorAndTerm() {
		// Y = C' + AB
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B", "C"), outputColumnNames = listOf("Y"))
		truthTable.setColumnValues(3, True, False, True, False, Error, False, True, True)

		val metaGraph = MetaGraph.withName("Test")
		CircuitFromTruthTableBuilder(truthTable, truthTableService.generateDnfs(truthTable), metaGraph.graph).build()

		assertEquals(4, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<CircuitInOutView>().size)
		assertEquals(0, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<ConstantView>().size)
		// No AND gate necessary for the single-factor AND term
		assertEquals(1, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<AndGateView>().size)
		assertEquals(1, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<OrGateView>().size)

		assertTrue(metaGraph.graph.graphView.getVerticeViews().filterIsInstance<OrGateView>()
			.first()
			.model.getInputs().all { it.isConnected }
		)
	}

	@Test
	fun shouldBuildSingleFactorOrTerm() {
		// Y = A
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("Y"))
		truthTable.setColumnValues(2, False, False, True, True)

		val metaGraph = MetaGraph.withName("Test")
		CircuitFromTruthTableBuilder(truthTable, truthTableService.generateDnfs(truthTable), metaGraph.graph).build()

		assertEquals(3, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<CircuitInOutView>().size)
		assertEquals(0, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<ConstantView>().size)
		assertEquals(0, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<AndGateView>().size)
		assertEquals(0, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<OrGateView>().size)

		val inputA = metaGraph.graph.model!!.getGraphInput<DigitalSignal>("A")!!
		val outputY = metaGraph.graph.model!!.getGraphOutput<DigitalSignal>("Y")!!
		assertSame(inputA.getOutput<DigitalSignal>().net, outputY.getInput<DigitalSignal>().net)
	}
}