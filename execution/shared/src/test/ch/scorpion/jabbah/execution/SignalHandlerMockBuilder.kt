package ch.scorpion.jabbah.execution

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever

/** A builder for mocks of [SignalHandler].*/
class SignalHandlerMockBuilder {

    private val signalHandler = mock<SignalHandler>()

    fun withExecutionTime(time: Long): SignalHandlerMockBuilder {
        whenever(signalHandler.executionTime).thenReturn(time)
        return this
    }

    fun build() = signalHandler
}