package ch.scorpion.jabbah.edit

import io.mockk.every
import io.mockk.mockk

/** A builder for [DrawingView] mocks*/
class DrawingViewMockBuilder {

    private val drawingView = mockk<DrawingView<Drawing<*>>>()
    private val selectionManager = mockk<SelectionManager>(relaxed = true)
    private val grid = mockk<Grid>(relaxed = true)

    init {
        every { grid.distance } returns 10.0
        every { drawingView.grid } returns grid
        every { drawingView.selectionManager } returns selectionManager
    }

    fun withDrawing(drawing: Drawing<*>): DrawingViewMockBuilder {
        every { drawingView.drawing } returns drawing
        return this
    }

    fun <T: Component> build(): DrawingView<Drawing<T>> {
        return drawingView as DrawingView<Drawing<T>>
    }
}