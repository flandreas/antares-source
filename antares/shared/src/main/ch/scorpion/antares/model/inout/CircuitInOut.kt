package ch.scorpion.antares.model.inout

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.model.signal.DigitalSignalSource
import ch.scorpion.jabbah.graph.model.BidirectionalGraphPort
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.PortType
import ch.scorpion.jabbah.graph.model.vertice.InteractableVertice

/**
 * A [Vertice] that can feed a [DigitalSignal] into a circuit [Graph] and forward it to the outside of a [Graph].
 *
 * On a conceptual level, a [CircuitInOut] can be seen as a [Port] of an entire [Graph], although it
 * is a [Vertice] that contains a [Port] with a particular [PortType].
 */
interface CircuitInOut : InteractableVertice, BidirectionalGraphPort<DigitalSignal>, DigitalSignalSource {

    /**
     * Determines whether this [CircuitInOut] belongs to a top-level [Graph]. Manually setting the input
     * value is only allowed for top-level [CircuitInOut]s.
     */
    val isToplevel: Boolean

    var signalRepresentation: DigitalSignalRepresentation
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