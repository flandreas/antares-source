package io.antarescircuit.jabbah.graph

import io.antarescircuit.jabbah.graph.model.*
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.TestGraphView
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import io.antarescircuit.jabbah.io.StorableCloner
import kotlin.test.*

class GraphStorableTest {

	@BeforeTest
	fun setup() {
		GraphViewTestRule.configure()
	}

	@Test
	fun shouldBeStorable() {
		val testGraph = TestGraphView()
		val orig = GraphStorable(testGraph.graphView)
		val clone: GraphStorable = StorableCloner.clone(orig)

		// Assert model connectedness
		val graph: Graph = clone.graphView.graph!!

		assertEquals(3, (graph.withId(1) as Vertice).getOutput<Boolean>().net?.id)
		assertEquals(3, (graph.withId(2) as Vertice).getInput<Boolean>().net?.id)

		assertTrue((graph.withId(3) as Net<Boolean>).isConnectedWith((graph.withId(1) as Vertice).getOutput()))
		assertTrue((graph.withId(3) as Net<Boolean>).isConnectedWith((graph.withId(2) as Vertice).getInput()))

		// Assert view connectedness
		val graphView = clone.graphView

		assertSame((graphView.getWithId(3) as EdgeView<Boolean>).origin!!.connectableView as TestVerticeView, graphView.getWithId(1))
		assertSame((graphView.getWithId(3) as EdgeView<Boolean>).origin!!.port as OutputPort<Boolean>, (graphView.getWithId(1)!!.model as TestVertice).getOutput())

		assertSame((graphView.getWithId(3) as EdgeView<Boolean>).destination!!.connectableView as TestVerticeView, graphView.getWithId(2))
		assertSame((graphView.getWithId(3) as EdgeView<Boolean>).destination!!.port as InputPort<Boolean>, (graphView.getWithId(2)!!.model as TestVertice).getInput())
	}
}