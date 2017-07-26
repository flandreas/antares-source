package ch.scorpion.antares.view.gate

import ch.scorpion.jabbah.graph.model.Port
/**
 * An [AndGateDataPort] designates a [Port] of a 2-input [AndGateView] to be the one that forwards the
 * main data flow, while the other [Port] being the control [Port]. If set, the [AndGateView] visualizes this
 * by drawing a line from the data [Port] to the output [Port]. This can be useful and informative for
 * circuits like multiplexers or shifters, where data flow control is the most relevant aspect of the circuit.
 */
enum class AndGateDataPort {
    /** No data port behaviour */
    NONE,
    /** Designates the [Port] with ID 1 as the data [Port] */
    ONE,
    /** Designates the [Port] with ID 2 as the data [Port] */
    TWO
}