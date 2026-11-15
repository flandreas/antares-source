package io.antarescircuit.jabbah.graph.view.connect

import io.antarescircuit.jabbah.base.event.Modifier
import io.antarescircuit.jabbah.draw.VirtualCanvas
import io.antarescircuit.jabbah.graph.health.GraphViewConsistencyCheck
import io.antarescircuit.jabbah.graph.view.AbstractGraphViewEditingTest
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.*

/** Exercise the actual editor listener, without supplying any native CLICKED events. */
class ReleaseClickConnectorIntegrationTest : AbstractGraphViewEditingTest() {

	private val canvas: VirtualCanvas

	init {
		editor.active = false
		canvas = VirtualCanvas(view).apply { hasFocus = true }
		CurrentConnectMethod.defaultMethod = ConnectMethod.SetPoints
	}

	override fun setupCircuit() {
		builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v1", 100, 100))
		builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v2", 200, 100))
		builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v3", 200, 200))
	}

	private fun clickWithMovement(x: Int, y: Int, count: Int = 1, modifiers: Int = 0) {
		canvas.moveMouseTo(x, y, modifiers)
		canvas.pressMouseAt(x, y, modifiers, clickCount = count)
		canvas.dragMouseTo(x + 1, y, modifiers)
		canvas.releaseMouseAt(x + 1, y, modifiers)
	}

	@Test
	fun shouldStartAddPointsAndCompleteAtInputDespiteMovement() {
		clickWithMovement(130, 100)
		assertEquals(1, view.drawing.getEdgeViews().size)
		val connector = GraphViewModule.outputToInputOrEdgeConnector
		assertNotNull(connector.adjustment)

		clickWithMovement(150, 160)
		assertEquals(2, connector.adjustment!!.model.size)
		clickWithMovement(190, 100)

		assertNull(connector.adjustment)
		val edge = view.drawing.getEdgeViews().single()
		assertNotNull(edge.origin)
		assertNotNull(edge.destination)
		GraphViewConsistencyCheck.execute(view.drawing)
	}

	@Test
	fun shouldStartAtInputAndCompleteAtOutputDespiteMovement() {
		clickWithMovement(190, 100)
		assertNotNull(GraphViewModule.inputToOutputOrEdgeConnector.adjustment)
		clickWithMovement(130, 100)

		assertNull(GraphViewModule.inputToOutputOrEdgeConnector.adjustment)
		val edge = view.drawing.getEdgeViews().single()
		assertNotNull(edge.origin)
		assertNotNull(edge.destination)
		GraphViewConsistencyCheck.execute(view.drawing)
	}

	@Test
	fun shouldCompleteOpenWireOnSecondReleaseOfDoubleClick() {
		clickWithMovement(130, 100)
		clickWithMovement(170, 200)
		clickWithMovement(170, 200, count = 2)

		assertNull(GraphViewModule.outputToInputOrEdgeConnector.adjustment)
		val edge = view.drawing.getEdgeViews().single()
		assertNotNull(edge.origin)
		assertNull(edge.destination)
		GraphViewConsistencyCheck.execute(view.drawing)
	}

	@Test
	fun shouldBranchFromWireDespiteMovement() {
		builder.connect(builder.graphView.getVerticeView("v1")!!, builder.graphView.getVerticeView("v2")!!)
		clickWithMovement(150, 100, modifiers = Modifier.Alt.mask)
		assertNotNull(GraphViewModule.edgeToPortOrEdgeConnector.adjustment)
		clickWithMovement(190, 200)

		assertNull(GraphViewModule.edgeToPortOrEdgeConnector.adjustment)
		assertEquals(3, view.drawing.getEdgeViews().size)
		GraphViewConsistencyCheck.execute(view.drawing)
	}

	@Test
	fun shouldKeepAutoLayoutDraggingWorking() {
		CurrentConnectMethod.defaultMethod = ConnectMethod.AutoLayout
		canvas.moveMouseTo(130, 100)
		canvas.pressMouseAt(130, 100)
		canvas.dragMouseTo(190, 100)
		canvas.releaseMouseAt(190, 100)

		val edge = view.drawing.getEdgeViews().single()
		assertNotNull(edge.origin)
		assertNotNull(edge.destination)
		GraphViewConsistencyCheck.execute(view.drawing)
	}
}
