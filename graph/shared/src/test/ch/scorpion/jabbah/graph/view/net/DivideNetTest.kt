package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * If an [EdgeView] between two [NodeView]s is deleted, the [Net] must perhaps be divided.
 */
class DivideNetTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val builder: GraphViewBuilder<Boolean> = GraphViewBuilder()
	private val drawingViewBuilder = DrawingViewMockBuilder().withDrawing(builder.build())
	private val vv1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv1", 100, 100))
	private val vv2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv2", 200, 100))
	private val vv3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv3", 200, 200))
	private val vv4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("vv4", 200, 300))
	private val ev12 = builder.connect(vv1, vv2)
	private val split3 = builder.split(ev12, 0, Point2D(150, 100), vv3)
	private val split4 = builder.split(split3.newEdgeView, 0, Point2D(150, 200), vv4)

	init {
		EditModule.commandManager.bindDataHolder(builder)
	}

	/** TODO Test is waiting for business logic to be implemented. See GitHub issue #69. */
	@Test
	@Ignore
	fun shouldDivideNet() {
		EditModule.drawingAppService.delete(listOf(split3.newEdgeView), drawingViewBuilder.build<GraphElementView<GraphElement>>())

		assertSame(vv1.model.getOutput<Boolean>().net, vv2.model.getInput<Boolean>().net)
		assertSame(vv3.model.getInput<Boolean>().net, vv4.model.getInput<Boolean>().net)

		assertNotSame(vv2.model.getInput<Boolean>().net, vv4.model.getInput<Boolean>().net)
	}
}