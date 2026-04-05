package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.Canvas
import io.antarescircuit.jabbah.edit.editor.EditorImpl
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.model.GenericUndoableDataHolder
import io.antarescircuit.jabbah.edit.module.EditModule
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.BeforeTest

abstract class AbstractEditIntegrationTest {

	private lateinit var drawing: DrawingImpl<Component>
	protected lateinit var view: DrawingView<Drawing<Component>>
	private lateinit var canvas: Canvas
	protected lateinit var editor: Editor
	protected lateinit var driver: EditorToolDriver

	@BeforeTest
	open fun setup() {
		EditTestRule.configure(SelectionModelMockFactory())
		createEnvironment()
	}

	protected open fun createEnvironment() {
		drawing = DrawingImpl()
		view = EditModule.drawingViewFactory.create(drawing, contextHolder = null, displayGlobalMessages = true, "")
		canvas = createCanvas()
		editor = EditorImpl(view)
		driver = EditorToolDriver(editor)

		GenericUndoableDataHolder(drawing, editor.commandManager) {
			view.setDrawing(it as Drawing<Component>, applyDefaultZoomStrategy = false)
		}
		view.canvas = canvas
	}

	private fun createCanvas(): Canvas {
		val canvas: Canvas = mock(MockMode.autofill)
		every { canvas.view } returns view
		every { canvas.dimension } returns Dimension2D(100, 100)
		every { canvas.mouseLocation } returns Point2D.ZERO
		return canvas
	}
}