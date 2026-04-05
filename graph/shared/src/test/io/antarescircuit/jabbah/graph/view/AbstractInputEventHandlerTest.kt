package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.event.MouseEventType
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.editor.EditEditorModule
import io.antarescircuit.jabbah.edit.editor.InputEventDriver
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.view.graph.GraphViewImpl
import io.antarescircuit.jabbah.graph.view.vertice.TestVerticeView

/**
 * A test base class using a [GraphViewBuilder] integrated with an [Editor] and its
 * [CommandManager] to support binding [UndoableDataHolder].
 */
abstract class AbstractInputEventHandlerTest(): InputEventDriver() {

	protected val builder: GraphViewBuilder<Boolean>

	protected val draggedEdgeView get() = builder.graphView.getEdgeViews().first()

	private val viewMock: DrawingViewMockBuilder

	protected val v1: TestVerticeView
	protected val v2: TestVerticeView

	override var editor: Editor
		set(value) {
			super.editor = value
		}
		get() {
			// The GraphView in the GraphViewBuilder might have changed by CommandManager
			viewMock.withDrawing(builder.graphView)
			return super.editor
		}

	protected val view get() = viewMock.build<Component>()

	init {
		GraphViewTestRule.configure()
		builder = GraphViewBuilder("Unknown", GenericGraphType)
		viewMock = DrawingViewMockBuilder()
		v1 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v1", 100, 100))
		v2 = builder.addVerticeView(TestVerticeView.createEastOutputVerticeView("v2", 200, 100))

		GraphViewImpl.inputEventHandler = null
		editor = EditEditorModule.createEditor("", viewMock.withDrawing(builder.build()).build())
		viewMock.withDrawing(builder.build())
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