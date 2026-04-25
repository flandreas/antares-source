package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.CanvasMockBuilder
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawableContainer
import io.antarescircuit.jabbah.draw.container.UnzoomableContainerIF
import io.antarescircuit.jabbah.draw.drawable.Unzoomable
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

/** A builder for [DrawingView] mocks*/
class DrawingViewMockBuilder {

    private val drawingView = mock<DrawingView<*, Drawing<*>>>(MockMode.autofill)
    private val selectionManager = mock<SelectionManager>(MockMode.autofill)
    private val grid = mock<Grid>(MockMode.autofill)
    private val ghostContainer = mock<UnzoomableContainerIF<Unzoomable>>(MockMode.autofill)
    private val animationContainer = mock<DrawableContainer<Drawable>>(MockMode.autofill)
    private val canvasBuilder = CanvasMockBuilder().withView(drawingView)

    init {
        every { grid.distance } returns 10.0
        every { drawingView.grid } returns grid
        every { drawingView.selectionManager } returns selectionManager
        every { drawingView.editable } returns true
        every { drawingView.ghostContainer } returns ghostContainer
        every { drawingView.animationContainer } returns animationContainer
        every { drawingView.canvas } returns canvasBuilder.build()
        every { drawingView.center } calls { Point2D(drawingView.width / 2, drawingView.height / 2) }
        every { drawingView.modelToView(any<Point2D>()) } returnsArgAt 0
        every { drawingView.viewToModel(any()) } returnsArgAt 0
    }

    fun withDrawing(drawing: Drawing<*>): DrawingViewMockBuilder {
        every { drawingView.drawing } returns drawing
        return this
    }

    fun withDrawingAccessor(accessor: () -> Drawing<*>): DrawingViewMockBuilder {
        every { drawingView.drawing } calls { accessor() }
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

    fun withSelectionManager(sm: SelectionManager): DrawingViewMockBuilder {
        every { drawingView.selectionManager } returns sm
        return this
    }

    fun <C: Component, T : Drawing<C>> build(): DrawingView<C, T> {
        @Suppress("UNCHECKED_CAST")
        return drawingView as DrawingView<C, T>
    }
}