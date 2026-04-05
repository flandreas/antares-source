package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.CanvasMockBuilder
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.view.DrawingViewImpl
import io.antarescircuit.jabbah.execution.scheduler.SchedulerImpl
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.GraphEditorMockBuilder
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolderImpl
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.editor.GraphEditor
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertSame

class GraphEditViewControllerTest {

	private val eventBus = EventBusImpl()
	private val systemSpeed = SystemSpeed(eventBus = eventBus)
	private val currentSystemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed, eventBus)
	private val graphViewBuilder: GraphViewBuilder<Boolean>
	private val scheduler: SchedulerImpl
	private val applicationContextHolder: GraphApplicationContextHolder
	private val drawingView: DrawingViewImpl<Drawing<Component>>
	private val editor: Editor
	private val applicationModeHolder: ApplicationModeHolderImpl
	private val controller: GraphEditViewController

	init {
		GraphViewTestRule.configure()
		graphViewBuilder = GraphViewBuilder<Boolean>()
		scheduler = SchedulerImpl(currentSystemSpeedCategory)
		applicationContextHolder = GraphApplicationContextHolder(scheduler, eventBus, systemSpeed, currentSystemSpeedCategory)
		drawingView = DrawingViewImpl(
			graphViewBuilder.graphView as Drawing<Component>,
			applicationContextHolder = applicationContextHolder,
			eventBus = eventBus)
		editor = GraphEditorMockBuilder().withDrawingView(drawingView as DrawingView<GraphView>).build()
		applicationModeHolder = ApplicationModeHolderImpl(editor, scheduler).also {
			applicationContextHolder.applicationModeHolder = it
		}
		controller = GraphEditViewController(editor, mock(MockMode.autofill), applicationModeHolder, applicationContextHolder, eventBus = eventBus)

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