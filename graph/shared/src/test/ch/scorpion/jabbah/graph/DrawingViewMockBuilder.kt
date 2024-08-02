package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.container.UnzoomableContainerIF
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.edit.*
import dev.mokkery.MockMode.autofill
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

/**
 * A builder for [DrawingView] mocks.
 * TODO Copy/Paste of corresponding class in edit module. Wasn't able to include test utilities from other project
 * with gradle.
 */
class DrawingViewMockBuilder {

	private val drawingView = mock<DrawingView<Drawing<*>>>(autofill)
	private val selectionManager = mock<SelectionManager>(autofill)
	private val grid = mock<Grid>(autofill)
	private val ghostContainer = mock<UnzoomableContainerIF<Unzoomable>>(autofill)
	private val animationContainer = mock<DrawableContainer<Drawable>>(autofill)

	init {
		every { grid.distance } returns 10.0
		every { drawingView.grid } returns grid
		every { drawingView.selectionManager } returns selectionManager
		every { drawingView.editable } returns true
		every { drawingView.ghostContainer } returns ghostContainer
		every { drawingView.animationContainer } returns animationContainer
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