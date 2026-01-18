package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.DrawingImpl
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class MovePolylinePointCommandTest {

	private val drawing: DrawingImpl<Component>
	private val editor = mock<Editor>()

	init {
		EditTestRule.configure()
		drawing = DrawingImpl<Component>()
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
		val cmd = MovePolylinePointCommand.forNewLocation(editor, component, 0, Point2D(50, 0))

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
		val cmd = MovePolylinePointCommand.forNewLocation(editor, component, 0, Point2D(50, 0))
		cmd.execute()

		cmd.undo()

		assertEquals(Point2D(0, 0), component.getPointAt(0))
	}
}