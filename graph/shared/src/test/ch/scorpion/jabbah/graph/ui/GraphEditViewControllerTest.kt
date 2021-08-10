package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.TestEditorBuilder
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertSame

class GraphEditViewControllerTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val eventBus = EventBusImpl()
	private val graphViewBuilder = GraphViewBuilder<Boolean>()
	private val applicationContextHolder = GraphApplicationContextHolder()
	private val drawingView = DrawingViewImpl(graphViewBuilder.graphView as Drawing<Component>, applicationContextHolder = applicationContextHolder, eventBus = eventBus)
	private val editor = TestEditorBuilder().withDrawingView(drawingView as DrawingView<GraphView>).build()
	private val controller = GraphEditViewController(editor, mockk(), applicationContextHolder, eventBus = eventBus)

	init {
		drawingView.canvas = mockk(relaxed = true)
		GraphEditViewMockBuilder(controller)
	}

	@Test
	fun shouldForwardGraphViewToInnerControllers() {
		val newGraphView = GraphViewBuilder<Boolean>().build()
		controller.setGraphView(newGraphView, editable = true)
		assertSame(newGraphView, controller.scenarioViewController.graphView)
		assertSame(newGraphView, controller.usecaseViewController.graphView)
	}
}