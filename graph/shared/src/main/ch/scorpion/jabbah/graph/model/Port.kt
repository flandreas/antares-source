package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.description.Describable
import kotlin.reflect.KClass

/**
 * A [Port] is an object in a [Vertice] to which [Net]s are attached.
 *
 * The value of [Describable] is this [Port]'s purpose within the owning [Vertice].
 *
 * @param T the type of signal that this [Port] can consume or produce.
 */
interface Port<T : Any> : Describable {

	companion object {
		const val PROP_NAME = "name"
		const val PROP_PORT_TYPE = "portType"
	}

	/** The type of this [Port] regarding signal flow direction.*/
	var portType: PortType

	/** The ID of this [Port] that is unique within the owning [Vertice].*/
	var portId: Int

	/** The class of signal this [Port] can handle. Used to support type inference at runtime.*/
	val signalClass: KClass<T>?

	/**
	 * The displayable name of this [Port] that can help the user to distinguish different [Port]s in a [Vertice].
	 */
	var name: String?

	/** The [Vertice] that owns this [Port], i.e. of which this [Port] is a part of.*/
	var owner: Vertice?

	/** The [Net] to which this [Port] is connected.*/
	val net: Net<T>?

	/** Determines whether this [Port] is currently connected to a [Net].*/
	val isConnected: Boolean get() = net != null

	fun accept(visitor: HierarchyVisitor)

	fun addPropertyChangeListener(l: PropertyChangeListener<Any>)

	fun removePropertyChangeListener(l: PropertyChangeListener<Any>)

	/**
	 * Connects this [Port] to the specified [Net].
	 * @throws IllegalStateException if this [Port] is already connected with a [Net].
	 */
	fun connectTo(net: Net<T>)

	/** Disconnects this [Port] from the [Net] to which it is currently connected, if any. */
	fun disconnect()

	/** Called by the execution environment to allow this [Port] to initialize its state after execution has been started.*/
	fun executionStarted(signalHandler: SignalHandler)

	/** Called by the execution environment to allow this [Port] to cleanup its state after execution has been stopped.*/
	fun executionStopped(signalHandler: SignalHandler)
}

/**
 * An [InputPort] is a [Port] in a [Vertice] that consumes signals of a particular type for a
 * connected [Net] and forwards them into its [Vertice], which triggers recalculation of the [Vertice]'s
 * [OutputPort]s.
 *
 * @param T the type of signal accepted by this [InputPort]
 */
interface InputPort<T : Any> : Port<T> {

	val incomingSignalDescription: String? get() = getIncomingSignal()?.toString()

	fun getIncomingSignal(): T?

	/**
	 * Sets the current signal of this [InputPort].
	 * Typically called by incoming [Net]s. Should notify the owning [Vertice], which will eventually lead to
	 * recalculation of the [Vertice]'s [OutputPort]'s values.
	 *
	 * @param signal the signal to set.
	 * @param signalHandler the [SignalHandler] to be used during execution.
	 */
	fun setIncomingSignal(signal: T?, signalHandler: SignalHandler)
}

interface WeakOutputPortBehaviour<T : Any> {

	/** Withdraws the defined value from a weak [OutputPort]'s output and replaces it with "undefined".*/
	fun withdrawWeakOutput(port: OutputPort<T>, signalHandler: SignalHandler)

	/**
	 * Activates the defined value of a weak [OutputPort]. This is used when all other [OutputPort]s assert
	 * "undefined" to the [Net].
	 *
	 * @param netSignal the signal about to become active on the net. If a signal in the using application
	 * support multi-part signals, this method sets only those parts of [netSignal] that are "undefined",
	 * and returns the adopted signal.
	 */
	fun activateWeakOutput(netSignal: T?, port: OutputPort<T>, signalHandler: SignalHandler): T
}

/**
 * An [OutputPort] is a [Port] in a [Vertice] that forwards signal produced by its [Vertice] to a
 * connected [Net].
 *
 * @param T the type of signal forwarded by this [OutputPort]
 */
interface OutputPort<T : Any> : Port<T> {

	/**
	 * Multiple [OutputPort]s can only be connected to the same [Net] if at most
	 * one of them cannot be undefined.
	 * Writable only to be set after deserialization depending on [PortType].
	 */
	var canBeUndefined: Boolean

	/** Determines whether all parts of the currently outgoing signal are undefined.*/
	val isOutputFullyUndefined: Boolean

	/** Determines whether at least some parts of the outgoing signal are undefined.*/
	val isOutputPartiallyUndefined: Boolean

	/**
	 * The specified [signal] is considered consistent with the current outgoing signal
	 * of this [OutputPort] if they don't conflict, which generally requires that they
	 * are equal, but depending on the application, this can also be fulfilled if parts
	 * of the signals are undefined.
	 */
	fun isOutgoingSignalConsistentWith(signal: T?): Boolean

	/**
	 * Set with the desired behaviour if this [OutputPort] supports "weak signals".
	 * A weak [OutputPort] asserts its output signal to the [Net] it is connected to
	 * only if the [Net]'s signal is undefined. Otherwise, it asserts "undefined" to the [Net].
	 */
	val weakBehaviour: WeakOutputPortBehaviour<T>?

	val outgoingSignalDescription: String? get() = getOutgoingSignal()?.toString()

	fun getOutgoingSignal(): T?

	/**
	 * Sets the buffered signal of this [OutputPort], but doesn't forward it yet to a connected [Net].
	 * Typically called only by the [Vertice] that contains this [OutputPort].
	 */
	fun setOutgoingSignalBuffered(signal: T?, signalHandler: SignalHandler)

	/**
	 * Sets the signal of this [OutputPort] and forwards it to a connected [Net], if any.
	 */
	fun setOutgoingSignal(signal: T?, signalHandler: SignalHandler)

	fun flush(signalHandler: SignalHandler)
}

/** A [Port] that can act both as an [InputPort] and as an [OutputPort].*/
interface BidirectionalPort<T : Any> : InputPort<T>, OutputPort<T>

/** Enumerates the type of a [Port] regarding signal flow direction.*/
enum class PortType(val customName: String) {
	INPUT("input"),
	OUTPUT("output"),
	INOUT("inout");

	companion object {
		private val LOG by logger(PortType::class)

		fun withName(customName: String): PortType {
			for (type in PortType.values()) {
				if (type.customName == customName) {
					return type
				}
			}
			PortType.LOG.error("unknown PortType '$customName'")
			throw IllegalArgumentException("unknown PortType '$customName'")
		}
	}

	override fun toString(): String {
		return when (this) {
			INPUT -> Translations.getString("graph.property.portType.input")
			OUTPUT -> Translations.getString("graph.property.portType.output")
			INOUT -> Translations.getString("graph.property.portType.inout")
		}
	}

	fun reverse(): PortType {
		return when (this) {
			INPUT -> OUTPUT
			OUTPUT -> INPUT
			INOUT -> INOUT
		}
	}

	fun isCompatibleWith(other: PortType): Boolean {
		return this == INOUT || other == INPUT || reverse() == other
	}

	val isInput: Boolean get() = this == INPUT || this == INOUT
	val isOutput: Boolean get() = this == OUTPUT || this == INOUT

}