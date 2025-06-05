package ch.scorpion.antares.model.signal

import ch.scorpion.antares.model.vertice.AdjustableBitWidth

/**
 * A source of a single [DigitalSignal] whose [BitWidth] can be chosen.
 */
interface DigitalSignalSource : AdjustableBitWidth {

    var bitWidth: BitWidth

    var signal: DigitalSignal?

	val fixedPointConfig: FixedPointConfig?
}