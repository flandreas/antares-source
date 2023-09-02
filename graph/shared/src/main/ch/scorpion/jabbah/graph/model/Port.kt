package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.CombinedNetAccess
import ch.scorpion.jabbah.graph.model.net.NetTopologyChangeListener
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * A [Port] is an object in a [Vertice] to which [Net]s are attached.
 *
 * The value of [Describable] is this [Port]'s purpose within the owning [Vertice].
 *
 * @param T the type of signal that this [Port] can consume or produce.
 */
interface Port<T : Any> : Describable, Bean {

	companion object {
		const val PROP_NAME = "name"
		const val PROP_PORT_TYPE = "portType"
	}

	/** The type of this [Port] regarding signal flow direction.*/
	var portType: PortType

	/** The ID of this [Port] that is unique within the owning [Vertice].*/
	var portId: Int

	/**
	 * The displayable name of this [Port] that can help the user to distinguish different [Port]s in a [Vertice].
	 * Must be unique in [Vertice] unless [Vertice.requireUniquePortNames] is `false`.
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
	 * @param force `true` if [signal] should be used even if its the same as the currently available signal.
	 * Introduced to support resending signals by [NetTopologyChangeListener]s
	 */
	fun setIncomingSignal(signal: T?, signalHandler: SignalHandler, force: Boolean = false)

	/**
	 * Revokes the signal that has previously been set using [setIncomingSignal], giving this
	 * [InputPort] a chance to reset the stored outgoing signal, if this [InputPort] is also
	 * a [OutputPort].
	 *
	 * This is needed when the signal on a connected [Net] is set to "undefined" immediately prior
	 * to setting a new, defined signal by an [OutputPort] in the same [Net]. If [InputPort] would
	 * not reset its stored outgoing signal, chances are the [Net] would NOT propagate the following
	 * defined signal to this [InputPort] because it would differ from the old, still stored signal.
	 */
	fun revokeSignal()
}

interface WeakOutputPortBehaviour<T : Any> {

	/**
	 * Withdraws the defined value from a weak [OutputPort]'s output and replaces it with "undefined".
	 *
	 * @param netSignal the signal about to become active on the net. If a signal in the using application
	 * support multi-part signals, this method withdraws only those parts [port]'s outgoing signal that
	 * correspond with parts of [netSignal] that are not "undefined".
	 */
	fun withdrawWeakOutput(netSignal: T?, port: OutputPort<T>, signalHandler: SignalHandler)

	/**
	 * Activates the defined value of a weak [OutputPort]. This is used when all other [OutputPort]s assert
	 * "undefined" to the [Net].
	 *
	 * @param netSignal the signal about to become active on the net. If a signal in the using application
	 * support multi-part signals, this method sets only those parts of [netSignal] that are "undefined",
	 * and returns the adopted signal.
	 */
	fun activateWeakOutput(netSignal: T?, port: OutputPort<T>, signalHandler: SignalHandler): T

	/**
	 * Called by a [Net] when its signal has changed.
	 */
	fun handleNetChanged(signalHandler: SignalHandler)
}

data class SignalReplacement<T: Any>(
	val signal: T?,
	val originPort: OutputPort<T>
)

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
	 * Is implicitly `true` in this [Port] is also an [InputPort].
	 */
	val canBeUndefined: Boolean get() = portType == PortType.INOUT || customCanBeUndefined

	/**
	 * The explicit value of [canBeUndefined] set by client classes or interactively by the user.
	 */
	var customCanBeUndefined: Boolean

	/** Determines whether all parts of the currently outgoing signal are undefined.*/
	val isOutputFullyUndefined: Boolean

	/** Determines whether at least some parts of the outgoing signal are undefined.*/
	val isOutputPartiallyUndefined: Boolean

	/**
	 * The [CombinedNet] that is the result of [formNet]. Accessible mainly used for testing.
	 */
	val combinedNets: Collection<CombinedNet<T>>

	/** Forms the [CombinedNet] used by this [OutputPort]. */
	fun formNet(signalHandler: SignalHandler)

	fun createAccess(): CombinedNetAccess<T>

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

	/**
	 * Forwards the currently buffered outgoing signal into the connected [Net].
	 * @param force force `true` if [InputPort]s in the [Net] should consume the forwarded signal even if
	 * its the same as the currently available signal.
	 */
	fun flush(signalHandler: SignalHandler, force: Boolean)

	/**
	 * Determines whether this [OutputPort] can be connected with the specified [Net],
	 * allowing to prevent clashes of signals from multiple [OutputPorts][OutputPort] on the same [Net].
	 */
	fun canConnectToNet(net: Net<*>, graphView: GraphView): Boolean =
		graphView.allowMultipleOutputsPerNet || canBeUndefined || net.ports
			.filter { it.portType == PortType.OUTPUT }
			.filterIsInstance<OutputPort<*>>()
			.all { it.customCanBeUndefined }
}

/** A [Port] that can act both as an [InputPort] and as an [OutputPort].*/
interface BidirectionalPort<T : Any> : InputPort<T>, OutputPort<T> {

	/**
	 * Determines for [BidirectionalPort]s with [PortType.INOUT] whether [InputPort] or the [OutputPort]
	 * is dominant for rendering the state of this [BidirectionalPort]
	 */
	var isOutputDominant: Boolean

	/**
	 * Returns for [BidirectionalPort]s with [PortType.INOUT] the relevant signal, which depends
	 * on the current value of [isOutputDominant].
	 */
	val dominantSignal: T
}

/** Enumerates the type of a [Port] regarding signal flow direction.*/
enum class PortType(
	override val customName: String,
	val isInput: Boolean,
	val isOutput: Boolean
) : EnumProperty<PortType> {
	INPUT("input", isInput = true, isOutput = false),
	OUTPUT("output", isInput = false, isOutput = true),
	INOUT("inout", isInput = true, isOutput = true);

	companion object {

		const val BASE_KEY = "graph.property.portType"
		private const val INPUT_KEY = "$BASE_KEY.input"
		private const val OUTPUT_KEY = "$BASE_KEY.output"
		private const val INOUT_KEY = "$BASE_KEY.inout"

		private val LOG by logger(PortType::class)

		fun withName(customName: String): PortType {
			for (type in values()) {
				if (type.customName == customName) {
					return type
				}
			}
			LOG.error("unknown PortType '$customName'")
			throw IllegalArgumentException("unknown PortType '$customName'")
		}
	}

	override fun toString(): String =
		when (this) {
			INPUT -> Translations.getString(INPUT_KEY)
			OUTPUT -> Translations.getString(OUTPUT_KEY)
			INOUT -> Translations.getString(INOUT_KEY)
		}

	fun reverse(): PortType =
		when (this) {
			INPUT -> OUTPUT
			OUTPUT -> INPUT
			INOUT -> INOUT
		}

	fun isCompatibleWith(other: PortType): Boolean =
		this == INOUT || other == INPUT || reverse() == other

	/**
	 * Returns the same as [toString], but in a form suitable for displaying as rich text, e.g. in tooltips.
	 * This representation must escape rich text control characters such as slashes.
	 */
	val richTextName: String get() =
		when (this) {
			INPUT -> Translations.getOptionalString("$INPUT_KEY.rich") ?: Translations.getString(INPUT_KEY)
			OUTPUT -> Translations.getOptionalString("$OUTPUT_KEY.rich") ?: Translations.getString(OUTPUT_KEY)
			INOUT -> Translations.getOptionalString("$INOUT_KEY.rich") ?: Translations.getString(INOUT_KEY)
		}
}