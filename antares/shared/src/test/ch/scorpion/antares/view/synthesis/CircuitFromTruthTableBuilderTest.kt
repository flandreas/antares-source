package ch.scorpion.antares.view.synthesis

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.antares.view.gate.NotGateView
import ch.scorpion.antares.view.gate.OrGateView
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.net.ConstantView
import ch.scorpion.jabbah.graph.MetaGraph
import kotlin.test.Test
import kotlin.test.assertEquals

class CircuitFromTruthTableBuilderTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldBuildXorCircuit() {
		// O = A'B + AB'
		val truthTable = TruthTable(inputColumnNames = listOf("A", "B"), outputColumnNames = listOf("O"))
		truthTable.setValue(0, 2, Bit.False)
		truthTable.setValue(1, 2, Bit.True)
		truthTable.setValue(2, 2, Bit.True)
		truthTable.setValue(3, 2, Bit.False)

		val metaGraph = MetaGraph.withName("Test")
		CircuitFromTruthTableBuilder(truthTable, metaGraph.graph).build()

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
		truthTable.setValue(0, 2, Bit.False)
		truthTable.setValue(1, 2, Bit.True)
		truthTable.setValue(2, 2, Bit.True)
		truthTable.setValue(3, 2, Bit.False)

		truthTable.setValue(0, 3, Bit.True)
		truthTable.setValue(1, 3, Bit.True)
		truthTable.setValue(2, 3, Bit.True)
		truthTable.setValue(3, 3, Bit.True)

		val metaGraph = MetaGraph.withName("Test")
		CircuitFromTruthTableBuilder(truthTable, metaGraph.graph).build()

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
		truthTable.setValue(0, 2, Bit.False)
		truthTable.setValue(1, 2, Bit.True)
		truthTable.setValue(2, 2, Bit.True)
		truthTable.setValue(3, 2, Bit.False)

		truthTable.setValue(0, 3, Bit.False)
		truthTable.setValue(1, 3, Bit.False)
		truthTable.setValue(2, 3, Bit.False)
		truthTable.setValue(3, 3, Bit.False)

		val metaGraph = MetaGraph.withName("Test")
		CircuitFromTruthTableBuilder(truthTable, metaGraph.graph).build()

		assertEquals(4, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<CircuitInOutView>().size)
		assertEquals(1, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<ConstantView>().size)
		assertEquals(2, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<AndGateView>().size)
		// No OR gate necessary for the Y expression
		assertEquals(1, metaGraph.graph.graphView.getVerticeViews().filterIsInstance<OrGateView>().size)
	}
}