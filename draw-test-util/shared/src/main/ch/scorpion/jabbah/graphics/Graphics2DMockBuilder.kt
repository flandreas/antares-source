package ch.scorpion.jabbah.graphics

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock

class Graphics2DMockBuilder {

	private val g: Graphics2D = mock(MockMode.autofill)

	private var transform = System.createAffineTransform()
	private val transformSlot = Capture.Companion.slot<AffineTransform>()
	private val x = Capture.Companion.slot<Int>()
	private val y = Capture.Companion.slot<Int>()
	private val width = Capture.Companion.slot<Int>()
	private val height = Capture.Companion.slot<Int>()
	private val xDouble = Capture.Companion.slot<Double>()
	private val yDouble = Capture.Companion.slot<Double>()
	private val point = Capture.Companion.slot<Point2D>()

	lateinit var drawnRectangle: Rectangle2D
		private set

	init {
		withColor(Color.BLACK)
		every { g.transform } returns transform
		every { g.transform = capture(transformSlot) } calls { transform = transformSlot.get() }
		every { g.scale(capture(xDouble), capture(yDouble)) } calls  { transform.scale(xDouble.get(), yDouble.get()) }
		every { g.translate(capture(xDouble), capture(yDouble)) } calls  { transform.translate(xDouble.get(), yDouble.get()) }
		every { g.translate(capture(point)) } calls  { transform.translate(point.get()) }

		every { g.drawRect(capture(x) as Int, capture(y), capture(width), capture(height)) } calls  {
			val topLeft = transform.transform(Point2D(x.get(), y.get()))
			val bottomRight = transform.transform(Point2D(x.get() + width.get(), y.get() + height.get()))
			drawnRectangle = Rectangle2D(
                x = topLeft.x,
                y = topLeft.y,
                width = bottomRight.x - topLeft.x,
                height = bottomRight.y - topLeft.y
            )
		}
	}

	fun withColor(color: Color): Graphics2DMockBuilder {
		every { g.color } returns color
		return this
	}

	fun build(): Graphics2D = g
}