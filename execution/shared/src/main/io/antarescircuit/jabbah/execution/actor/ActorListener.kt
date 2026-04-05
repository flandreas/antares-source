package io.antarescircuit.jabbah.execution.actor

import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.execution.scheduler.Scheduler

/**
 * Can be added to an [Actor] in order to be called at important moments during an execution cycle, and to
 * drive the execution process through its various steps.
 *
 * [ActorListener]s have originally been invented to support view-related objects that render the animation
 * state using asynchronous processes. The interface supports a mechanism that allows an [Actor] to end an
 * execution cycle not before all its [ActorListener]s have completed their rendering.
 */
interface ActorListener {

    /**
     * Called by the [Actor] being listened to after the [Actor] has requested acting at the
     * [SignalHandler]. A view-related [ActorListener] might implement this method in order to setup and
     * prepare an execution animation.
     */
    fun actingRequested(actor: Actor, signalHandler: SignalHandler, data: ActorData)

    /**
     * Called by the [Actor] being listened to after the [Actor] has been acting as initiated by the [Scheduler].
     *
     * A view-related [ActorListener] might implement this method in order to perform an execution animation.
     * After the execution animation has (asynchronously) ended, this [ActorListener] should
     * call [Actor.actingVisualized]. The [Actor] waits until all registered [ActorListener] have called
     * [Actor.actingVisualized], and then continues by calling [SignalHandler.actingDone].
     */
    fun acted(actor: Actor, signalHandler: SignalHandler, data: ActorData)
}
