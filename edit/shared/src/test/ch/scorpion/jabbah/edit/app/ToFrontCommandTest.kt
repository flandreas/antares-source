package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.TestTranslationsBuilder
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentMockBuilder
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test


/**
 * Unit tests for [ToFrontCommand].
 */
class ToFrontCommandTest {

    private val drawing = DrawingImpl<Component>()
    private val c1 = ComponentMockBuilder().build()
    private val c2 = ComponentMockBuilder().build()
    private val c3 = ComponentMockBuilder().build()
    private val c4 = ComponentMockBuilder().build()

    @Before
    fun setup() {
        EditModuleJvm.require()
        TestTranslationsBuilder().withAnyKey()
        drawing.add(c4).add(c3).add(c2).add(c1)
    }

    @Test
    fun shouldExecute() {
        val command = ToFrontCommand(drawing, setOf(c2, c4))
        command.execute()
        assertThat(drawing.getStackingOrderPosition(c2), `is`(0))
        assertThat(drawing.getStackingOrderPosition(c4), `is`(1))
        assertThat(drawing.getStackingOrderPosition(c1), `is`(2))
        assertThat(drawing.getStackingOrderPosition(c3), `is`(3))
    }

    @Test
    fun shouldUndo() {
        val command = ToFrontCommand(drawing, setOf(c2, c4))
        command.execute()
        command.undo()
        assertThat(drawing.getStackingOrderPosition(c1), `is`(0))
        assertThat(drawing.getStackingOrderPosition(c2), `is`(1))
        assertThat(drawing.getStackingOrderPosition(c3), `is`(2))
        assertThat(drawing.getStackingOrderPosition(c4), `is`(3))
    }
}