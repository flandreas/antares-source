package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.event.SHIFT_MASK
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.CanvasJvm
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.editor.EditorImpl
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.tool.ToolTestUtil
import kotlin.test.*

class SelectionToolImplTest {

	companion object {
		init {
			EditTestRule.configure()
		}
	}

	private val canvas = CanvasJvm(EditModule.drawingViewFactory.invoke(DrawingImpl()))
	private val editor = EditorImpl(canvas.view as DrawingView<Drawing<Component>>)
	private val toolUtil = ToolTestUtil(SelectionToolImpl(editor, RubberBandHandler(RectangularRubberBand()), BaseModule.eventBus), editor)
	private val rect1 = RectangleComponent(shape = Rectangle2D(100, 100, 100, 100))
	private val rect2 = RectangleComponent(shape = Rectangle2D(300, 300, 100, 100))

	init {
		toolUtil.tool.activate()
		editor.drawing.add(rect1)
		editor.drawing.add(rect2)
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
		toolUtil.pressMouseAt(350, 350, SHIFT_MASK)
		toolUtil.pressMouseAt(350, 350, SHIFT_MASK)

		assertTrue(editor.view.selectionManager.isSelected(rect1))
		assertFalse(editor.view.selectionManager.isSelected(rect2))
	}

	@Test
	fun shouldExpandSelectionWithShiftPress() {
		toolUtil.pressMouseAt(150, 150)
		toolUtil.pressMouseAt(350, 350, SHIFT_MASK)

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
		toolUtil.pressMouseAt(290, 290, SHIFT_MASK)
		toolUtil.dragMouseTo(410, 410)
		toolUtil.releaseMouseAt(410, 410)

		assertTrue(editor.view.selectionManager.isSelected(rect1))
		assertTrue(editor.view.selectionManager.isSelected(rect2))
	}
}