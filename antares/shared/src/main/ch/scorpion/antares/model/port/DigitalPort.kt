package ch.scorpion.antares.model.port

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.OutputPort

/**
 * Represents a [Port] that produces or consumes [DigitalSignal]s.
 */
interface DigitalPort : InputPort<DigitalSignal>, OutputPort<DigitalSignal> {

    companion object {
        val PROP_LOGIC = "logic"
        val PROP_BIT_WIDTH = "bitWidth"
        val PROP_SIGNAL_REPRESENTATION = "signalRepresentation"
        val PROP_TRIGGER = "trigger"
    }

    /** Holds the type of [Logic] of this [DigitalPort].*/
    var logic: Logic

    /** Holds the number of parallel [DigitalSignal]s that this [DigitalPort] supports.*/
    var bitWidth: BitWidth

    var trigger: Trigger

    var signalRepresentation: DigitalSignalRepresentation

    val defaultDigitalSignal: DigitalSignal

    /**
     * Determines for [DigitalPort]s with [PortType.INOUT] whether [InputPort] or the [OutputPort]
     * is dominant for rendering the state of this [DigitalPort]
     */
    var isOutputDominant: Boolean

    /**
     * Returns for [DigitalPort]s with [PortType.INOUT] the relevant [DigitalSignal], which depends
     * on the current value of [isOutputDominant].
     */
    val dominantSignal: DigitalSignal

}