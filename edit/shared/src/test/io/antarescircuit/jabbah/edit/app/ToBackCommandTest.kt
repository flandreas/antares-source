package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.io.StorableCloner
import dev.mokkery.answering.returns
import dev.mokkery.every
import io.antarescircuit.jabbah.edit.Drawing
import kotlin.test.Test
import kotlin.test.assertEquals

class ToBackCommandTest {

    private val drawing = DrawingImpl<Component>()
    private val drawingView = DrawingViewMockBuilder().withDrawing(drawing).build<Component, Drawing<Component>>()
    private val c1: RectangleComponent
    private val c2: RectangleComponent
    private val c3: RectangleComponent
    private val c4: RectangleComponent

	init {
        EditTestRule.configure()
        c1 = RectangleComponent()
        c2 = RectangleComponent()
        c3 = RectangleComponent()
        c4 = RectangleComponent()

		drawing.add(c4).add(c3).add(c2).add(c1)
	}

    @Test
    fun shouldExecute() {
        val command = ToBackCommand(drawingView, setOf(c1.id, c3.id))

        command.execute()

        assertEquals(0, drawing.getStackingOrderPosition(c2.id))
        assertEquals(1, drawing.getStackingOrderPosition(c4.id))
        assertEquals(2, drawing.getStackingOrderPosition(c1.id))
        assertEquals(3, drawing.getStackingOrderPosition(c3.id))
    }

    @Test
    fun shouldUndo() {
        val command = ToBackCommand(drawingView, setOf(c1.id, c3.id))
        command.execute()

        command.undo()

        assertEquals(0, drawing.getStackingOrderPosition(c1.id))
        assertEquals(1, drawing.getStackingOrderPosition(c2.id))
        assertEquals(2, drawing.getStackingOrderPosition(c3.id))
        assertEquals(3, drawing.getStackingOrderPosition(c4.id))
    }

	@Test
	fun shouldUndoWithDrawingSnapshot() {
		val command = ToBackCommand(drawingView, setOf(c1.id, c3.id))
		command.execute()
		val clone = StorableCloner.clone(drawing)
		every { drawingView.drawing } returns clone

		command.undo()

		assertEquals(0, drawingView.drawing.getStackingOrderPosition(c1.id))
		assertEquals(1, drawingView.drawing.getStackingOrderPosition(c2.id))
		assertEquals(2, drawingView.drawing.getStackingOrderPosition(c3.id))
		assertEquals(3, drawingView.drawing.getStackingOrderPosition(c4.id))
	}
}