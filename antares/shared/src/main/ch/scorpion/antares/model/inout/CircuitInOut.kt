package ch.scorpion.antares.model.inout

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.signal.DigitalSignalSource
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.vertice.InteractableVertice

/**
 * A [Vertice] that can feed a [DigitalSignal] into a circuit [Graph] and forward it to the outside of a [Graph].
 *
 * On a conceptual level, a [CircuitInOut] can be seen as a [Port] of an entire [Graph], although it
 * is a [Vertice] that contains a [Port] with a particular [PortType].
 */
interface CircuitInOut : InteractableVertice<DigitalSignal>, BidirectionalGraphPort<DigitalSignal>, DigitalSignalSource {

    /**
     * Determines whether this [CircuitInOut] belongs to a top-level [Graph]. Manually setting the input
     * value is only allowed for top-level [CircuitInOut]s.
     */
    val isToplevel: Boolean

    var signalRepresentation: DigitalSignalRepresentation

	/**
	 * Toggles the bit at the specified index.
	 * This method is typically used by the UI and should use a propagation delay that is similar to the one
	 * used by a [Switch].
	 */
	fun toggleBit(index: Int, undefine: Boolean, signalHandler: SignalHandler)

	/**
	 * Sets the new signal entered manually by the user.
	 * This method is typically used by the UI and should use a propagation delay that is similar to the one
	 * used by a [Switch].
	 */
	fun setSignalManually(signal: DigitalSignal, signalHandler: SignalHandler)
}

/** Notifies the change of the [BitWidth] of a [CircuitInOut].*/
class CircuitInOutBitWidthChanged(
    val circuitInOut: CircuitInOut,
    val oldValue: BitWidth,
    val newValue: BitWidth
)

/** Notifies the change of the [DigitalSignalRepresentation] of a [CircuitInOut].*/
class CircuitInOutSignalRepresentationChanged(
    val circuitInOut: CircuitInOut,
    val oldValue: DigitalSignalRepresentation,
    val newValue: DigitalSignalRepresentation
)