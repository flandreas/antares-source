package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

/** Unit tests for [CopyPasteUtilitySwing]. */
class CopyPasteUtilitySwingTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	@BeforeTest
	fun setup() {
		IOModule.typeMap.register("testVertice", TestVertice::class)
		IOModule.typeMap.register("testVerticeView", TestVerticeView::class)
	}

	@Test
	fun shouldCopyPaste() {
		val testGraphView = copyPaste()
		assertEquals(4, testGraphView.graphView.drawablesCount)
	}

	@Test
	fun copyShouldHaveDifferentModel() {
		val testGraphView = copyPaste()
		assertNotSame(testGraphView.vv2.model, getCopiedVerticeView(testGraphView).model)
	}

	/** Creates a [TestGraphView] and performs a copy/paste for its [TestVerticeView] vv2.*/
	private fun copyPaste(): TestGraphView {
		val testGraphView = TestGraphView()
		CopyPasteUtilitySwing.copy(testGraphView.graphView, listOf(testGraphView.vv2))
		CopyPasteUtilitySwing.paste(DrawingViewMockBuilder().withDrawing(testGraphView.graphView).build())
		return testGraphView
	}

	fun getCopiedVerticeView(testGraphView: TestGraphView): VerticeView<*> {
		return testGraphView.graphView.get(testGraphView.graphView.drawablesCount - 1) as VerticeView<*>
	}
}