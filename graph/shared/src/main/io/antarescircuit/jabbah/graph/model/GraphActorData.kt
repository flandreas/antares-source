package io.antarescircuit.jabbah.graph.model

import io.antarescircuit.jabbah.execution.actor.ActorData

/**
 * An [ActorData] used for graph related actors that remembers the [Port] that has changed.
 */
interface GraphActorData : ActorData {

	/**
	 * Returns the [Port] whose signal change triggered the scheduling of a [Vertice]. Can be `null`
	 * if the change wasn't triggered by a [Port] (for example in case of a switch that has no input),
	 * or if the [Vertice] is executed at simulation startup.
	 */
	val changedPort: Port<*>?

	/** Determines whether the signal change occurred on an input.*/
	val isInput: Boolean

	/**
	 * The [OutputPort] that sends this [GraphActorData] into a [Graph]. Usually the same as [changedPort],
	 * but might be different if the originating [changedPort] lies behind a [SubGraphPort]. UI related
	 * classes will use [immediatePort] to find UI representations of objects to which this [immediatePort] belongs.
	 */
	val immediatePort: Port<*>?

	val force: Boolean

	/** Returns a [Port]'s signal, sampled on arrival or read live according to the implementation. */
	fun <T : Any> getSignal(portId: Int): T?
}

/** Captures all inputs together when a scripted component receives an input event requiring sampling. */
class SampledGraphActorData(
	override val changedPort: InputPort<*>,
	inputs: List<InputPort<*>>,
	override val force: Boolean = false,
) : GraphActorData {

	private val signals = inputs.associate { it.portId to it.getIncomingSignal() }

	override val isInput: Boolean get() = true
	override val immediatePort: Port<*> get() = changedPort

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> getSignal(portId: Int): T? = signals[portId] as T?

	override fun dataToString(): String = signals.toString()
}

/** A [GraphActorData] implementation that stores the single [Port] signal.*/
class StoringGraphActorData(
	override val changedPort: Port<*>?,
	val signal: Any?,
	override val isInput: Boolean = true,
	override val immediatePort: Port<*>? = changedPort,
	override val force: Boolean = false
) : GraphActorData {

	override fun dataToString(): String = "${changedPort?.name}:$signal"

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> getSignal(portId: Int): T? = signal as T?
}
