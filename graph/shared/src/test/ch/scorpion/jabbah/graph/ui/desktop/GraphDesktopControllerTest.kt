package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.base.event.EventBusImpl
import ch.scorpion.jabbah.draw.drawable.Colorable
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.ui.GraphDesktopViewController
import ch.scorpion.jabbah.graph.ui.GraphDesktopViewItemCloseRequest
import ch.scorpion.jabbah.graph.ui.graphpanel.EditedGraphViewEvent
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.OpenSubGraphRequest
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphDesktopControllerTest {

	companion object {
		init {
			GraphViewTestRule.configure()

			LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
			LibraryModule.libraryService = LibraryService()
			LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
		}
	}

	private val eventBus = EventBusImpl()
	private val graphViewBuilder = GraphViewBuilder<Boolean>()
	private val drawingView = DrawingViewImpl(graphViewBuilder.graphView as Drawing<Component>, eventBus = eventBus)
	private val controller = GraphDesktopViewController(eventBus = eventBus)
	private val vv = createSubGraphVerticeView()
	private val viewItemMock = GraphDesktopViewItemMockBuilder()
		.withDrawingView(drawingView as DrawingView<GraphView>)
	private val viewMock = GraphDesktopViewMockBuilder(controller)
		.withMainViewItem(viewItemMock.build())

	init {
		drawingView.canvas = mockk(relaxed = true)
		graphViewBuilder.addVerticeView(vv)
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
		eventBus.post(GraphDesktopViewItemCloseRequest(controller.additionalDesktopItems.first(), false))
		assertEquals(0, controller.additionalDesktopItems.size)
	}

	@Test
	fun shouldCloseSubGraphOnDeleteVerticeView() {
		viewMock.withCreatedSubGraphDesktopItem(GraphDesktopViewItemMockBuilder().build())
		viewItemMock.withFindContent(drawingView.content as DrawingViewContent<GraphView>)
		openSubGraph()
		graphViewBuilder.graphView.remove(vv)
		assertEquals(0, controller.additionalDesktopItems.size)
	}

	private fun openSubGraph() {
		eventBus.post(EditedGraphViewEvent(oldGraphView = null, newGraphView = graphViewBuilder.graphView))
		eventBus.post(OpenSubGraphRequest(vv, newView = true, quickMode = true))
	}

	private fun createSubGraphVerticeView(): SubGraphVerticeView<*> {
		val library = LibraryModule.libraryHolder.library
		TestLibraryBuilder().addInnerCustomComponent(library)
		return (library.get(TestLibraryBuilder.INNER_CUSTOM_COMP) as LibraryElement)
			.getNewInstance<SubGraphVerticeRef>() as SubGraphVerticeView<*>
	}
}