package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.geom.Dimension2D
import io.mockk.every
import io.mockk.mockk

class CanvasMockBuilder {

	private val canvas: Canvas = mockk(relaxed = true)

	fun withView(view: View<*>): CanvasMockBuilder {
		every { canvas.view } returns view
		view.canvas = canvas
		return this
	}

	fun withDimension(dimension: Dimension2D): CanvasMockBuilder {
		every { canvas.dimension } returns dimension
		return this
	}

	fun build(): Canvas = canvas
}