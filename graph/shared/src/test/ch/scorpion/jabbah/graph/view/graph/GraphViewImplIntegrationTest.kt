package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.connect.SplitEdgeViewResult
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.StorableCloner
import kotlin.test.BeforeTest
import kotlin.test.Test

class GraphViewImplIntegrationTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val builder = GraphViewBuilder<Boolean>("Test")
	private lateinit var vv1: TestVerticeView
	private lateinit var vv2: TestVerticeView
	private lateinit var vv3: TestVerticeView
	private lateinit var ev: EdgeView<Boolean>
	private lateinit var splitResult: SplitEdgeViewResult<Boolean>

	@BeforeTest
	fun buildGraphView() {
		vv1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv1", 0, 0))
		vv2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv2", 100, 0))
		vv3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv3", 100, 100))
		ev = builder.connect(vv1, vv2)
		splitResult = builder.split(ev, 0, Point2D(50, 0), vv3)
	}

	@Test
	fun shouldCloneForExistingModel() {
		val metaGraph = MetaGraph(builder.graphStorable, ContainerDrawing())
		val clone = StorableCloner.clone(metaGraph)

		metaGraph.graph.graphView.cloneForExistingModel(clone.graph.model!!)
	}
}