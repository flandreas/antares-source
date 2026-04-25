package io.antarescircuit.jabbah.graph.ui

import dev.mokkery.MockMode
import dev.mokkery.mock
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.CanvasMockBuilder
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.view.DrawingViewImpl
import io.antarescircuit.jabbah.execution.scheduler.SchedulerImpl
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.GraphEditorMockBuilder
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolderImpl
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import kotlin.test.Test
import kotlin.test.assertSame

class GraphEditViewControllerTest {

	private val eventBus = EventBusImpl()
	private val systemSpeed = SystemSpeed(eventBus = eventBus)
	private val currentSystemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed, eventBus)
	private val graphViewBuilder: GraphViewBuilder<Boolean>
	private val scheduler: SchedulerImpl
	private val applicationContextHolder: GraphApplicationContextHolder
	private val drawingView: DrawingViewImpl<GraphElementView<*>, GraphView>
	private val editor: Editor
	private val applicationModeHolder: ApplicationModeHolderImpl
	private val controller: GraphEditViewController

	init {
		GraphViewTestRule.configure()
		graphViewBuilder = GraphViewBuilder()
		scheduler = SchedulerImpl(currentSystemSpeedCategory)
		applicationContextHolder = GraphApplicationContextHolder(scheduler, eventBus, systemSpeed, currentSystemSpeedCategory)
		drawingView = DrawingViewImpl(
			graphViewBuilder.graphView,
			applicationContextHolder = applicationContextHolder,
			eventBus = eventBus)
		editor = GraphEditorMockBuilder().withDrawingView(drawingView).build()
		applicationModeHolder = ApplicationModeHolderImpl(editor, scheduler).also {
			applicationContextHolder.applicationModeHolder = it
		}
		controller = GraphEditViewController(drawingView, editor, mock(MockMode.autofill), applicationModeHolder, applicationContextHolder, eventBus = eventBus)

		drawingView.canvas = CanvasMockBuilder().build()
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