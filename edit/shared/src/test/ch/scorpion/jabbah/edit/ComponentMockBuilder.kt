package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.mock

/**
 * A builder for mocks of [Component].
 */
class ComponentMockBuilder {

    private val component = mock<Component>(MockMode.autofill)

    init {
	    withId(0)
	    withBoundingBox(Rectangle2D())
        visible()
	    withInteractionHandler(InputEventHandlerAdapter())
	    withSelectionDrawingStrategy(SelectionDrawingStrategy.REPLACE)
		withTooltip(null)
		withDeleteBuddies(emptyList())
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
		val x = Capture.slot<Double>()
		val y = Capture.slot<Double>()
		val p = Capture.slot<Point2D>()
		every { component.contains(capture(x), capture(y)) } calls {
			bbox.contains(it.args[0] as Double, it.args[1] as Double)
		}
		every { component.contains(capture(p)) } calls {
			bbox.contains(it.args[0] as Point2D)
		}
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

	fun withTooltip(tooltip: Tooltip?): ComponentMockBuilder {
		every { component.getTooltip(any()) } returns tooltip
		return this
	}

	fun withDeleteBuddies(buddies: List<Component>): ComponentMockBuilder {
		every { component.getDeleteBuddies(any()) } returns buddies
		return this
	}

    fun build() = component
}