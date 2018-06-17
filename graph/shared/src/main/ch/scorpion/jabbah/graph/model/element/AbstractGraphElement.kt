package ch.scorpion.jabbah.graph.model.element

import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.execution.actor.*
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.DesignError
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.GraphElementListener
import ch.scorpion.jabbah.io.*

/**
 * Abstract base implementation of the [GraphElement] interface.
 */
abstract class AbstractGraphElement : GraphElement {

    /** Holds all registered [GraphElementListener]s */
    private var listeners: MutableList<GraphElementListener>? = null

    /** Manages [Actor] behaviour on behalf of this [GraphElement].*/
    private val actorSupport: ActorSupport by lazy{ ActorSupport(this) }

    /** ---- [GraphElement] interface */

    override var id: Int = 0

    override val isError: Boolean get() = executionError != null || designError != null

    override var executionError: ExecutionError? = null

    override val designError: DesignError? = null

    override fun accept(visitor: HierarchyVisitor): Boolean {
        return visitor.visit(this)
    }

    override fun addGraphElementListener(l: GraphElementListener) {
        if (listeners == null) {
            listeners = mutableListOf()
        }
        if (!listeners!!.contains(l)) {
            listeners!!.add(l)
        }
    }

    override fun removeGraphElementListener(l: GraphElementListener) {
        if (listeners != null) {
            listeners!!.remove(l)
        }
    }

    override fun bind(repository: MetaGraphRepository, storableCreator: StorableCreator) {
        // empty
    }

    /** ---- [Storable] interface */

    override var storableId: Int = 0

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        // empty
    }

    override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()

    override fun write(writer: StoreWriter) {
        writer.writeInt("id", id)
        writer.writeLong("delay", propagationDelay)
    }

    override fun read(reader: StoreReader) {
        id = reader.readInt("id")
        propagationDelay = reader.readLong("delay")
        // Add an artificial resolution request so that views can request to be resolved AFTER this model
        reader.requestResolution(this, Reference(name = "modelId"))
    }

    /** ---- [Actor] interface */

    private var _state: ActorState = ActorState.NonExecuting

    override var propagationDelay: Long = 0

    override val state: ActorState get() = _state

    override val isBreakpoint: Boolean get() = actorSupport.hasListeners

    override fun addActorListener(l: ActorListener) {
        actorSupport.addListener(l)
    }

    override fun removeActorListener(l: ActorListener) {
        actorSupport.removeListener(l)
    }

    override fun executionStarted(signalHandler: SignalHandler) {
        _state = ActorState.Idle
        executionError = null
        listeners?.forEach { it.executionStarted(signalHandler) }
    }

    override fun act(signalHandler: SignalHandler, data: ActorData): Boolean {
        _state = ActorState.Acting
        return actorSupport.notifyActed(signalHandler, data)
    }

    override fun actingVisualized(signalHandler: SignalHandler, l: ActorListener) {
        actorSupport.actingVisualized(signalHandler, l)
    }

    override fun actingDone(signalHandler: SignalHandler, data: ActorData) {
        _state = ActorState.Idle
    }

    override fun executionStopped(signalHandler: SignalHandler) {
        executionError = null
        listeners?.forEach { it.executionStopped(signalHandler) }
        _state = ActorState.NonExecuting
    }

    /** ---- [AbstractGraphElement] */

    protected fun requestActingAfter(signalHandler: SignalHandler, delay: Long, data: ActorData) {
        _state = ActorState.Waiting
        actorSupport.requestActingAfter(signalHandler, delay, data)
    }

    protected fun requestActingTimeFreeze(signalHandler: SignalHandler, data: ActorData) {
        _state = ActorState.Waiting
        actorSupport.requestActingTimeFreeze(signalHandler, data)
    }

    fun notifyActed(signalHandler: SignalHandler, data: ActorData): Boolean {
        return actorSupport.notifyActed(signalHandler, data)
    }

    /** Notifies all registered [GraphElementListener]s that the state of this [GraphElement] has changed.*/
    protected fun stateChanged(signalHandler: SignalHandler? = null) {
        if (listeners != null) {
            val event = GraphElementEvent(this, signalHandler)
            listeners!!.forEach { it.stateChanged(event) }
        }
    }
}