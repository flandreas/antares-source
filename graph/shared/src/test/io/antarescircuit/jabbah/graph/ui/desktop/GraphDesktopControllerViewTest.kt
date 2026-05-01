package io.antarescircuit.jabbah.graph.ui.desktop

import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.CanvasMockBuilder
import io.antarescircuit.jabbah.draw.drawable.Colorable
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.view.DrawingViewImpl
import io.antarescircuit.jabbah.execution.scheduler.SchedulerImpl
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryImpl
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.library.MemoryLibraryPersistenceService
import io.antarescircuit.jabbah.graph.model.vertice.SubGraphVerticeRef
import io.antarescircuit.jabbah.graph.ui.GraphDesktopViewItemMockBuilder
import io.antarescircuit.jabbah.graph.ui.GraphDesktopViewMockBuilder
import io.antarescircuit.jabbah.graph.ui.graphpanel.EditedGraphViewEvent
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import io.antarescircuit.jabbah.graph.view.vertice.OpenSubGraphRequest
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GraphDesktopControllerViewTest {

	private val eventBus = EventBusImpl()
	private val systemSpeed = SystemSpeed(eventBus = eventBus)
	private val currentSystemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed, eventBus)
	private val applicationContextHolder: GraphApplicationContextHolder
	private val graphViewBuilder: GraphViewBuilder<Boolean>
	private val drawingView: DrawingViewImpl<GraphElementView<*>, GraphView>
	private val controller: GraphDesktopViewController
	private val vv: SubGraphVerticeView<*>
	private val viewItemMock: GraphDesktopViewItemMockBuilder
	private val viewMock: GraphDesktopViewMockBuilder

	init {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
		applicationContextHolder = GraphApplicationContextHolder(SchedulerImpl(currentSystemSpeedCategory), eventBus, systemSpeed, currentSystemSpeedCategory)
		graphViewBuilder = GraphViewBuilder()
		drawingView = DrawingViewImpl(graphViewBuilder.graphView, applicationContextHolder = applicationContextHolder, eventBus = eventBus)
		controller = GraphDesktopViewController(applicationContextHolder, eventBus = eventBus)
		vv = createSubGraphVerticeView()
		viewItemMock = GraphDesktopViewItemMockBuilder()
			.withDrawingView(drawingView as DrawingView<GraphElementView<*>, GraphView>)
			.withFindElementWithRef(vv)
		viewMock = GraphDesktopViewMockBuilder(controller)
			.withMainViewItem(viewItemMock.build())

		drawingView.canvas = CanvasMockBuilder().withView(drawingView).build()
		graphViewBuilder.addVerticeView(vv)
		viewItemMock.withElementRef(GraphDesktopViewItemElementDepthRef(vv.id, 0))
	}

	@Test
	fun shouldOpenSubGraphInNewDesktopItem() {
		openSubGraph()
		assertEquals(1, controller.additionalDesktopItems.size)
	}

	@Test
	fun shouldAssociateWithReferenceColor() {
		viewMock.withCreatedSubGraphDesktopItem(GraphDesktopViewItemMockBuilder().build())
		openSubGraph()
		assertEquals(1, drawingView.content.highlighter.highlightCount)
		assertEquals(viewMock.referenceColor, (drawingView.content.highlighter.getHighlightFor(vv) as Colorable).color)
	}

	@Test
	fun shouldCloseSubGraphOnRequest() {
		openSubGraph()
		closeSubGraph()
		assertEquals(0, controller.additionalDesktopItems.size)
	}

	@Test
	fun shouldCloseSubGraphOnDeleteVerticeView() {
		viewMock.withCreatedSubGraphDesktopItem(viewItemMock.build())
		viewItemMock.withFindContent(drawingView.content)
		openSubGraph()
		graphViewBuilder.graphView.remove(vv)
		assertEquals(0, controller.additionalDesktopItems.size)
	}

	@Test
	fun shouldAlsoCloseSubGraphWhenClosingMainGraph() {
		viewMock.withCreatedSubGraphDesktopItem(viewItemMock.build())
		viewItemMock.withFindContent(drawingView.content)
		openSubGraph()

		closeMainGraph()

		assertNull(controller.mainDesktopViewItem)
		assertEquals(0, controller.additionalDesktopItems.size)
	}

	private fun openSubGraph() {
		eventBus.post(EditedGraphViewEvent(oldGraphView = null, newGraphView = graphViewBuilder.graphView))
		eventBus.post(OpenSubGraphRequest(vv, newView = true, quickMode = true))
	}

	private fun closeSubGraph() {
		val item = controller.additionalDesktopItems.first()
		viewItemMock.withFindContent(item.drawingView!!.content)
		eventBus.post(GraphDesktopViewItemCloseRequest(item, false))
	}

	private fun closeMainGraph() {
		eventBus.post(GraphDesktopViewItemCloseRequest(controller.mainDesktopViewItem!!, true))
	}

	private fun createSubGraphVerticeView(): SubGraphVerticeView<*> {
		val library = LibraryModule.libraryHolder.library
		TestLibraryBuilder().addInnerCustomComponent(library)
		return (library.get(TestLibraryBuilder.INNER_CUSTOM_COMP) as LibraryElement)
			.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<*>
	}
}