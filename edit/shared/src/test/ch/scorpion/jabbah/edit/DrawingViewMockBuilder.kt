package ch.scorpion.jabbah.edit

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever

/** A builder for [DrawingView] mocks*/
class DrawingViewMockBuilder {

    private val drawingView = mock<DrawingView<Drawing<*>>>()
    private val selectionManager = mock<SelectionManager>()
    private val grid = mock<Grid>()

    init {
        whenever(grid.distance).thenReturn(10.0)
        whenever(drawingView.grid).thenReturn(grid)
        whenever(drawingView.selectionManager).thenReturn(selectionManager)
    }

    fun withDrawing(drawing: Drawing<*>): DrawingViewMockBuilder {
        whenever(drawingView.drawing).thenReturn(drawing)
        return this
    }

    fun <T: Component> build(): DrawingView<Drawing<T>> {
        return drawingView as DrawingView<Drawing<T>>
    }
}