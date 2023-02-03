package ch.scorpion.antares.model.inout

import ch.scorpion.antares.model.input.Switch
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.signal.DigitalSignalSource
import ch.scorpion.jabbah.execution.SignalHandler

interface DigitalCircuitInOut : CircuitInOut<DigitalSignal>, DigitalSignalSource {

    var signalRepresentation: DigitalSignalRepresentation

	/**
	 * Toggles the bit at the specified index.
	 * This method is typically used by the UI and should use a propagation delay that is similar to the one
	 * used by a [Switch].
	 */
	fun toggleBit(index: Int, undefine: Boolean, signalHandler: SignalHandler)
}

/** Notifies the change of the [BitWidth] of a [DigitalCircuitInOut].*/
class DigitalCircuitInOutBitWidthChanged(
	val circuitInOut: DigitalCircuitInOut,
	val oldValue: BitWidth,
	val newValue: BitWidth
)

/** Notifies the change of the [DigitalSignalRepresentation] of a [DigitalCircuitInOut].*/
class DigitalCircuitInOutSignalRepresentationChanged(
	val circuitInOut: DigitalCircuitInOut,
	val oldValue: DigitalSignalRepresentation,
	val newValue: DigitalSignalRepresentation
)