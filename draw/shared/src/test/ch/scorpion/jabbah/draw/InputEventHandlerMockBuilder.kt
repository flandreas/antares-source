package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.geom.Point2D
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class InputEventHandlerMockBuilder {

	private val handler = mockk<InputEventHandler<InputEventContext>>()

	private val context = slot<InputEventContext>()

	val eventLocation: Point2D get() = context.captured.location

	fun withMouseMoved(handle: Boolean): InputEventHandlerMockBuilder {
		every { handler.mouseMoved(capture(context)) } returns if (handle) handler else null
		return this
	}

	fun build() = handler
}