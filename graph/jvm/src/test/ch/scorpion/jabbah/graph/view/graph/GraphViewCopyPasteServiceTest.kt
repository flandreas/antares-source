package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphViewCopyPasteServiceTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val service = GraphViewCopyPasteService()

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

		assertEquals(3, builder.graphView.drawablesCount)
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

		assertEquals(3, drawing.drawablesCount)
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

		assertEquals(3, drawing.drawablesCount)
		assertEquals("bla", (drawing.get(0) as LabelComponent).text.getTranslation())
	}
}