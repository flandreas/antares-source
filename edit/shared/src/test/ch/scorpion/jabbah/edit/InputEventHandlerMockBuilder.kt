package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.InputEventHandler
import io.mockk.every
import io.mockk.mockk

class InputEventHandlerMockBuilder {

	private val handler = mockk<InputEventHandler<EditInputEventContext>>()

	fun withMouseMoved(handle: Boolean): InputEventHandlerMockBuilder {
		every { handler.mouseMoved(any()) } returns if (handle) handler else null
		return this
	}

	fun build() = handler
}