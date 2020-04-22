package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.edit.*
import io.mockk.every
import io.mockk.mockk

/**
 * A builder for [DrawingView] mocks.
 * TODO Copy/Paste of corresponding class in edit module. Wasn't able to include test utilities from other project
 * with gradle.
 */
class DrawingViewMockBuilder {

	private val drawingView = mockk<DrawingView<Drawing<*>>>(relaxed = true)
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

	fun withSelection(vararg components: Component): DrawingViewMockBuilder {
		every { selectionManager.selection } returns components.toList()
		return this
	}

	fun <T: Component> build(): DrawingView<Drawing<T>> {
		return drawingView as DrawingView<Drawing<T>>
	}
}