package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.drawable.Colorable
import ch.scorpion.jabbah.draw.CanvasMockBuilder
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.ui.GraphDesktopViewItemMockBuilder
import ch.scorpion.jabbah.graph.ui.GraphDesktopViewMockBuilder
import ch.scorpion.jabbah.graph.ui.graphpanel.EditedGraphViewEvent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GraphDesktopControllerViewTest {

	private val eventBus = EventBusImpl()
	private val systemSpeed = SystemSpeed(eventBus = eventBus)
	private val currentSystemSpeedCategory = CurrentSystemSpeedCategory(systemSpeed, eventBus)
	private val applicationContextHolder: GraphApplicationContextHolder
	private val graphViewBuilder: GraphViewBuilder<Boolean>
	private val drawingView: DrawingViewImpl<Drawing<Component>>
	private val controller: GraphDesktopViewController
	private val vv: SubGraphVerticeView<*>
	private val viewItemMock: GraphDesktopViewItemMockBuilder
	private val viewMock: GraphDesktopViewMockBuilder

	init {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
		applicationContextHolder = GraphApplicationContextHolder(SchedulerImpl(currentSystemSpeedCategory), eventBus, systemSpeed, currentSystemSpeedCategory)
		graphViewBuilder = GraphViewBuilder<Boolean>()
		drawingView = DrawingViewImpl(graphViewBuilder.graphView as Drawing<Component>, applicationContextHolder = applicationContextHolder, eventBus = eventBus)
		controller = GraphDesktopViewController(applicationContextHolder, eventBus = eventBus)
		vv = createSubGraphVerticeView()
		viewItemMock = GraphDesktopViewItemMockBuilder()
			.withDrawingView(drawingView as DrawingView<GraphView>)
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
		viewItemMock.withFindContent(drawingView.content as DrawingViewContent<GraphView>)
		openSubGraph()
		graphViewBuilder.graphView.remove(vv)
		assertEquals(0, controller.additionalDesktopItems.size)
	}

	@Test
	fun shouldAlsoCloseSubGraphWhenClosingMainGraph() {
		viewMock.withCreatedSubGraphDesktopItem(viewItemMock.build())
		viewItemMock.withFindContent(drawingView.content as DrawingViewContent<GraphView>)
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