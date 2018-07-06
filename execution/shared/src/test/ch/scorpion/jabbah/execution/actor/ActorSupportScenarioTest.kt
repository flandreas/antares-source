package ch.scorpion.jabbah.execution.actor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import ch.scorpion.jabbah.execution.ExecutionTestRule
import ch.scorpion.jabbah.execution.SignalHandler
import org.junit.ClassRule
import org.junit.Test

/**
 * Scenario tests for [ActorSupport] using [TestVertice], which inherits [ActorSupport] functionality
 * from [AbstractGraphElement].
 */
class ActorSupportScenarioTest {

    companion object {
        @ClassRule @JvmField
        val testRule = ExecutionTestRule()
    }

    val signalHandler: SignalHandler = mock()

    @Test
    fun shouldRequestActing() {
        val actorSupport = ActorSupport(mock())
        val view1: ActorListener = mock()
        actorSupport.addListener(view1)
        val view2: ActorListener = mock()
        actorSupport.addListener(view2)

        actorSupport.requestActingAfter(signalHandler, 0, mock())

        verify(signalHandler).requestActingAfter(any(), any(), any())
        verify(view1).actingRequested(any(), any(), any())
        verify(view2).actingRequested(any(), any(), any())
    }

    @Test
    fun shouldAct() {
        val actor: Actor = mock()
        val actorSupport = ActorSupport(actor)
        val view1: ActorListener = mock()
        actorSupport.addListener(view1)
        val view2: ActorListener = mock()
        actorSupport.addListener(view2)

        val actorData: ActorData = mock()
        actorSupport.requestActingAfter(signalHandler, 0,actorData)
        actorSupport.notifyActed(signalHandler, actorData)

        verify(view1).acted(actor, signalHandler,actorData)
        verify(view2).acted(actor, signalHandler,actorData)
    }

    @Test
    fun shouldWaitForAllVisualizations() {
        val actor: Actor = mock()
        val actorSupport = ActorSupport(actor)
        val view1: ActorListener = mock()
        actorSupport.addListener(view1)
        val view2: ActorListener = mock()
        actorSupport.addListener(view2)

        val actorData: ActorData = mock()
        actorSupport.requestActingAfter(signalHandler, 0, actorData)
        actorSupport.notifyActed(signalHandler, mock())
        verify(signalHandler, times(0)).actingDone(actor)

        actorSupport.actingVisualized(signalHandler, view1)
        verify(signalHandler, times(0)).actingDone(actor)

        actorSupport.actingVisualized(signalHandler, view2)
        verify(signalHandler, times(1)).actingDone(actor)
    }
}
