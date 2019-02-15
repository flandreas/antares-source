package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.Editor
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit test for [MovePolylinePointCommand].*/
class MovePolylinePointCommandTest {

	@BeforeTest
	fun setup() {
		EditTestRule.configure()
	}

	private val editor = mockk<Editor>()

	@Test
	fun shouldExecuteMovePoint() {
		val component = PolylineComponent()
			.addPoint(0, 0)
			.addPoint(100, 0)
			.addPoint(100, 100)
			.addPoint(0, 100)
		val cmd = MovePolylinePointCommand(editor, component, 0, Point2D(50, 0))

		cmd.execute()

		assertEquals(Point2D(50, 0), component.getPointAt(0))
	}

	@Test
	fun shouldUndoMovePoint() {
		val component = PolylineComponent()
			.addPoint(0, 0)
			.addPoint(100, 0)
			.addPoint(100, 100)
			.addPoint(0, 100)
		val cmd = MovePolylinePointCommand(editor, component, 0, Point2D(50, 0))
		cmd.execute()

		cmd.undo()

		assertEquals(Point2D(0, 0), component.getPointAt(0))
	}
}