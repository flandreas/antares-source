package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [OneDownCommand].
 */
class OneDownCommandTest {

	companion object {
		init {
			EditTestRule.configure()
		}
	}

    private val drawing = DrawingImpl<Component>()
	private val drawingView = mockk<DrawingView<Drawing<Component>>>()
    private val c1 = RectangleComponent()
    private val c2 = RectangleComponent()
    private val c3 = RectangleComponent()
    private val c4 = RectangleComponent()

	init {
		every { drawingView.drawing } returns drawing
		drawing.add(c4).add(c3).add(c2).add(c1)
	}

    @Test
    fun shouldExecuteOneDown() {
        val command = OneDownCommand(drawingView, setOf(c1.id, c3.id))
        command.execute()
        assertEquals(0, drawing.getStackingOrderPosition(c2))
        assertEquals(1, drawing.getStackingOrderPosition(c1))
        assertEquals(2, drawing.getStackingOrderPosition(c4))
        assertEquals(3, drawing.getStackingOrderPosition(c3))
    }

    @Test
    fun shouldMaintainRelativeOrderWhenExecuting() {
        val command = OneDownCommand(drawingView, setOf(c1.id, c3.id, c4.id))
        command.execute()
        assertEquals(0, drawing.getStackingOrderPosition(c2))
        assertEquals(1, drawing.getStackingOrderPosition(c1))
        assertEquals(2, drawing.getStackingOrderPosition(c3))
        assertEquals(3, drawing.getStackingOrderPosition(c4))
    }

    @Test
    fun shouldUndoOneUp() {
        val command = OneDownCommand(drawingView, setOf(c1.id, c3.id))
        command.execute()
        command.undo()
        assertEquals(0, drawing.getStackingOrderPosition(c1))
        assertEquals(1, drawing.getStackingOrderPosition(c2))
        assertEquals(2, drawing.getStackingOrderPosition(c3))
        assertEquals(3, drawing.getStackingOrderPosition(c4))
    }

    @Test
    fun shouldUndoMaintainRelativeOrderWhenExecuting() {
        val command = OneDownCommand(drawingView, setOf(c1.id, c3.id, c4.id))
        command.execute()
        command.undo()
        assertEquals(0, drawing.getStackingOrderPosition(c1))
        assertEquals(1, drawing.getStackingOrderPosition(c2))
        assertEquals(2, drawing.getStackingOrderPosition(c3))
        assertEquals(3, drawing.getStackingOrderPosition(c4))
    }
}