package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.execution.SignalHandler
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

/**
 * A builder for mocks of [SignalHandler].
 * TODO Copy/Paste of corresponding class in execution module. Wasn't able to include test utilities from other project
 * with gradle.
 */
class SignalHandlerMockBuilder {

	private val signalHandler = mock<SignalHandler>(MockMode.autofill)

	fun withExecutionTime(time: Long): SignalHandlerMockBuilder {
		every { signalHandler.executionTime } returns time
		return this
	}

	fun build() = signalHandler
}