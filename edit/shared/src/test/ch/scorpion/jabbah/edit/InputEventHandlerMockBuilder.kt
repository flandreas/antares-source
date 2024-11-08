package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.InputEventHandler
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock

/**
 * TODO: Copy/Paste from corresponding class in ch.scorpion.jabbah.draw test package
 * due to missing Kotlin MPP feature KT-35073.
 */

class InputEventHandlerMockBuilder {

	private val handler = mock<InputEventHandler<EditInputEventContext>>()

	fun withMouseMoved(handle: Boolean): InputEventHandlerMockBuilder {
		every { handler.mouseMoved(any()) } returns if (handle) handler else null
		return this
	}

	fun build() = handler
}