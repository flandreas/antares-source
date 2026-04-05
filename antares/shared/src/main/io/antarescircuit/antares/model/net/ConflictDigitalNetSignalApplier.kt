package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.graph.model.DefaultNetSignalApplier
import io.antarescircuit.jabbah.graph.model.Net

/**
 * The standard Antares [NetSignalApplier<DigitalSignal>] that regards differing signals
 * on the same [Net] as conflicts.
 */
object ConflictDigitalNetSignalApplier : DefaultNetSignalApplier<DigitalSignal>() {

	override fun signalsAreConsistent(a: DigitalSignal?, b: DigitalSignal?): Boolean =
		a?.isConsistentWith(b) ?: false
}