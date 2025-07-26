package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentMockBuilder
import ch.scorpion.jabbah.edit.module.EditModule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DrawingServiceImplTest {

    private val service = DrawingServiceImpl()

    @BeforeTest
    fun setup() {
        EditModule.require()
    }

    @Test
    fun shouldDelete() {
        val drawing = DrawingImpl<Component>()
        val c1 = ComponentMockBuilder().withId(1).build()
        val c2 = ComponentMockBuilder().withId(2).build()

        drawing.add(c1)
        drawing.add(c2)

        service.delete(listOf(c1), drawing)

        assertFalse(drawing.contains(c1))
        assertTrue(drawing.contains(c2))
        assertEquals(1, drawing.drawables.size)
    }

    @Test
    fun shouldDeleteWithBuddy() {
        val drawing = DrawingImpl<Component>()
        val c1 = ComponentMockBuilder().withId(1).build()
        val c2 = ComponentMockBuilder().withId(2).withDeleteBuddies(listOf(c1)).build()
        val c3 = ComponentMockBuilder().withId(3).build()

        drawing.add(c1)
        drawing.add(c2)
        drawing.add(c3)

        service.delete(listOf(c2), drawing)

        assertFalse(drawing.contains(c1))
        assertFalse(drawing.contains(c2))
        assertTrue(drawing.contains(c3))
        assertEquals(1, drawing.drawables.size)
    }
}