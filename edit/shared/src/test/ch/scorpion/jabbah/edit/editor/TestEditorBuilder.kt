package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.edit.*
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

/**
 * Builds test mocks for [Editor].
 */
class TestEditorBuilder {

    val editor: Editor = mock(MockMode.autofill)
    private val view: DrawingView<Drawing<Component>> = mock(MockMode.autofill)

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
        val grid: Grid = mock()
	    every { grid.distance } returns distance
	    every { view.grid } returns grid
        return this
    }

    fun withSelectionManager(): TestEditorBuilder {
	    every { view.selectionManager } returns mock()
        return this
    }

	fun build(): Editor = editor
}