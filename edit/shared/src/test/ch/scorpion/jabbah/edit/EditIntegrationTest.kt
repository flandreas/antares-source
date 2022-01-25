package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.GenericUndoableDataHolder
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.module.EditModule
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class EditIntegrationTest {

	companion object {
		init {
			EditModule.reset()
			EditTestRule.configure()
		}
	}

	private val drawing = DrawingImpl<Component>()
	private val view = EditModule.drawingViewFactory.create(drawing, contextHolder = null, displayGlobalMessages = true)
	private val canvas = createCanvas()
	private val editor = EditorImpl(view)

	init {
		GenericUndoableDataHolder(drawing, editor.commandManager) {
			view.setDrawing(it as Drawing<Component>, applyDefaultZoomStrategy = false)
		}
		view.canvas = canvas
	}

	@Test
	fun shouldNotChangeZoomPanOnUndo() {
		view.navigator.panBy(20, 30)

		// Use "delete" because DeleteCommand is NOT Undoable and leads to setting new content in the DrawingView
		// when replaying the snapshot upon "Undo"
		val component = EditModule.drawingAppService.add(RectangleComponent(shape = Rectangle2D(0, 0, 200, 100)), view)
		EditModule.drawingAppService.delete(listOf(component), view)

		val origTransform = view.transformation.copy()
		editor.commandManager.undo()

		assertEquals(origTransform.affineTransform, view.transformation.affineTransform)
	}

	private fun createCanvas(): Canvas {
		val canvas: Canvas = mockk(relaxed = true)
		every { canvas.view } returns view
		every { canvas.dimension } returns Dimension2D(100, 100)
		return canvas
	}
}