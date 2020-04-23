package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.GraphUITestRule
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.TestGraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import ch.scorpion.jabbah.io.IOModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

/** Unit tests for [CopyPasteUtilitySwing]. */
class ClipboardIntegrationTest {

	companion object {
		init {
			GraphViewModuleJvm.require()
			GraphUITestRule.configure()
		}
	}

	private val service = EditModule.drawingAppService
	private val testGraphView = TestGraphView()

	@Test
	fun shouldCopyPaste() {
		val view: DrawingView<Drawing<Component>> = DrawingViewMockBuilder()
			.withDrawing(testGraphView.graphView)
			.withSelection(testGraphView.vv2)
			.build()

		service.copy(view)
		service.paste(view)

		assertEquals(4, testGraphView.graphView.drawablesCount)
		assertNotSame(testGraphView.vv2.model, getCopiedVerticeView(testGraphView).model)
	}

	fun getCopiedVerticeView(testGraphView: TestGraphView): VerticeView<*> {
		return testGraphView.graphView.get(testGraphView.graphView.drawablesCount - 1) as VerticeView<*>
	}
}