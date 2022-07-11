package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.NetSignalApplier
import ch.scorpion.jabbah.graph.model.OutputPort

interface DigitalNetSignalApplier : NetSignalApplier<DigitalSignal>

/**
 * The standard Antares [DigitalNetSignalApplier] that regards differing signals
 * on the same [Net] as conflicts.
 */
object ConflictDigitalNetSignalApplier : DigitalNetSignalApplier {

	override fun signalsAreConsistent(a: DigitalSignal?, b: DigitalSignal?): Boolean =
		a?.isConsistentWith(b) ?: false

	override fun calculateSignal(
		signal: DigitalSignal?,
		net: Net<DigitalSignal>,
		excludePort: OutputPort<DigitalSignal>
	): DigitalSignal? = signal
}