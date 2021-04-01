package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.container.OriginIndicator
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.StorableCloner
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

	@Test
	fun shouldNotCopyOriginIndicator() {
		val drawing = ContainerDrawing()
		val view = DrawingViewMockBuilder().withDrawing(drawing)
		val rect = RectangleComponent()
		val indicator = OriginIndicator()
		drawing.add(rect).add(indicator)

		val contents = service.copy(listOf(rect.id, indicator.id), drawing)
		service.paste(contents, view.build(), Point2D(10, 10))

		assertEquals(3, drawing.drawablesCount)
	}

	@Test
	fun shouldCopyPastePartOfNetViews() {
		val builder = GraphViewBuilder<Boolean>()
		val view = DrawingViewMockBuilder().withDrawing(builder.graphView)

		val v1 = builder.addVerticeView(TestVerticeView(loc = Point2D(100, 100)))
		val v2 = builder.addVerticeView(TestVerticeView(loc = Point2D(200, 100)))
		val v3 = builder.addVerticeView(TestVerticeView(loc = Point2D(200, 200)))
		val origEdgeView = builder.connect(v1, v2)
		val splitResult = builder.split(origEdgeView, 0, Point2D(150, 100), v3)

		val contents = service.copy(listOf(v3.id, splitResult.newEdgeView.id, splitResult.nodeView.id), builder.graphView)
		service.paste(contents, view.build(), Point2D(10, 10))

		assertEquals(7 + 2, builder.graphView.drawablesCount)

		// Check if resulting GraphView can be read back after being serialized (check for dangling references)
		StorableCloner.clone(GraphStorable(builder.graphView))
	}
}