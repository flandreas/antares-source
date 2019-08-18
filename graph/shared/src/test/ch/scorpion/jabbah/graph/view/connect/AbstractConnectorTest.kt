package ch.scorpion.jabbah.graph.view.connect

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.vertice.TestVerticeView
import io.mockk.every
import io.mockk.mockk

abstract class AbstractConnectorTest(
	private val connector: AbstractConnector
) {
	companion object {
		const val WIDTH = 20
	}

	protected val builder: GraphViewBuilder<Boolean> = GraphViewBuilder()
	protected val view = drawingViewMock()
	protected val editor = EditEditorModule.createEditor(view)
	protected val draggedEdgeView get() = builder.graphView.getEdgeViews().first()
	protected val v1 = builder.addVerticeView(createEastOutputVerticeView(100, 100))
	protected val v2 = builder.addVerticeView(createEastOutputVerticeView(200, 100))

	private fun drawingViewMock(): DrawingView<Drawing<Component>> {
		val view = mockk<DrawingView<Drawing<Component>>>(relaxed = true)
		every { view.drawing } returns builder.graphView as Drawing<Component>
		return view
	}

	protected fun mouseMoveTo(x: Int, y: Int, modifiers: Int = 0) {
		val context = context(MouseEventType.MOVED, x, y, modifiers)
		builder.graphView.getInputEventHandler(context).mouseMoved(context)
	}

	protected fun pressMouseAt(x: Int, y: Int) {
		connector.handler.mousePressed(context(MouseEventType.PRESSED, x, y))
	}

	protected fun dragMouseTo(x: Int, y: Int) {
		connector.handler.mouseDragged(context(MouseEventType.DRAGGED, x, y))
	}

	protected fun releaseMouseAt(x: Int, y: Int) {
		connector.handler.mouseReleased(context(MouseEventType.RELEASED, x, y))
	}

	protected fun cancelDrag() {
		connector.handler.keyPressed(context(KeyEventType.PRESSED, KeyEvent.VK_ESCAPE))
	}

	protected fun context(type: MouseEventType, x: Int, y: Int, modifiers: Int = 0): EditInputEventContext {
		return EditInputEventContext(editor, mouseEvent = MouseEventImpl(type, x, y, modifiers = modifiers), x = x.toDouble(), y = y.toDouble())
	}

	protected fun context(type: KeyEventType, keyCode: Int): EditInputEventContext {
		return EditInputEventContext(editor, keyEvent = KeyEventImpl(type, key = keyCode, keyChar = ' '))
	}

	protected fun createEastOutputVerticeView(x: Int, y: Int): TestVerticeView {
		return TestVerticeView(loc = Point2D(x, y), inputDirection = Direction.WEST, outputDirection = Direction.EAST, width = WIDTH)
	}
}