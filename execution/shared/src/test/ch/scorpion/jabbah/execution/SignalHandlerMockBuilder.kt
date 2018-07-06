package ch.scorpion.jabbah.execution

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

/** A builder for mocks of [SignalHandler].*/
class SignalHandlerMockBuilder {

    private val signalHandler = mock<SignalHandler>()

    fun withExecutionTime(time: Long): SignalHandlerMockBuilder {
        whenever(signalHandler.executionTime).thenReturn(time)
        return this
    }

    fun build() = signalHandler
}