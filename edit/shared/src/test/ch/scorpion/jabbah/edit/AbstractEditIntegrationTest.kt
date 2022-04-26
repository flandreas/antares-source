package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.GenericUndoableDataHolder
import ch.scorpion.jabbah.edit.module.EditModule
import io.mockk.every
import io.mockk.mockk

abstract class AbstractEditIntegrationTest {

	companion object {
		init {
			EditModule.reset()
			EditTestRule.configure()
		}
	}

	private val drawing = DrawingImpl<Component>()
	protected val view = EditModule.drawingViewFactory.create(drawing, contextHolder = null, displayGlobalMessages = true)
	private val canvas = createCanvas()
	protected val editor = EditorImpl(view)

	init {
		GenericUndoableDataHolder(drawing, editor.commandManager) {
			view.setDrawing(it as Drawing<Component>, applyDefaultZoomStrategy = false)
		}
		view.canvas = canvas
	}

	private fun createCanvas(): Canvas {
		val canvas: Canvas = mockk(relaxed = true)
		every { canvas.view } returns view
		every { canvas.dimension } returns Dimension2D(100, 100)
		return canvas
	}
}