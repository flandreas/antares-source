package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.graphics.PredefinedColorIdentity
import ch.scorpion.jabbah.draw.graphics.PredefinedColorRepository
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphViewCopyPasteServiceTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val service = GraphViewCopyPasteService()

	@BeforeTest
	fun setup() {
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryService = LibraryService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
	}

	@Test
	fun shouldCopyPasteInGraphView() {
		val builder = GraphViewBuilder<Boolean>()
		val view = DrawingViewMockBuilder().withDrawing(builder.graphView)
		val vv1 = TestVerticeView()
		val vv2 = TestVerticeView()
		builder.addVerticeView(vv1)
		builder.addVerticeView(vv2)

		val contents = service.copy(listOf(vv2.id), builder.graphView)
		service.paste(contents, view.build(), Point2D(10, 10))

		assertEquals(3, builder.graphView.drawables.size)
	}

	@Test
	fun shouldCopyPasteInContainerDrawing() {
		val drawing = ContainerDrawing()
		val view = DrawingViewMockBuilder().withDrawing(drawing)
		val comp1 = RectangleComponent()
		val comp2 = RectangleComponent()
		drawing.add(comp1)
		drawing.add(comp2)

		val contents = service.copy(listOf(comp2.id), drawing)
		service.paste(contents, view.build(), Point2D(10, 10))

		assertEquals(3, drawing.drawables.size)
	}

	@Test
	fun shouldCopyPasteLabelComponent() {
		val drawing = ContainerDrawing()
		val view = DrawingViewMockBuilder().withDrawing(drawing)
		val comp1 = RectangleComponent()
		val label = LabelComponent("bla")
		drawing.add(comp1).add(label)

		val contents = service.copy(listOf(label.id), drawing)
		service.paste(contents, view.build(), Point2D(10, 10))

		assertEquals(3, drawing.drawables.size)
		assertEquals("bla", (drawing.get(0) as LabelComponent).text.getTranslation())
	}

	@Test
	fun shouldNotCopyOriginIndicator() {
		val drawing = ContainerDrawing()
		val view = DrawingViewMockBuilder().withDrawing(drawing)
		val rect = RectangleComponent()
		val indicator = OriginIndicator()
		drawing.add(rect).add(indicator)

		val contents = service.copy(listOf(rect.id, indicator.id), drawing)
		service.paste(contents, view.build(), Point2D(10, 10))

		assertEquals(3, drawing.drawables.size)
	}

	@Test
	fun shouldCopyPasteNetViewProperties() {
		val builder = GraphViewBuilder<Boolean>()
		val view = DrawingViewMockBuilder().withDrawing(builder.graphView)
		val vv1 = TestVerticeView()
		val vv2 = TestVerticeView()
		builder.addVerticeView(vv1)
		builder.addVerticeView(vv2)
		val ev = builder.connect(vv1, vv2)
		ev.netView!!.style = NetViewStyle.BLOCK
		ev.netView!!.customColor = PredefinedColorRepository.withIdentity(PredefinedColorIdentity.Blue)

		val contents = service.copy(listOf(ev.id), builder.graphView)
		service.paste(contents, view.build(), Point2D(10, 10))

		assertEquals(2, builder.graphView.netViewsCount)

		val newEv = builder.graphView.getEdgeViews().first()
		assertEquals(NetViewStyle.BLOCK, newEv.netView!!.style)
		assertEquals(PredefinedColorRepository.withIdentity(PredefinedColorIdentity.Blue), newEv.netView!!.customColor)
	}

	/** Regression test for GitHub bug #426. */
	@Test
	fun shouldCopyPasteSubGraphVerticeViewWithEditedSymbol() {
		val builder = GraphViewBuilder<Boolean>()
		val view = DrawingViewMockBuilder().withDrawing(builder.graphView)

		val libraryElement = createMetaGraph()
		val vv = libraryElement.getNewInstance<Vertice>() as SubGraphVerticeView
		builder.addVerticeView(vv)

		// Edit ContainerDrawing
		val containerDrawing = vv.getEditableContainerDrawing()
		containerDrawing.add(LabelComponent("Hello"))
		vv.setEditedContainerDrawing(containerDrawing)

		val contents = service.copy(listOf(vv.id), builder.graphView)

		// The bug #425 threw an IllegalArgumentException
		service.paste(contents, view.build(), Point2D(10, 10))
	}

	private fun createMetaGraph(): ContainerLibraryElement {
		val library = LibraryModule.libraryHolder.library
		val metaGraph = TestLibraryBuilder().addInnerCustomComponent(library)
		val libraryElement = library.getContainerLibraryElement(metaGraph.uuid)!!
		LibraryModule.libraryService.updateContainerLibraryElement(library, metaGraph, libraryElement)
		return libraryElement
	}
}