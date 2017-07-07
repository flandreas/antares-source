package ch.scorpion.jabbah.edit.editor

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import ch.scorpion.jabbah.edit.*

/**
 * Builds test mocks for [Editor].
 */
class TestEditorBuilder {

    val editor: Editor = mock()
    private val view: DrawingView<Drawing<Component>> = mock()

    init {
        whenever(editor.view).thenReturn(view)
    }

    fun withDrawing(drawing: Drawing<Component>): TestEditorBuilder {
        whenever(editor.drawing).thenReturn(drawing)
        whenever(view.drawing).thenReturn(drawing)
        return this
    }

    fun withGrid(grid: Grid): TestEditorBuilder {
        whenever(view.grid).thenReturn(grid)
        return this
    }

    fun withGridDistance(distance: Double): TestEditorBuilder {
        val grid: Grid = mock()
        whenever(grid.distance).thenReturn(distance)
        whenever(view.grid).thenReturn(grid)
        return this
    }

    fun withSelectionManager(): TestEditorBuilder {
        whenever(view.selectionManager).thenReturn(mock<SelectionManager>())
        return this
    }
}