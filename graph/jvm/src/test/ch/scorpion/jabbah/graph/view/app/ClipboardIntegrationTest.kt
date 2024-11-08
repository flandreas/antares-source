package ch.scorpion.jabbah.graph.view.app

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import ch.scorpion.jabbah.graph.view.graph.GraphViewCopyPasteService
import ch.scorpion.jabbah.graph.view.module.GraphViewModuleJvm
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for clipboard methods of [GraphViewAppService]. */
class ClipboardIntegrationTest {

	companion object {
		init {
			GraphViewModuleJvm.require()
			GraphViewTestRule.configure()
		}
	}

	// Since GraphViewCopyPasteService is stateful, make sure that its instance is under control
	private val commandManager = SourcingCommandManager()
	private val cpService = GraphViewCopyPasteService()
	private val service = GraphViewAppServiceImpl(cpService, commandManager)

	private val builder: GraphViewBuilder<Boolean> = GraphViewBuilder()
	private val _view = DrawingViewMockBuilder().withDrawing(builder.build()).withSize(800, 600)
	private val _editor = EditorImpl(view, commandManager, EditSelectModule.selectionToolFactory)

	private val v1 = builder.addVerticeView(createEastOutputVerticeView("v1", 100, 100))
	private val v2 = builder.addVerticeView(createEastOutputVerticeView("v2", 200, 100))
	private val ev = builder.connect(v1, v2)

	private val view get() = _view.build<Component>()

	private val editor: Editor
		get() {
			// The GraphView in the GraphViewBuilder might have changed by CommandManager
			_view.withDrawing(builder.graphView)
		return _editor
	}

	private fun createEastOutputVerticeView(name: String, x: Int, y: Int): TestVerticeView {
		return TestVerticeView(name = name, loc = Point2D(x, y), inputDirection = Direction.WEST, outputDirection = Direction.EAST, width = 20)
	}

	init {
		_view.withModelToView(Rectangle2D(0, 0, 800, 600))
		_editor.commandManager.bindDataHolder(builder)
		editor.commandManager.reset()
		cpService.reset()
	}

	@Test
	fun shouldCopyPasteAll() {
		_view.withSelection(v1, v2, ev)
		service.copy(view)
		service.paste(view)

		assertEquals(6, editor.drawing.drawables.size)
	}

	@Test
	fun shouldPlacePastedComponentRightBelowCopiedComponent() {
		_view.withSelection(v1)
		service.copy(view)
		service.paste(view)

		assertEquals(Point2D(100 + 3 * 10, 100 + 3 * 10), builder.graphView.get(0).location)
	}

	@Test
	fun shouldAdjustMorePastesToPlacedFirstPaste() {
		_view.withSelection(v1)
		service.copy(view)
		service.paste(view)

		// Establish vertical distance of 50
		val pastedComponent = view.drawing.get(0)
		pastedComponent.location = Point2D(100, 150)

		service.paste(view)
		assertEquals(Point2D(100, 200), builder.graphView.get(0).location)

		service.paste(view)
		assertEquals(Point2D(100, 250), builder.graphView.get(0).location)

		service.paste(view)
		assertEquals(Point2D(100, 300), builder.graphView.get(0).location)

		service.paste(view)
		assertEquals(Point2D(100, 350), builder.graphView.get(0).location)

		service.paste(view)
		assertEquals(Point2D(100, 400), builder.graphView.get(0).location)
	}

	@Test
	fun shouldRespectDeletedFirstPasteInConsecutivePasts() {
		_view.withSelection(v1)
		service.copy(view)
		service.paste(view)

		service.delete(listOf(view.drawing.get(0)), view)
		service.paste(view)

		assertEquals(Point2D(100 + 3 * 10, 100 + 3 * 10), builder.graphView.get(0).location)
	}

	@Test
	fun shouldRedoAdjustments() {
		_view.withSelection(v1)
		service.copy(view)
		service.paste(view)

		// Establish vertical distance of 50
		val pastedComponent = view.drawing.get(0)
		pastedComponent.location = Point2D(100, 150)

		service.paste(view)
		assertEquals(Point2D(100, 200), builder.graphView.get(0).location)
		service.paste(view)
		assertEquals(Point2D(100, 250), builder.graphView.get(0).location)
		service.paste(view)
		assertEquals(Point2D(100, 300), builder.graphView.get(0).location)


		editor.commandManager.undo()
		editor.commandManager.undo()

		editor.commandManager.redo()

		assertEquals(Point2D(100, 250), builder.graphView.get(0).location)
	}

	@Test
	fun shouldAdjustInPasteAfterUndo() {
		_view.withSelection(v1)
		service.copy(view)
		service.paste(view)

		// Establish vertical distance of 50
		val pastedComponent = view.drawing.get(0)
		pastedComponent.location = Point2D(100, 150)

		service.paste(view)
		assertEquals(Point2D(100, 200), builder.graphView.get(0).location)
		service.paste(view)
		assertEquals(Point2D(100, 250), builder.graphView.get(0).location)
		service.paste(view)
		assertEquals(Point2D(100, 300), builder.graphView.get(0).location)

		editor.commandManager.undo()
		editor.commandManager.undo()

		service.paste(view)

		assertEquals(Point2D(100, 250), builder.graphView.get(0).location)
	}

	@Test
	fun shouldUndoPaste() {
		_view.withSelection(v1)
		service.copy(view)
		service.paste(view)

		editor.commandManager.undo()

		assertEquals(3, editor.drawing.drawables.size)
	}

	@Test
	fun shouldRedoPaste() {
		_view.withSelection(v1)
		service.copy(view)
		service.paste(view)

		editor.commandManager.undo()
		editor.commandManager.redo()

		assertEquals(4, editor.drawing.drawables.size)
		assertEquals(Point2D(100 + 3 * 10, 100 + 3 * 10), builder.graphView.get(0).location)
	}

	@Test
	fun shouldUndoRedo() {
		_view.withSelection(v1)
		service.copy(view)
		service.paste(view)

		editor.commandManager.undo()
		editor.commandManager.redo()
		editor.commandManager.undo()

		assertEquals(3, editor.drawing.drawables.size)
	}
}