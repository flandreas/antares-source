package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentMockBuilder
import ch.scorpion.jabbah.edit.model.DrawingImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [OneDownCommand].
 */
class OneDownCommandTest {

    private val drawing = DrawingImpl<Component>()
    private val c1 = ComponentMockBuilder().build()
    private val c2 = ComponentMockBuilder().build()
    private val c3 = ComponentMockBuilder().build()
    private val c4 = ComponentMockBuilder().build()

    @BeforeTest
    fun setup() {
	    EditTestRule.configure()
        drawing.add(c4).add(c3).add(c2).add(c1)
    }

    @Test
    fun shouldExecuteOneUp() {
        val command = OneDownCommand(drawing, setOf(c1, c3))
        command.execute()
        assertEquals(0, drawing.getStackingOrderPosition(c2))
        assertEquals(1, drawing.getStackingOrderPosition(c1))
        assertEquals(2, drawing.getStackingOrderPosition(c4))
        assertEquals(3, drawing.getStackingOrderPosition(c3))
    }

    @Test
    fun shouldMaintainRelativeOrderWhenExecuting() {
        val command = OneDownCommand(drawing, setOf(c1, c3, c4))
        command.execute()
        assertEquals(0, drawing.getStackingOrderPosition(c2))
        assertEquals(1, drawing.getStackingOrderPosition(c1))
        assertEquals(2, drawing.getStackingOrderPosition(c3))
        assertEquals(3, drawing.getStackingOrderPosition(c4))
    }

    @Test
    fun shouldUndoOneUp() {
        val command = OneDownCommand(drawing, setOf(c1, c3))
        command.execute()
        command.undo()
        assertEquals(0, drawing.getStackingOrderPosition(c1))
        assertEquals(1, drawing.getStackingOrderPosition(c2))
        assertEquals(2, drawing.getStackingOrderPosition(c3))
        assertEquals(3, drawing.getStackingOrderPosition(c4))
    }

    @Test
    fun shouldUndoMaintainRelativeOrderWhenExecuting() {
        val command = OneDownCommand(drawing, setOf(c1, c3, c4))
        command.execute()
        command.undo()
        assertEquals(0, drawing.getStackingOrderPosition(c1))
        assertEquals(1, drawing.getStackingOrderPosition(c2))
        assertEquals(2, drawing.getStackingOrderPosition(c3))
        assertEquals(3, drawing.getStackingOrderPosition(c4))
    }
}