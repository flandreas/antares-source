package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.InputEventHandler
import io.mockk.every
import io.mockk.mockk

/**
 * TODO: Copy/Paste from corresponding class in ch.scorpion.jabbah.draw test package
 * due to missing Kotlin MPP feature KT-35073.
 */

class InputEventHandlerMockBuilder {

	private val handler = mockk<InputEventHandler<EditInputEventContext>>()

	fun withMouseMoved(handle: Boolean): InputEventHandlerMockBuilder {
		every { handler.mouseMoved(any()) } returns if (handle) handler else null
		return this
	}

	fun build() = handler
}