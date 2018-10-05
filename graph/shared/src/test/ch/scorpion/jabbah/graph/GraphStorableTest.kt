package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [GraphStorable].
 */
class GraphStorableTest {

    companion object {
        @ClassRule @JvmField
        val rule = GraphViewTestRule()
    }

    @Before
    fun setup() {
	    TestTranslationsBuilder().withAnyKey()
        IOModule.typeMap.register("testVertice", TestVertice::class)
        IOModule.typeMap.register("testVerticeView", TestVerticeView::class)
    }

    @Test
    fun shouldBeStorable() {
        val testGraph = TestGraphView()
        val orig: GraphStorable = GraphStorable(testGraph.graphView)
        val clone: GraphStorable = IOModule.storableClonerProvider.invoke().cloneUsingCreator(orig, IOModule.storableCreator) as GraphStorable

        // Assert model connectedness
        val graph: Graph = clone.graphView!!.graph!!

        assertThat((graph.withId(1) as Vertice).getOutput<Boolean>().net?.id, `is`(3))
        assertThat((graph.withId(2) as Vertice).getInput<Boolean>().net?.id, `is`(3))

        assertThat((graph.withId(3) as Net<Boolean>).isConnectedWith((graph.withId(1) as Vertice).getOutput()), `is`(true))
        assertThat((graph.withId(3) as Net<Boolean>).isConnectedWith((graph.withId(2) as Vertice).getInput()), `is`(true))

        // Assert view connectedness
        val graphView = clone.graphView!!

        assertThat((graphView.getWidthId(3) as EdgeView<Boolean>).origin as TestVerticeView, `is`(sameInstance(graphView.getWidthId(1))))
        assertThat((graphView.getWidthId(3) as EdgeView<Boolean>).originPort as OutputPort<Boolean>, `is`(sameInstance((graphView.getWidthId(1)!!.model as TestVertice).getOutput<Boolean>())))

        assertThat((graphView.getWidthId(3) as EdgeView<Boolean>).destination as TestVerticeView, `is`(sameInstance(graphView.getWidthId(2))))
        assertThat((graphView.getWidthId(3) as EdgeView<Boolean>).destinationPort as InputPort<Boolean>, `is`(sameInstance((graphView.getWidthId(2)!!.model as TestVertice).getInput<Boolean>())))
    }
}