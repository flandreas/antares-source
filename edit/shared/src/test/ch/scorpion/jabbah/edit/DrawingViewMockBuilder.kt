package ch.scorpion.jabbah.edit

import dev.mokkery.MockMode.autofill
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock


/** A builder for [DrawingView] mocks*/
class DrawingViewMockBuilder {

    private val drawingView = mock<DrawingView<Drawing<*>>>(autofill)
    private val selectionManager = mock<SelectionManager>(autofill)
    private val grid = mock<Grid>(autofill)

    init {
	    editable(true)
        every { grid.distance } returns 10.0
        every { drawingView.grid } returns grid
        every { drawingView.selectionManager } returns selectionManager
    }

    fun withDrawing(drawing: Drawing<*>): DrawingViewMockBuilder {
        every { drawingView.drawing } returns drawing
        return this
    }

	fun editable(editable: Boolean): DrawingViewMockBuilder {
		every { drawingView.editable } returns editable
		return this
	}

    fun <T: Component> build(): DrawingView<Drawing<T>> {
        return drawingView as DrawingView<Drawing<T>>
    }
}