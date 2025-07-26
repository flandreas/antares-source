package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.edit.AbstractEditIntegrationTest
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DrawingAppServiceImplTest : AbstractEditIntegrationTest() {

    private val service = DrawingAppServiceImpl()

    private fun addComponents() {
        view.drawing.add(RectangleComponent())
        view.drawing.add(RectangleComponent())
    }

    @Test
    fun shouldDelete() {
        addComponents()

        delete(1)

        assertDeleted(1)
    }

    private fun delete(id: Int) {
        service.delete(listOf(view.drawing.getWithId(id)!!), view)
    }

    private fun assertDeleted(id: Int) {
        assertNull(view.drawing.getWithId(id))
        assertNotNull(view.drawing.getWithId(2))
        assertEquals(1, view.drawing.drawables.size)
    }

    private fun assertInitial() {
        assertNotNull(view.drawing.getWithId(1))
        assertNotNull(view.drawing.getWithId(2))
        assertEquals(2, view.drawing.drawables.size)
    }

    @Test
    fun shouldUndoDelete() {
        addComponents()
        delete(1)

        editor.commandManager.undo()

        assertInitial()
    }

    @Test
    fun shouldRedoDelete() {
        addComponents()
        delete(1)
        editor.commandManager.undo()

        editor.commandManager.redo()

        assertDeleted(1)
    }
}