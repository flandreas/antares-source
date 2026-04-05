package io.antarescircuit.jabbah.graph.ui.graphpanel

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataContentEvent
import io.antarescircuit.jabbah.app.ApplicationDataEvent
import io.antarescircuit.jabbah.app.Savable
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
import io.antarescircuit.jabbah.graph.*
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolderImpl
import io.antarescircuit.jabbah.graph.container.ContainerDrawing
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.ui.GraphPanelViewMockBuilder
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class GraphPanelViewControllerTest {

	private val eventBus = EventBusImpl()
	private val systemSpeed = SystemSpeed(eventBus = eventBus)
	private val currentSystemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed, eventBus)
	private val scheduler: SchedulerImpl
	private val graphViewBuilder: GraphViewBuilder<Boolean>
	private val applicationContextHolder: GraphApplicationContextHolder
	private val drawingView: DrawingViewImpl<Drawing<Component>>
	private val editor: Editor
	private val applicationModeHolder: ApplicationModeHolderImpl
	private val controller: GraphPanelViewController

	init {
		GraphViewTestRule.configure()
		scheduler = SchedulerImpl(currentSystemSpeedCategory)
		graphViewBuilder = GraphViewBuilder<Boolean>()
		applicationContextHolder = GraphApplicationContextHolder(scheduler, eventBus, systemSpeed, currentSystemSpeedCategory)
		drawingView = DrawingViewImpl(graphViewBuilder.graphView as Drawing<Component>, applicationContextHolder = applicationContextHolder, eventBus = eventBus)
		editor = GraphEditorMockBuilder().withDrawingView(drawingView as DrawingView<GraphView>).build()
		applicationModeHolder = ApplicationModeHolderImpl(editor, scheduler).also {
			applicationContextHolder.applicationModeHolder = it
		}

		drawingView.canvas = CanvasMockBuilder().build()
		LibraryModule.libraryHolder.l = mock(MockMode.autofill)

		controller = GraphPanelViewController(editor, mock(MockMode.autofill), applicationContextHolder, applicationModeHolder, eventBus = eventBus)
		GraphPanelViewMockBuilder(controller)
	}

	@Test
	fun shouldSetApplicationData() {
		val content = GraphViewBuilder<Boolean>().build()
		val savable = mock<Savable>(MockMode.autofill)
		lateinit var event: EditedGraphViewEvent
		eventBus.register(EditedGraphViewEvent::class) { event = it }

		eventBus.post(ApplicationDataEvent(null, applicationDataFor(content, savable)))

		assertSame(content, controller.editViewController.editor.view.drawing as Drawing<GraphElementView<*>>)
		assertSame(content, event.newGraphView)
	}

	@Test
	fun shouldSetApplicationDataContent() {
		val content = GraphViewBuilder<Boolean>().build()
		val savable = mock<Savable>(MockMode.autofill)
		lateinit var event: EditedGraphViewEvent
		eventBus.register(EditedGraphViewEvent::class) { event = it }

		eventBus.post(ApplicationDataContentEvent(
			applicationDataFor(content, savable),
			graphViewBuilder.graphView))

		assertSame(content, controller.editViewController.editor.view.drawing as Drawing<GraphElementView<*>>)
		assertSame(content, event.newGraphView)
	}

	@Test
	fun shouldStopSimulationIfApplicationDataIsClosed() {
		val content = GraphViewBuilder<Boolean>().build()
		val savable = mock<Savable>(MockMode.autofill)
		eventBus.post(ApplicationDataEvent(null, applicationDataFor(content, savable)))
		controller.applicationModeHolder.setMode(ApplicationMode.EXECUTE)

		eventBus.post(ApplicationDataEvent(null, null))

		assertEquals(ApplicationMode.EDIT, controller.applicationModeHolder.currentMode)
	}

	private fun applicationDataFor(content: GraphView, savable: Savable): ApplicationData =
		ApplicationData(MetaGraph(GraphStorable(content), ContainerDrawing()), savable)
}