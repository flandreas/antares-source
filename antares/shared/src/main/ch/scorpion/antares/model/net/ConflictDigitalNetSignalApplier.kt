package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.model.DefaultNetSignalApplier
import ch.scorpion.jabbah.graph.model.Net

/**
 * The standard Antares [NetSignalApplier<DigitalSignal>] that regards differing signals
 * on the same [Net] as conflicts.
 */
object ConflictDigitalNetSignalApplier : DefaultNetSignalApplier<DigitalSignal>() {

	override fun signalsAreConsistent(a: DigitalSignal?, b: DigitalSignal?): Boolean =
		a?.isConsistentWith(b) ?: false
}