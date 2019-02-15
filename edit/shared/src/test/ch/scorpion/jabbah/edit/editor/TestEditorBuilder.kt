package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.*
import io.mockk.every
import io.mockk.mockk

/**
 * Builds test mocks for [Editor].
 */
class TestEditorBuilder {

    val editor: Editor = mockk(relaxed = true)
    private val view: DrawingView<Drawing<Component>> = mockk(relaxed = true)

    init {
        every { editor.view } returns view
    }

    fun withDrawing(drawing: Drawing<Component>): TestEditorBuilder {
	    every { editor.drawing } returns drawing
	    every { view.drawing } returns drawing
        return this
    }

    fun withGrid(grid: Grid): TestEditorBuilder {
	    every { view.grid } returns grid
        return this
    }

    fun withGridDistance(distance: Double): TestEditorBuilder {
        val grid: Grid = mockk()
	    every { grid.distance } returns distance
	    every { view.grid } returns grid
        return this
    }

    fun withSelectionManager(): TestEditorBuilder {
	    every { view.selectionManager } returns mockk()
        return this
    }
}