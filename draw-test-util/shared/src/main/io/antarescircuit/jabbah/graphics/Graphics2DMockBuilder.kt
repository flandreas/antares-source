package io.antarescircuit.jabbah.graphics

import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.AffineTransform
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.Graphics2D
import io.antarescircuit.jabbah.draw.graphics.Stroke
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
	private val transformSlot = Capture.slot<AffineTransform>()
	private val x = Capture.slot<Int>()
	private val y = Capture.slot<Int>()
	private val width = Capture.slot<Int>()
	private val height = Capture.slot<Int>()
	private val xDouble = Capture.slot<Double>()
	private val yDouble = Capture.slot<Double>()
	private val point = Capture.slot<Point2D>()

	lateinit var drawnRectangle: Rectangle2D
		private set

	init {
		withColor(Color.BLACK)
		withStroke(Stroke())
		every { g.transform } returns transform
		every { g.transform = capture(transformSlot) } calls { transform = transformSlot.get() }
		every { g.scale(capture(xDouble), capture(yDouble)) } calls  { transform.scale(xDouble.get(), yDouble.get()) }
		every { g.translate(capture(xDouble), capture(yDouble)) } calls  { transform.translate(xDouble.get(), yDouble.get()) }
		every { g.translate(capture(point)) } calls  { transform.translate(point.get()) }

		every { g.drawRect(capture(x), capture(y), capture(width), capture(height)) } calls  {
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

	fun withStroke(stroke: Stroke): Graphics2DMockBuilder {
		every { g.stroke } returns stroke
		return this
	}

	fun build(): Graphics2D = g
}