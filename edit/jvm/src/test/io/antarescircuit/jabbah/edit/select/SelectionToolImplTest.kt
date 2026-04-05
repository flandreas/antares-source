package io.antarescircuit.jabbah.edit.select

import io.antarescircuit.jabbah.base.event.Modifier
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.view.CanvasJvm
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.edit.editor.EditorImpl
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.ToolTestUtil
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectionToolImplTest {

	private val canvas: CanvasJvm
	private val editor: EditorImpl
	private val toolUtil: ToolTestUtil
	private val rect1: RectangleComponent
	private val rect2: RectangleComponent

	init {
		EditTestRule.configure()
		canvas = CanvasJvm(EditModule.drawingViewFactory.create(DrawingImpl(), null, false, ""))
		editor = EditorImpl(canvas.view as DrawingView<Drawing<Component>>)
		toolUtil = ToolTestUtil(editor.selectionTool, editor)
		rect1 = RectangleComponent(shape = Rectangle2D(100, 100, 100, 100))
		rect2 = RectangleComponent(shape = Rectangle2D(300, 300, 100, 100))

		toolUtil.tool.activate()
		editor.drawing.add(rect1)
		editor.drawing.add(rect2)

		editor.selectionTool.rubberBandHandler.delaySelectTimer = null
	}

	@Test
	fun shouldSelectWithClickInsideRect() {
		toolUtil.pressMouseAt(150, 150)

		assertTrue(editor.view.selectionManager.isSelected(rect1))
		assertFalse(editor.view.selectionManager.isSelected(rect2))
	}

	@Test
	fun shouldDeselectAllWithClickOutsideRect() {
		editor.view.selectionManager.select(rect1)
		editor.view.selectionManager.select(rect2)
		toolUtil.pressMouseAt(0, 0)

		assertEquals(0, editor.view.selectionManager.selectionCount)
	}

	@Test
	fun shouldChangeSelectionWithPress() {
		toolUtil.pressMouseAt(150, 150)
		toolUtil.pressMouseAt(350, 350)

		assertEquals(1, editor.view.selectionManager.selectionCount)
		assertTrue(editor.view.selectionManager.isSelected(rect2))
	}

	@Test
	fun shouldInvertSelectionWithShiftPress() {
		toolUtil.pressMouseAt(150, 150)
		toolUtil.pressMouseAt(350, 350, Modifier.Shift.mask)
		toolUtil.pressMouseAt(350, 350, Modifier.Shift.mask)

		assertTrue(editor.view.selectionManager.isSelected(rect1))
		assertFalse(editor.view.selectionManager.isSelected(rect2))
	}

	@Test
	fun shouldExpandSelectionWithShiftPress() {
		toolUtil.pressMouseAt(150, 150)
		toolUtil.pressMouseAt(350, 350, Modifier.Shift.mask)

		assertEquals(2, editor.view.selectionManager.selectionCount)
	}

	@Test
	fun shouldMoveSingleComponent() {
		toolUtil.pressMouseAt(150, 150)
		toolUtil.dragMouseTo(200, 150)
		toolUtil.releaseMouseAt(200, 150)

		assertTrue(editor.view.selectionManager.isSelected(rect1))
		assertEquals(Point2D(150, 100), rect1.location)
	}

	@Test
	fun shouldMoveAllSelectedComponents() {
		editor.view.selectionManager.selectAll()
		toolUtil.pressMouseAt(150, 150)
		toolUtil.dragMouseTo(200, 150)
		toolUtil.releaseMouseAt(200, 150)

		assertEquals(2, editor.view.selectionManager.selectionCount)
		assertEquals(Point2D(150, 100), rect1.location)
		assertEquals(Point2D(350, 300), rect2.location)
	}

	@Test
	fun shouldSelectWithRubberband() {
		toolUtil.pressMouseAt(0, 0)
		toolUtil.dragMouseTo(500, 500)
		toolUtil.releaseMouseAt(500, 500)

		assertTrue(editor.view.selectionManager.isSelected(rect1))
		assertTrue(editor.view.selectionManager.isSelected(rect2))
	}

	@Test
	fun shouldExpandRubberbandSelectionWithShiftPress() {
		editor.view.selectionManager.select(rect1)
		toolUtil.pressMouseAt(290, 290, Modifier.Shift.mask)
		toolUtil.dragMouseTo(410, 410)
		toolUtil.releaseMouseAt(410, 410)

		assertTrue(editor.view.selectionManager.isSelected(rect1))
		assertTrue(editor.view.selectionManager.isSelected(rect2))
	}
}