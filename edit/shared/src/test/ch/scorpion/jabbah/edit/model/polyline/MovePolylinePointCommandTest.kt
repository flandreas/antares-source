package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.Editor
import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/** Unit test for [MovePolylinePointCommand].*/
class MovePolylinePointCommandTest {

	companion object {
		@ClassRule
		@JvmField
		val editTestRule = EditTestRule()
	}

	private val editor = mock<Editor>()

	@Test
	fun shouldExecuteMovePoint() {
		val component = PolylineComponent()
			.addPoint(0, 0)
			.addPoint(100, 0)
			.addPoint(100, 100)
			.addPoint(0, 100)
		val cmd = MovePolylinePointCommand(editor, component, 0, Point2D(50, 0))

		cmd.execute()

		assertThat(component.getPointAt(0), `is`(Point2D(50, 0)))
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

		assertThat(component.getPointAt(0), `is`(Point2D(0, 0)))
	}
}