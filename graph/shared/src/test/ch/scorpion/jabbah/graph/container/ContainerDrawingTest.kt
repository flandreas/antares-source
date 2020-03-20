package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.graph.view.CompositeTestGraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.Test
import kotlin.test.assertEquals

class ContainerDrawingTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@Test
	fun shouldCreateSubGraphVerticeView() {
		val builder = CompositeTestGraphViewBuilder("Hello")
		val metaGraph = builder.buildMetaGraph(builder.buildInnerCustomComponent())

		val vv = metaGraph.containerDrawing.createSubGraphVerticeView()

		assertEquals("Hello", vv.type)
	}
}