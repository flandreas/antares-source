package ch.scorpion.jabbah.execution

import io.mockk.every
import io.mockk.mockk

/** A builder for mocks of [SignalHandler].*/
class SignalHandlerMockBuilder {

    private val signalHandler = mockk<SignalHandler>()

    fun withExecutionTime(time: Long): SignalHandlerMockBuilder {
        every { signalHandler.executionTime } returns time
        return this
    }

    fun build() = signalHandler
}