package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice

/**
 * A [GraphPort] is a [Vertice] whose purpose is to connect a [Graph] (of which the [GraphPort] is a part)
 * with a surrounding [Graph] by forwarding or receiving signals to/from it.
 * Implementations of [GraphPort] will typically have either a single [InputPort] or a single [OutputPort].
 *
 * @param T the type of signal handled by this [GraphPort]
 */
interface GraphPort<out T: Any> : Vertice {

    /** The current signal of this [GraphPort].*/
    val signal: T?

    /**
     * The [PortType] of the single [Port] of this [GraphPort]. Can be changed to support scenarios where the
     * user can choose whether a [Port] is an input or an output, or both.
     */
    var portType: PortType

    /** Corresponds with [Port.description] of the single [Port] of this [GraphPort].*/
    var portDescription: String?
        get() {
            val port: Port<T> = getPort()
            return port.description
        }
        set(value) {
            val port: Port<T> = getPort()
            port.description = value
        }
}

/** Gets posted on [EventBus] when the name of a [GraphPort] changes.*/
data class GraphPortNameChanged<out T: Any>(
    val graphPort: GraphPort<T>,
    val oldName: String?,
    val newName: String?
)

/**
 * A [GraphInput] is a special [GraphPort] that can feed a signal from outside into a [Graph].
 * The name of a [GraphInput] must be unique within a [Graph], because the name is used to bind
 * [InputPort]s from outside to this [GraphInput]
 *
 * @param T the type of signal that this {@link GraphInput} forwards.
 */
interface GraphInput<T: Any> : GraphPort<T> {

    /**
     * Holds the [SubGraphInputPort] that forwards signal to this [GraphInput]. That [SubGraphInputPort]
     * belongs to the surrounding [Graph] of this [GraphInput]. The reference to it is not used
     * for signal propagation, but only for determining whether this [GraphInput] belongs to a top-level
     * [Graph] or to a [SubGraphVertice]. If it belongs to a [SubGraphVertice], it is not allowed to
     * change the signal manually. `null` for top-level [Graph]s. Not set before binding.
     */
    var subGraphInputPort: SubGraphInputPort<T>?

    /** Sets the signal to be forwarded into the [Graph] that owns this [GraphInput].*/
    fun setIncomingSignal(signal: T?, signalHandler: SignalHandler)
}

/**
 * A [GraphOutput] is a special [GraphPort] that can feed a signal from a [Graph] to the outside.
 * The name of a [GraphOutput] must be unique within a [Graph], because the name is used to bind
 * [OutputPort]s from outside to this [GraphOutput]
 * @param T the type of signal that this [GraphOutput] forwards.
 */
interface GraphOutput<T: Any> : GraphPort<T> {

    /**
     * Sets the [SubGraphOutputPort] to which this [GraphOutput] forwards signals while execution.
     * Not set before binding.
     */
    fun setSubGraphOutputPort(port: SubGraphOutputPort<T>)
}

interface SubGraphPort<T: Any> : Port<T>, Storable

interface SubGraphInputPort<T: Any> : InputPort<T>, SubGraphPort<T> {

    /** Binds this [SubGraphInputPort] to the [GraphInput] of the sub [Graph] to which it will forward signals.*/
    var graphInput: GraphInput<T>?
}

interface SubGraphOutputPort<T: Any> : OutputPort<T>, SubGraphPort<T>
