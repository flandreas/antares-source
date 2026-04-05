package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.*

/**
 * Performs a "Wired OR" function when applying multiple [DigitalSignals][DigitalSignal]
 * to the same [Net].
 */
object WiredOrNetSignalApplier : DefaultNetSignalApplier<DigitalSignal>() {

	override fun signalsAreConsistent(a: DigitalSignal?, b: DigitalSignal?): Boolean {
		if (a == null || b == null) {
			return true
		}
		if (a.hasError || b.hasError) {
			return a == b
		}
		return a.bitWidth == b.bitWidth
	}

	override fun replaceOwnUndefinedSignals(
		outputPort: OutputPort<DigitalSignal>,
		outgoingSignal: DigitalSignal?,
		signalHandler: SignalHandler
	): SignalReplacement<DigitalSignal> {
		var replacement = SignalReplacement(outgoingSignal, outputPort)

		if (outputPort.net == null) {
			return replacement
		}

		var s = outgoingSignal ?: DigitalSignalFactory.undefined((outputPort as DigitalPort).bitWidth)

		// First replace undefined signal with signals from other consistent accesses
		outputPort.combinedNets.forEach { combinedNet ->
			combinedNet.accesses.forEach { access ->
				if (access.port !== outputPort && access.port.getOutgoingSignal() != null) {
					s = s.or(access.port.getOutgoingSignal()!!)
				}
			}
		}

		return SignalReplacement(s, outputPort)
	}
}