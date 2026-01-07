package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.Modifier.Alt
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.health.GraphViewConsistencyCheck
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.AbstractInputEventHandlerTest
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.*

class EdgeToEdgeConnectorTest : AbstractInputEventHandlerTest(){

	private lateinit var v3: TestVerticeView
	private lateinit var v4: TestVerticeView
	private lateinit var v5: TestVerticeView

	@BeforeTest
	fun initialize() {
		handler = GraphViewModule.edgeToPortOrEdgeConnector.handler
		v3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
		v4 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v4", 100, 300))
		v5 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v5", 200, 300))

		val ev1 = builder.connect(v1, v2)
		builder.split(ev1, 0, Point2D(150, 100), v3)
		builder.connect(v4, v5)
		EditModule.drawingAppService.delete(listOf(ev1), editor.view)

		editor.commandManager.reset()
		CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout
	}

	private fun connect() {
		mouseMoveTo(150, 300, modifiers = Alt.mask)
		pressMouseAt(150, 300, modifiers = Alt.mask)
		dragMouseTo(150, 200)
		releaseMouseAt(150, 200)
	}

	@Test
	fun shouldConnectEdgeViewToEdgeView() {
		connect()

		val edgeViews = builder.graphView.getEdgeViews()

		assertEquals(2, builder.graphView.getNodeViews().size)
		assertEquals(1, builder.graphView.graph!!.elements.filterIsInstance<Net<*>>().size)
		assertEquals(5, edgeViews.size)

		val net = builder.graphView.graph!!.elements.first { it is Net<*> } as Net<Boolean>

		edgeViews.forEach {
			assertSame(net, it.model)
			assertFalse(it.hasBrokenPortRef)
		}

		assertNull(net.designError)
		assertEquals(4, net.portsCount)
		assertSame(net, v2.model.getInput<Boolean>().net)
		assertSame(net, v3.model.getInput<Boolean>().net)
		assertSame(net, v4.model.getOutput<Boolean>().net)
		assertSame(net, v5.model.getInput<Boolean>().net)

		GraphViewConsistencyCheck.execute(builder.graphView)
	}
}