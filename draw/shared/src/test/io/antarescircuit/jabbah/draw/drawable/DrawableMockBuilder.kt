package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.*
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.matcher.eq
import dev.mokkery.mock

/**
 * A builder for mocks of [Drawable].
 */
class DrawableMockBuilder {

    private val drawable: Drawable = mock(MockMode.autofill)

	private val boundingBoxDrawer: (DrawContext) -> Unit = { it.g.drawRect(
		drawable.boundingBox.x.toInt(),
		drawable.boundingBox.y.toInt(),
		drawable.boundingBox.width.toInt(),
		drawable.boundingBox.height.toInt())
	}

    init {
	    withBoundingBox(Rectangle2D())
	    withDrawLogic(boundingBoxDrawer)
	    withInteractionHandler(InputEventHandlerAdapter())
	    visible()
    }

	fun withBoundingBox(bbox: Rectangle2D): DrawableMockBuilder {
		every { drawable.boundingBox } returns bbox
		val x = Capture.slot<Double>()
		val y = Capture.slot<Double>()
		val point = Capture.slot<Point2D>()
		every { drawable.contains(capture(x), capture(y)) } calls {
			bbox.contains(it.args[0] as Double, it.args[1] as Double)
		}
		every { drawable.contains(capture(point)) } calls {
			bbox.contains(it.args[0] as Point2D)
		}
		return this
	}

	fun visible(): DrawableMockBuilder {
		every { drawable.visible } returns true
		return this
	}

    fun invisible(): DrawableMockBuilder {
	    every { drawable.visible } returns false
        return this
    }

    fun contains(x: Double, y: Double): DrawableMockBuilder {
	    every { drawable.contains(x, y) } returns true
	    every { drawable.contains(Point2D(x, y))} returns true
        return this
    }

    fun tooltip(s: String): DrawableMockBuilder {
	    every { drawable.getTooltip(any()) } returns Tooltip(s, 0.0, 0.0)
        return this
    }

	fun withInteractionHandler(handler: InputEventHandler<InputEventContext>): DrawableMockBuilder {
		every { drawable.getInputEventHandler(any()) } returns handler
		return this
	}

	fun withDrawLogic(logic: (context: DrawContext) -> Unit): DrawableMockBuilder {
		val slot = Capture.slot<DrawContext>()
		every { drawable.draw(capture(slot)) } calls { logic.invoke(slot.get()) }
		return this
	}

    fun build() = drawable
}