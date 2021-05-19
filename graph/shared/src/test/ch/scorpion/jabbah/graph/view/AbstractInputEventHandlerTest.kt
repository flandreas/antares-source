package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.MouseEventType
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.editor.InputEventDriver
import ch.scorpion.jabbah.graph.DrawingViewMockBuilder
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView

/**
 * A test base class using a [GraphViewBuilder] integrated with an [Editor] and its
 * [CommandManager] to support binding [UndoableDataHolder].
 */
abstract class AbstractInputEventHandlerTest(
	handler: InputEventHandler<EditInputEventContext>,
	private val viewMock: DrawingViewMockBuilder = DrawingViewMockBuilder()
): InputEventDriver(
	EditEditorModule.createEditor(viewMock.build()),
	handler
) {

	companion object {
		const val WIDTH = 20
	}

	protected val builder: GraphViewBuilder<Boolean> = GraphViewBuilder()
	protected val draggedEdgeView get() = builder.graphView.getEdgeViews().first()

	protected val v1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v1", 100, 100))
	protected val v2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v2", 200, 100))

	override val editor: Editor get() {
		// The GraphView in the GraphViewBuilder might have changed by CommandManager
		viewMock.withDrawing(builder.graphView)
		return super.editor
	}

	protected val view get() = viewMock.build<Component>()

	init {
		GraphViewImpl.inputEventHandler = null
		editor.commandManager.bindDataHolder(builder)
	}

	private var target: InputEventHandler<EditInputEventContext>? = null

	override fun mouseMoveTo(x: Int, y: Int, modifiers: Int): InputEventDriver {
		val context = context(MouseEventType.MOVED, x, y, modifiers)

		target = if (target != null) {
			target!!.mouseMoved(context)
		} else {
			builder.graphView.getInputEventHandler(context).mouseMoved(context)
		}

		return this
	}
}