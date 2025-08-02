package ch.scorpion.jabbah.draw

import ch.scorpion.jabbah.base.geom.Point2D
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock

class InputEventHandlerMockBuilder {

	private val handler = mock<InputEventHandler<InputEventContext>>(MockMode.autofill)

	private val context = Capture.Companion.slot<InputEventContext>()

	val eventLocation: Point2D get() = context.get().location

	fun withMouseMoved(handle: Boolean): InputEventHandlerMockBuilder {
		every { handler.mouseMoved(capture(context)) } returns (if (handle) handler else null)
		return this
	}

	fun build() = handler
}