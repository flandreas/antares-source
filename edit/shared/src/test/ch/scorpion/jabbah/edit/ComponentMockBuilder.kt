package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

/**
 * A builder for mocks of [Component].
 */
class ComponentMockBuilder {

    private val component = mockk<Component>(relaxed = true)

    init {
	    withId(0)
	    withBoundingBox(Rectangle2D())
        visible()
	    withInteractionHandler(InputEventHandlerAdapter())
	    withSelectionDrawingStrategy(SelectionDrawingStrategy.REPLACE)
    }

    fun withId(id: Int): ComponentMockBuilder {
        every { component.id } returns id
        return this
    }

	fun withType(type: String): ComponentMockBuilder {
		every { component.type } returns type
		return this
	}

	fun withBoundingBox(bbox: Rectangle2D): ComponentMockBuilder {
		every { component.boundingBox } returns bbox
		val x = slot<Double>()
		val y = slot<Double>()
		val p = slot<Point2D>()
		every { component.contains(capture(x), capture(y)) } answers { bbox.contains(x.captured, y.captured)}
		every { component.contains(capture(p)) } answers { bbox.contains(p.captured) }
		return this
	}

	fun visible(): ComponentMockBuilder {
		every { component.visible } returns true
		return this
	}

	fun invisible(): ComponentMockBuilder {
		every { component.visible } returns false
		return this
	}

	fun withInteractionHandler(handler: InputEventHandler<EditInputEventContext>): ComponentMockBuilder {
		every { component.getInputEventHandler<EditInputEventContext>(any()) } returns handler
		return this
	}

	fun withSelectionDrawingStrategy(strategy: SelectionDrawingStrategy): ComponentMockBuilder {
		every { component.preferredSelectionDrawingStrategy } returns strategy
		return this
	}

    fun build() = component
}