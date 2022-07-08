package ch.scorpion.antares.model.signal

import ch.scorpion.jabbah.graph.model.Vertice

/**
 * A source of a single [DigitalSignal] whose [BitWidth] can be chosen.
 */
interface DigitalSignalSource : Vertice {

    var bitWidth: BitWidth

    var signal: DigitalSignal?

	val fixedPointConfig: FixedPointConfig?
}