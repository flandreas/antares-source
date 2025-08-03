package ch.scorpion.jabbah.graph.ui.graphpanel

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.CanvasMockBuilder
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.*
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ApplicationModeHolderImpl
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.ui.GraphPanelViewMockBuilder
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class GraphPanelViewControllerTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val eventBus = EventBusImpl()
	private val systemSpeed = SystemSpeed(eventBus = eventBus)
	private val currentSystemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed, eventBus)
	private val scheduler = SchedulerImpl(currentSystemSpeedCategory)
	private val graphViewBuilder = GraphViewBuilder<Boolean>()
	private val applicationContextHolder = GraphApplicationContextHolder(scheduler, eventBus, systemSpeed, currentSystemSpeedCategory)
	private val drawingView = DrawingViewImpl(graphViewBuilder.graphView as Drawing<Component>, applicationContextHolder = applicationContextHolder, eventBus = eventBus)
	private val editor = GraphEditorMockBuilder().withDrawingView(drawingView as DrawingView<GraphView>).build()
	private val applicationModeHolder = ApplicationModeHolderImpl(editor, scheduler).also {
		applicationContextHolder.applicationModeHolder = it
	}
	private val controller: GraphPanelViewController

	init {
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