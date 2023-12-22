package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.view.GraphView

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

	/** Returns the current signal of a particular [Port] at the beginning of an execution step.*/
	fun <T : Any> getSignal(portId: Int): T?
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

	override fun <T : Any> getSignal(portId: Int): T? = signal as T?
}