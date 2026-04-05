package io.antarescircuit.antares.model.signal

import io.antarescircuit.antares.model.vertice.AdjustableBitWidth

/**
 * A source of a single [DigitalSignal] whose [BitWidth] can be chosen.
 */
interface DigitalSignalSource : AdjustableBitWidth {

    var bitWidth: BitWidth

    var signal: DigitalSignal?

	val fixedPointConfig: FixedPointConfig?
}