package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView

/**
 * A test base class using a [GraphViewBuilder] integrated with an [Editor] and its
 * [CommandManager] to support binding [UndoableDataHolder].
 * In addition, it proved convenient methods for simulation user input with mouse or keyboard.
 */
abstract class AbstractInputEventHandlerTest(
	protected val handler: InputEventHandler<EditInputEventContext>
) {

	companion object {
		const val WIDTH = 20
	}

	protected val builder: GraphViewBuilder<Boolean> = GraphViewBuilder()
	protected val _view = DrawingViewMockBuilder()
	protected val _editor = EditEditorModule.createEditor(_view.build())
	protected val draggedEdgeView get() = builder.graphView.getEdgeViews().first()

	protected val v1 = builder.addVerticeView(createEastOutputVerticeView("v1", 100, 100))
	protected val v2 = builder.addVerticeView(createEastOutputVerticeView("v2", 200, 100))

	protected val editor: Editor get() {
		// The GraphView in the GraphViewBuilder might have changed by CommandManager
		_view.withDrawing(builder.graphView)
		return _editor
	}

	protected val view get() = _view.build<Component>()

	init {
		_editor.commandManager.bindDataHolder(builder)
	}

	protected open fun mouseMoveTo(x: Int, y: Int, modifiers: Int = 0) {
		val context = context(MouseEventType.MOVED, x, y, modifiers)
		builder.graphView.getInputEventHandler(context).mouseMoved(context)
	}

	protected fun pressMouseAt(x: Int, y: Int, modifiers: Int = 0) {
		handler.mousePressed(context(MouseEventType.PRESSED, x, y, modifiers))
	}

	protected fun clickMouseAt(x: Int, y: Int, modifiers: Int = 0) {
		handler.mouseClicked(context(MouseEventType.CLICKED, x, y, modifiers))

	}

	protected fun doubleClickMouseAt(x: Int, y: Int, modifiers: Int = 0) {
		handler.mouseClicked(context(MouseEventType.CLICKED, x, y, modifiers, clickCount = 2))
	}

	protected fun dragMouseTo(x: Int, y: Int) {
		handler.mouseDragged(context(MouseEventType.DRAGGED, x, y))
	}

	protected fun releaseMouseAt(x: Int, y: Int) {
		handler.mouseReleased(context(MouseEventType.RELEASED, x, y))
	}

	protected fun pressKey(keyCode: Int) {
		handler.keyPressed(context(KeyEventType.PRESSED, keyCode))
	}

	protected fun pressEscape() {
		pressKey(KeyEvent.VK_ESCAPE)
	}

	protected fun pressAlt() {
		pressKey(KeyEvent.VK_ALT)
	}

	protected fun context(type: MouseEventType, x: Int, y: Int, modifiers: Int = 0, clickCount: Int = 1): EditInputEventContext {
		return EditInputEventContext(
			editor = editor,
			mouseEvent = MouseEventImpl(type, x = x, y = y, modifiers = modifiers, clickCount = clickCount),
			x = x.toDouble(),
			y = y.toDouble())
	}

	protected fun context(type: KeyEventType, keyCode: Int): EditInputEventContext {
		return EditInputEventContext(editor, keyEvent = KeyEventImpl(type, key = keyCode, keyChar = ' '))
	}

	protected fun createEastOutputVerticeView(name: String, x: Int, y: Int): TestVerticeView {
		return TestVerticeView(name = name, loc = Point2D(x, y), inputDirection = Direction.WEST, outputDirection = Direction.EAST, width = WIDTH)
	}
}