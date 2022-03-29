package ch.scorpion.jabbah.graph.view.oscilloscope

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OscilloscopeEditIntegrationTest {

	companion object {
		init {
			GraphViewTestRule.configure()
		}
	}

	private val viewMock: DrawingViewMockBuilder = DrawingViewMockBuilder()
	private val editor: Editor = EditEditorModule.createEditor(viewMock.build())
	private val builder = GraphViewBuilder<Boolean>()

	private val view: DrawingView<GraphView> get() {
		// The GraphView in the GraphViewBuilder might have changed by CommandManager
		viewMock.withDrawing(builder.graphView)
		return viewMock.build<Component>() as DrawingView<GraphView>
	}

	init {
		editor.commandManager.bindDataHolder(builder)
	}

	@BeforeTest
	fun setupCircuit() {

		val vv1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("VV1", 0, 0))
		val vv2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("VV2", 100, 0))
		val vv3 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("VV3", 200, 0))
		builder.connect(vv1, vv2)
		builder.connect(vv2, vv3)
	}

	@Test
	fun shouldCreateOscilloscopeView() {
		GraphViewModule.oscilloscopeViewService.displayOscilloscope(view)
		assertTrue(GraphViewModule.oscilloscopeViewService.isOscilloscopeDisplayed(view.drawing))
		assertTrue(EditModule.commandManager.canUndo())
	}

	@Test
	fun shouldUndoCreateOscilloscopeView() {
		GraphViewModule.oscilloscopeViewService.displayOscilloscope(view)
		EditModule.commandManager.undo()
		assertFalse(GraphViewModule.oscilloscopeViewService.isOscilloscopeDisplayed(view.drawing))
	}

	@Test
	fun shouldHideOscilloscopeView() {
		GraphViewModule.oscilloscopeViewService.displayOscilloscope(view)
		EditModule.commandManager.reset()
		GraphViewModule.oscilloscopeViewService.hideOscilloscope(view)
		assertFalse(GraphViewModule.oscilloscopeViewService.isOscilloscopeDisplayed(view.drawing))
		assertTrue(EditModule.commandManager.canUndo())
	}

	@Test
	fun shouldUndoHideOscilloscopeView() {
		GraphViewModule.oscilloscopeViewService.displayOscilloscope(view)
		GraphViewModule.oscilloscopeViewService.hideOscilloscope(view)
		EditModule.commandManager.undo()
		assertTrue(GraphViewModule.oscilloscopeViewService.isOscilloscopeDisplayed(view.drawing))
	}
}