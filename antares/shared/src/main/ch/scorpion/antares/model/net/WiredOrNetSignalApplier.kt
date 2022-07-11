package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.model.Net

/**
 * Performs a "Wired OR" function when applying multiple [DigitalSignals][DigitalSignal]
 * to the same [Net].
 */
object WiredOrNetSignalApplier : DigitalNetSignalApplier {

	override fun signalsAreConsistent(a: DigitalSignal?, b: DigitalSignal?): Boolean {
		if (a == null || b == null) {
			return true
		}
		if (a.hasError || b.hasError) {
			return a == b
		}
		return a.bitWidth == b.bitWidth
	}

	override fun calculateSignal(signal: DigitalSignal?, netSignal: DigitalSignal?): DigitalSignal? {
		if (signal == null) {
			return null
		}
		if (netSignal == null) {
			return signal
		}
		return netSignal.or(signal)
	}
}