package ch.scorpion.jabbah.graph.view.net

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.*

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

	@Test
	fun shouldNotDivideNet() {
		EditModule.drawingAppService.delete(
			listOf(ev12),
			drawingViewBuilder.build<GraphElementView<GraphElement>>())

		assertSame(vv2.model.getInput<Boolean>().net, vv3.model.getInput<Boolean>().net)
		assertSame(vv3.model.getInput<Boolean>().net, vv4.model.getInput<Boolean>().net)
	}

	@Test
	fun shouldDivideNet() {
		EditModule.drawingAppService.delete(
			listOf(split3.newEdgeView),
			drawingViewBuilder.build<GraphElementView<GraphElement>>())

		assertEquals(6, builder.graph.elementsCount)
		assertEquals(6, builder.graphView.drawables.size)
		assertEquals(2, builder.graph.elements.filterIsInstance<Net<*>>().size)
		assertEquals(2, (builder.graphView as GraphViewImpl).getNetViewMap().size)
		assertEquals(2, builder.graphView.getEdgeViews().size)

		// Net 1
		val net1 = builder.graph.elements.filterIsInstance<Net<*>>()[0]
		assertEquals(2, net1.portsCount)
		assertSame(net1, vv1.model.getOutput<Boolean>().net as Net<Boolean>)
		assertSame(net1, vv2.model.getInput<Boolean>().net as Net<Boolean>)
		assertNotSame(vv2.model.getInput<Boolean>().net, vv4.model.getInput<Boolean>().net)

		// NetView 1
		val netView1 = (builder.graphView as GraphViewImpl).getNetViewMap()[net1]!!
		assertEquals(1, netView1.getElements().size)
		assertSame(net1, builder.graphView.getEdgeViews()[1].model)
		assertContains(netView1.getElements() as List<NetViewElement<Boolean>>, builder.graphView.getEdgeView(vv1.model.getOutput<Boolean>()) as EdgeView<Boolean>)
		assertContains(netView1.getElements() as List<NetViewElement<Boolean>>, builder.graphView.getEdgeView(vv2.model.getInput<Boolean>()) as EdgeView<Boolean>)

		// Net 2
		val net2 = builder.graph.elements.filterIsInstance<Net<*>>()[1]
		assertNotSame(net1, net2)
		assertEquals(2, net2.portsCount)
		assertSame(net2, vv3.model.getInput<Boolean>().net as Net<Boolean>)
		assertSame(net2, vv4.model.getInput<Boolean>().net as Net<Boolean>)

		// NetView 2
		val netView2 = (builder.graphView as GraphViewImpl).getNetViewMap()[net2]!!
		assertNotSame(netView1, netView2)
		assertSame(net2, builder.graphView.getEdgeViews()[0].model)
		assertEquals(1, netView2.getElements().size)
		assertContains(netView2.getElements() as List<NetViewElement<Boolean>>, builder.graphView.getEdgeView(vv3.model.getInput<Boolean>()) as EdgeView<Boolean>)
		assertContains(netView2.getElements() as List<NetViewElement<Boolean>>, builder.graphView.getEdgeView(vv4.model.getInput<Boolean>()) as EdgeView<Boolean>)
	}
}