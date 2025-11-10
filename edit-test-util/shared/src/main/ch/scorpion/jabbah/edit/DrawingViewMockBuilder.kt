package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.container.UnzoomableContainerIF
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

/** A builder for [DrawingView] mocks*/
class DrawingViewMockBuilder {

    private val drawingView = mock<DrawingView<Drawing<*>>>(MockMode.autofill)
    private val selectionManager = mock<SelectionManager>(MockMode.autofill)
    private val grid = mock<Grid>(MockMode.autofill)
    private val ghostContainer = mock<UnzoomableContainerIF<Unzoomable>>(MockMode.autofill)
    private val animationContainer = mock<DrawableContainer<Drawable>>(MockMode.autofill)
    private val canvas = mock<Canvas>(MockMode.autofill)

    init {
        every { grid.distance } returns 10.0
        every { drawingView.grid } returns grid
        every { drawingView.selectionManager } returns selectionManager
        every { drawingView.editable } returns true
        every { drawingView.ghostContainer } returns ghostContainer
        every { drawingView.animationContainer } returns animationContainer

        every { canvas.hasFocus } returns true
        every { drawingView.canvas } returns canvas
    }

    fun withDrawing(drawing: Drawing<*>): DrawingViewMockBuilder {
        every { drawingView.drawing } returns drawing
        return this
    }

    fun withSelection(vararg components: Component): DrawingViewMockBuilder {
        every { selectionManager.selection } returns components.toList()
        return this
    }

    fun withSize(w: Int, h: Int): DrawingViewMockBuilder {
        every { drawingView.width } returns w
        every { drawingView.height } returns h
        return this
    }

    fun withModelToView(rectangularShape: RectangularShape): DrawingViewMockBuilder {
        every { drawingView.modelToView(any<RectangularShape>()) } returns rectangularShape
        return this
    }

    fun <T: Component> build(): DrawingView<Drawing<T>> {
        return drawingView as DrawingView<Drawing<T>>
    }
}