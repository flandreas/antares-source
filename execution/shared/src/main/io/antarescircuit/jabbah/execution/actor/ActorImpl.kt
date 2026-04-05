package io.antarescircuit.jabbah.execution.actor

import io.antarescircuit.jabbah.base.LongValue
import io.antarescircuit.jabbah.base.LongValueImpl
import io.antarescircuit.jabbah.execution.ExecutionError
import io.antarescircuit.jabbah.execution.SignalHandler

open class ActorImpl(
    override val id: Int = 0,
    propagationDelay: Long = 0
) : Actor {

    /** Manages [Actor] behaviour on behalf of this [Actor].*/
    private val actorSupport: ActorSupport by lazy { ActorSupport(this) }

    protected var executionRunning: Boolean = false
        private set

    private var _state: ActorState = ActorState.NonExecuting

    /** ---- [Actor] interface */

    override var propagationDelay: LongValue = LongValueImpl(propagationDelay)
        set(value) {
            require(value.value >= 0) { "Propagation delay must be greater than 0" }
            field = value
        }

    override var executionError: ExecutionError? = null

    override val state: ActorState get() = _state

    override val isBreakpoint: Boolean get() = actorSupport.hasListeners

    override fun addActorListener(l: ActorListener) {
        actorSupport.addListener(l)
    }

    override fun removeActorListener(l: ActorListener) {
        actorSupport.removeListener(l)
    }

    override fun executionInitialize(signalHandler: SignalHandler) {
        executionRunning = true
    }

    override fun executionStart(signalHandler: SignalHandler) {
        _state = ActorState.Idle
        executionError = null
    }

    override fun act(signalHandler: SignalHandler, data: ActorData) {
        _state = ActorState.Acting
        actorSupport.notifyActed(signalHandler, data)
    }

    override fun actingVisualized(signalHandler: SignalHandler, l: ActorListener, data: ActorData?) {
        actorSupport.actingVisualized(signalHandler, l, data)
    }

    override fun actingDone(signalHandler: SignalHandler, data: ActorData?) {
        _state = ActorState.Idle
    }

    override fun executionStopped(signalHandler: SignalHandler) {
        executionError = null
        _state = ActorState.NonExecuting
        executionRunning = false
    }

    /** ---- [ActorImpl] */

    fun requestActingAfter(signalHandler: SignalHandler, delay: Long, data: ActorData) {
        _state = ActorState.Waiting
        actorSupport.requestActingAfter(signalHandler, delay, data)
    }

    fun notifyActed(signalHandler: SignalHandler, data: ActorData) {
        actorSupport.notifyActed(signalHandler, data)
    }
}