package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.io.StorableCloner
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class OneUpCommandTest {

    private val drawing = DrawingImpl<Component>()
	private val drawingView = mock<DrawingView<Drawing<Component>>>()
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

		every { drawingView.drawing } returns drawing
		drawing.add(c4).add(c3).add(c2).add(c1)
	}

    @Test
    fun shouldExecuteOneUp() {
        val command = OneUpCommand(drawingView, setOf(c2.id, c4.id))

        command.execute()

        assertEquals(0, drawing.getStackingOrderPosition(c2.id))
        assertEquals(1, drawing.getStackingOrderPosition(c1.id))
        assertEquals(2, drawing.getStackingOrderPosition(c4.id))
        assertEquals(3, drawing.getStackingOrderPosition(c3.id))
    }

    @Test
    fun shouldMaintainRelativeOrderWhenExecuting() {
        val command = OneUpCommand(drawingView, setOf(c1.id, c2.id, c4.id))

        command.execute()

        assertEquals(0, drawing.getStackingOrderPosition(c1.id))
        assertEquals(1, drawing.getStackingOrderPosition(c2.id))
        assertEquals(2, drawing.getStackingOrderPosition(c4.id))
        assertEquals(3, drawing.getStackingOrderPosition(c3.id))
    }

    @Test
    fun shouldUndoOneUp() {
        val command = OneUpCommand(drawingView, setOf(c2.id, c4.id))
        command.execute()

        command.undo()

        assertEquals(0, drawing.getStackingOrderPosition(c1.id))
        assertEquals(1, drawing.getStackingOrderPosition(c2.id))
        assertEquals(2, drawing.getStackingOrderPosition(c3.id))
        assertEquals(3, drawing.getStackingOrderPosition(c4.id))
    }

	@Test
	fun shouldUndoOneUpWithDrawingSnapshot() {
		val command = OneUpCommand(drawingView, setOf(c2.id, c4.id))
		command.execute()
		val clone = StorableCloner.clone(drawing)
		every { drawingView.drawing } returns clone

		command.undo()

		assertEquals(0, drawingView.drawing.getStackingOrderPosition(c1.id))
		assertEquals(1, drawingView.drawing.getStackingOrderPosition(c2.id))
		assertEquals(2, drawingView.drawing.getStackingOrderPosition(c3.id))
		assertEquals(3, drawingView.drawing.getStackingOrderPosition(c4.id))
	}

    @Test
    fun shouldUndoMaintainRelativeOrderWhenExecuting() {
        val command = OneUpCommand(drawingView, setOf(c1.id, c2.id, c4.id))
        command.execute()

        command.undo()

        assertEquals(0, drawing.getStackingOrderPosition(c1.id))
        assertEquals(1, drawing.getStackingOrderPosition(c2.id))
        assertEquals(2, drawing.getStackingOrderPosition(c3.id))
        assertEquals(3, drawing.getStackingOrderPosition(c4.id))
    }
}