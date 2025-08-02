package ch.scorpion.jabbah.execution

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock

/** A builder for mocks of [SignalHandler].*/
class SignalHandlerMockBuilder {

    private val signalHandler = mock<SignalHandler>(MockMode.autofill)

    fun withExecutionTime(time: Long): SignalHandlerMockBuilder {
        every { signalHandler.executionTime } returns time
        return this
    }

    fun build() = signalHandler
}