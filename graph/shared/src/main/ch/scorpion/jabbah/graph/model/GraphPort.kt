package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.io.Storable

/**
 * A [GraphPort] is a [Vertice] whose purpose is to connect a [Graph] (of which the [GraphPort] is a part)
 * with a surrounding [Graph] by forwarding or receiving signals to/from it.
 * Implementations of [GraphPort] will typically have either a single [InputPort] or a single [OutputPort].
 *
 * The name of a [GraphPort] must not be empty.
 *
 * @param T the type of signal handled by this [GraphPort]
 */
interface GraphPort<out T : Any> : Vertice {

	/** The current signal of this [GraphPort].*/
	val signal: T?

	/**
	 * The [PortType] of the single [Port] of this [GraphPort]. Can be changed to support scenarios where the
	 * user can choose whether a [Port] is an input or an output, or both.
	 */
	var portType: PortType
}

/** Gets posted on [EventBus] when the name of a [GraphPort] has changed.*/
data class GraphPortNameChanged<out T : Any>(
	val graphPort: GraphPort<T>,
	val oldName: String?,
	val newName: String?
)

/** Gets posted on [EventBus] when the [PortType] of a [GraphPort] has changed.*/
data class GraphPortTypeChanged<out T : Any>(
	val graphPort: GraphPort<T>,
	val oldPortType: PortType,
	val newPortType: PortType
)

data class GraphPortCanBeUndefinedChanged<out T : Any>(
	val graphPort: GraphPort<T>,
	val value: Boolean
)

/**
 * A [GraphInput] is a special [GraphPort] that can feed a signal from outside into a [Graph].
 * The name of a [GraphInput] must be unique within a [Graph], because the name is used to bind
 * [InputPort]s from outside to this [GraphInput]
 *
 * @param T the type of signal that this [GraphInput] forwards.
 */
interface GraphInput<T : Any> : GraphPort<T> {

	/**
	 * Holds the [SubGraphInputPort] that forwards signal to this [GraphInput]. That [SubGraphInputPort]
	 * belongs to the surrounding [Graph] of this [GraphInput]. The reference to it is not used
	 * for signal propagation, but only for determining whether this [GraphInput] belongs to a top-level
	 * [Graph] or to a [SubGraphVertice]. If it belongs to a [SubGraphVertice], it is not allowed to
	 * change the signal manually. `null` for top-level [Graph]s. Not set before binding.
	 */
	var subGraphInputPort: SubGraphInputPort<T>?

	/** Sets the signal to be forwarded into the [Graph] that owns this [GraphInput].*/
	fun setIncomingSignal(signal: T?, signalHandler: SignalHandler, force: Boolean = false)
}

/**
 * A [GraphOutput] is a special [GraphPort] that can feed a signal from a [Graph] to the outside.
 * The name of a [GraphOutput] must be unique within a [Graph], because the name is used to bind
 * [OutputPort]s from outside to this [GraphOutput]
 * @param T the type of signal that this [GraphOutput] forwards.
 */
interface GraphOutput<T : Any> : GraphPort<T>, NetCombiner {

	/**
	 * Holds the [SubGraphOutputPort] to which this [GraphOutput] forwards signals while execution.
	 * That [SubGraphOutputPort] belongs to the surrounding [Graph] of this [GraphOutput].
	 * During execution, this [GraphOutput] forwards signals to that [SubGraphOutputPort].
	 * `null` for top-level [Graph]s. Not set before binding.
	 */
	var subGraphOutputPort: SubGraphOutputPort<T>?

	/**
	 * Corresponds with [OutputPort.customCanBeUndefined] of this [GraphOutput]'s [SubGraphOutputPort].
	 */
	var customCanBeUndefined: Boolean
}

interface SubGraphPort<T : Any> : Port<T>, Storable {
	fun handleGraphPortChanged(graphPort: GraphPort<*>) {}
}

interface SubGraphInputPort<T : Any> : InputPort<T>, SubGraphPort<T> {

	/** Binds this [SubGraphInputPort] to the [GraphInput] of the sub [Graph] to which it will forward signals.*/
	var graphInput: GraphInput<T>?
}

interface SubGraphOutputPort<T : Any> : OutputPort<T>, SubGraphPort<T> {

	fun propagateSignal(signal: T, signalHandler: SignalHandler)
}
