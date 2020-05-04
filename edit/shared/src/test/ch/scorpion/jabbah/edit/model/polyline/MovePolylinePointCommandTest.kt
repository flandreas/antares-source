package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.DrawingImpl
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit test for [MovePolylinePointCommand].*/
class MovePolylinePointCommandTest {

	companion object {
		init {
			EditTestRule.configure()
		}
	}

	private val drawing = DrawingImpl<Component>()
	private val editor = mockk<Editor>()

	init {
		every { editor.drawing } returns drawing
	}

	@Test
	fun shouldExecuteMovePoint() {
		val component = PolylineComponent()
		component.addPoint(0, 0)
			.addPoint(100, 0)
			.addPoint(100, 100)
			.addPoint(0, 100)
		drawing.add(component)
		val cmd = MovePolylinePointCommand(editor, component.id, 0, Point2D(50, 0))

		cmd.execute()

		assertEquals(Point2D(50, 0), component.getPointAt(0))
	}

	@Test
	fun shouldUndoMovePoint() {
		val component = PolylineComponent()
		component.addPoint(0, 0)
			.addPoint(100, 0)
			.addPoint(100, 100)
			.addPoint(0, 100)
		drawing.add(component)
		val cmd = MovePolylinePointCommand(editor, component.id, 0, Point2D(50, 0))
		cmd.execute()

		cmd.undo()

		assertEquals(Point2D(0, 0), component.getPointAt(0))
	}
}