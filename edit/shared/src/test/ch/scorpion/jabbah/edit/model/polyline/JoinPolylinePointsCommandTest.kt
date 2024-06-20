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

/** Unit tests for [JoinPolylinePointsCommand].*/
class JoinPolylinePointsCommandTest {

	companion object {
		init {
			EditTestRule.configure()
		}
	}

	private val drawing = DrawingImpl<Component>()
	private val editor = mock<Editor>()

	init {
		every { editor.drawing } returns drawing
	}

	@Test
	fun shouldExecuteJoinPoints() {
		val component = PolylineComponent()
		component.addPoint(0, 0)
			.addPoint(100, 0)
			.addPoint(100, 100)
			.addPoint(0, 100)
		drawing.add(component)
		val cmd = JoinPolylinePointsCommand(editor, component.id, 0, Point2D(0, 0))

		cmd.execute()

		assertEquals(3, component.pointsCount)
		assertEquals(Point2D(100, 0), component.getPointAt(0))
	}

	@Test
	fun shouldUndoJoinPoints() {
		val component = PolylineComponent()
		component.addPoint(0, 0)
			.addPoint(100, 0)
			.addPoint(100, 100)
			.addPoint(0, 100)
		drawing.add(component)
		val cmd = JoinPolylinePointsCommand(editor, component.id, 0, Point2D(0, 0))
		cmd.execute()

		cmd.undo()

		assertEquals(4, component.pointsCount)
		assertEquals(Point2D(0, 0), component.getPointAt(0))
		assertEquals(Point2D(100, 0), component.getPointAt(1))
	}
}