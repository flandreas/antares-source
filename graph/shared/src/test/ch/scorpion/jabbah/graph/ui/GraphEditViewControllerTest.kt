package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.CanvasMockBuilder
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.GraphEditorMockBuilder
import ch.scorpion.jabbah.graph.app.ApplicationModeHolderImpl
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.editor.GraphEditor
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