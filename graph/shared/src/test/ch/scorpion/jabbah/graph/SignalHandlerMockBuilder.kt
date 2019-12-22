package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.execution.SignalHandler
import io.mockk.every
import io.mockk.mockk

/**
 * A builder for mocks of [SignalHandler].
 * TODO Copy/Paste of corresponding class in execution module. Wasn't able to include test utilities from other project
 * with gradle.
 */
class SignalHandlerMockBuilder {

	private val signalHandler = mockk<SignalHandler>()

	fun withExecutionTime(time: Long): SignalHandlerMockBuilder {
		every { signalHandler.executionTime } returns time
		return this
	}

	fun build() = signalHandler
}