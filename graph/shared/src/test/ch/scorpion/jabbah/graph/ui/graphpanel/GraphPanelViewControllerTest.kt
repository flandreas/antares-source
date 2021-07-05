package ch.scorpion.jabbah.graph.ui.graphpanel

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.TestEditorBuilder
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.mockk
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
	private val graphViewBuilder = GraphViewBuilder<Boolean>()
	private val drawingView = DrawingViewImpl(graphViewBuilder.graphView as Drawing<Component>, eventBus = eventBus)
	private val editor = TestEditorBuilder().withDrawingView(drawingView as DrawingView<GraphView>).build()
	private val controller: GraphPanelViewController

	init {
		drawingView.canvas = mockk(relaxed = true)
		LibraryModule.libraryHolder.l = mockk(relaxed = true)
		ProjectModule.projectHolder.p = mockk(relaxed = true)

		controller = GraphPanelViewController(editor, mockk(relaxed = true), eventBus = eventBus)
		GraphViewModule.applicationModeHolder = controller
		GraphPanelViewMockBuilder(controller)
	}

	@Test
	fun shouldSetApplicationData() {
		val content = GraphViewBuilder<Boolean>().build()
		val savable = mockk<Savable>(relaxed = true)
		lateinit var event: EditedGraphViewEvent
		eventBus.register(EditedGraphViewEvent::class) { event = it }

		eventBus.post(ApplicationDataEvent(null, applicationDataFor(content, savable)))

		assertSame(content, controller.editViewController.editor.view.drawing as Drawing<GraphElementView<*>>)
		assertSame(content, event.newGraphView)
	}

	@Test
	fun shouldSetApplicationDataContent() {
		val content = GraphViewBuilder<Boolean>().build()
		val savable = mockk<Savable>(relaxed = true)
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
		val savable = mockk<Savable>(relaxed = true)
		eventBus.post(ApplicationDataEvent(null, applicationDataFor(content, savable)))
		GraphViewModule.applicationModeHolder.setMode(ApplicationMode.EXECUTE)

		eventBus.post(ApplicationDataEvent(null, null))

		assertEquals(ApplicationMode.EDIT, controller.currentMode)
	}

	private fun applicationDataFor(content: GraphView, savable: Savable): ApplicationData =
		ApplicationData(MetaGraph(GraphStorable(content), ContainerDrawing()), savable)
}