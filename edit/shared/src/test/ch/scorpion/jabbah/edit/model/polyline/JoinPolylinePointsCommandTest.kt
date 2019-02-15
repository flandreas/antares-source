package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.Editor
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [JoinPolylinePointsCommand].*/
class JoinPolylinePointsCommandTest {

	@BeforeTest
	fun setup() {
		EditTestRule.configure()
	}

	private val editor = mockk<Editor>()

	@Test
	fun shouldExecuteJoinPoints() {
		val component = PolylineComponent()
			.addPoint(0, 0)
			.addPoint(100, 0)
			.addPoint(100, 100)
			.addPoint(0, 100)
		val cmd = JoinPolylinePointsCommand(editor, component, 0, Point2D(0, 0))

		cmd.execute()

		assertEquals(3, component.pointsCount)
		assertEquals(Point2D(100, 0), component.getPointAt(0))
	}

	@Test
	fun shouldUndoJoinPoints() {
		val component = PolylineComponent()
			.addPoint(0, 0)
			.addPoint(100, 0)
			.addPoint(100, 100)
			.addPoint(0, 100)
		val cmd = JoinPolylinePointsCommand(editor, component, 0, Point2D(0, 0))
		cmd.execute()

		cmd.undo()

		assertEquals(4, component.pointsCount)
		assertEquals(Point2D(0, 0), component.getPointAt(0))
		assertEquals(Point2D(100, 0), component.getPointAt(1))
	}
}