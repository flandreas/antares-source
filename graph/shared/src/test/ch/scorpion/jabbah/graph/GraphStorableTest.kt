package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule
import kotlin.test.*


/** Unit tests for [GraphStorable].*/
class GraphStorableTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@BeforeTest
	fun setup() {
		TestTranslationsBuilder().withAnyKey()
		IOModule.typeMap.register("testVertice", TestVertice::class)
		IOModule.typeMap.register("testVerticeView", TestVerticeView::class)
	}

	@Test
	fun shouldBeStorable() {
		val testGraph = TestGraphView()
		val orig = GraphStorable(testGraph.graphView)
		val clone: GraphStorable = IOModule.storableClonerProvider.invoke().cloneUsingCreator(orig, IOModule.storableCreator) as GraphStorable

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