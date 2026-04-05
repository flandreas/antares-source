package io.antarescircuit.jabbah.edit.model.polyline

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class JoinPolylinePointsCommandTest {

	private val drawing: DrawingImpl<Component>
	private val editor = mock<Editor>()

	init {
		EditTestRule.configure()
		drawing = DrawingImpl<Component>()
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