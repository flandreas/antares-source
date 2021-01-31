package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class Graphics2DMockBuilder {

	private val g: Graphics2D = mockk(relaxed = true)

	private var transform = System.createAffineTransform()
	private val transformSlot = slot<AffineTransform>()
	private val x = slot<Int>()
	private val y = slot<Int>()
	private val width = slot<Int>()
	private val height = slot<Int>()

	lateinit var drawnRectangle: Rectangle2D
		private set

	init {
		every { g.transform } returns transform
		every { g.transform = capture(transformSlot) } answers { transform = transformSlot.captured }

		every { g.drawRect(capture(x), capture(y), capture(width), capture(height))} answers {
			val topLeft = transform.transform(Point2D(x.captured, y.captured))
			val bottomRight = transform.transform(Point2D(x.captured + width.captured, y.captured + height.captured))
			drawnRectangle = Rectangle2D(
				x = topLeft.x,
				y = topLeft.y,
				width = bottomRight.x - topLeft.x,
				height = bottomRight.y - topLeft.y)
		}
	}

	fun build(): Graphics2D = g
}