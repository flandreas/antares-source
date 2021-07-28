package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.text.StyledTextBuilder
import ch.scorpion.jabbah.draw.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

/**
 * A builder for mocks of [Drawable].
 */
class DrawableMockBuilder {

    private val drawable: Drawable = mockk(relaxed = true)

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
		val x = slot<Double>()
		val y = slot<Double>()
		val point = slot<Point2D>()
		every { drawable.contains(capture(x), capture(y)) } answers { bbox.contains(x.captured, y.captured) }
		every { drawable.contains(capture(point)) } answers { bbox.contains(point.captured)}
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
	    every { drawable.contains(eq(x), eq(y)) } returns true
	    every { drawable.contains(eq(Point2D(x, y)))} returns true
        return this
    }

    fun tooltip(s: String): DrawableMockBuilder {
	    every { drawable.getTooltip(any(), any()) } returns Tooltip(StyledTextBuilder().append(s).build(), 0.0, 0.0)
        return this
    }

	fun withInteractionHandler(handler: InputEventHandler<InputEventContext>): DrawableMockBuilder {
		every { drawable.getInputEventHandler(any()) } returns handler
		return this
	}

	fun withDrawLogic(logic: (context: DrawContext) -> Unit): DrawableMockBuilder {
		val slot = slot<DrawContext>()
		every { drawable.draw(capture(slot)) } answers { logic.invoke(slot.captured) }
		return this
	}

    fun build() = drawable
}