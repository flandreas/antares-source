package ch.scorpion.antares.model.net

import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*

/**
 * Performs a "Wired OR" function when applying multiple [DigitalSignals][DigitalSignal]
 * to the same [Net].
 */
object WiredOrNetSignalApplier : NetSignalApplier<DigitalSignal> {

	override fun signalsAreConsistent(a: DigitalSignal?, b: DigitalSignal?): Boolean {
		if (a == null || b == null) {
			return true
		}
		if (a.hasError || b.hasError) {
			return a == b
		}
		return a.bitWidth == b.bitWidth
	}

	override fun calculateSignal(
		signal: DigitalSignal?,
		net: Net<DigitalSignal>,
		excludePort: OutputPort<DigitalSignal>
	): DigitalSignal {
		var result = signal ?: DigitalSignalFactory.undefined((excludePort as DigitalPort).bitWidth)
		net.ports
			.filter { it is OutputPort<*> && it !== excludePort }
			.map { it as OutputPort<DigitalSignal> }
			.forEach { port ->
				if (port !is BidirectionalPort<*> || (port as BidirectionalPort<*>).isOutputDominant) {
					port.getOutgoingSignal()?.let {
						result = result.or(it)
					}
				}
			}
		return result
	}

	override fun replaceOwnUndefinedSignals(
		outputPort: OutputPort<DigitalSignal>,
		outgoingSignal: DigitalSignal?,
		signalHandler: SignalHandler
	): SignalReplacement<DigitalSignal> {
		var replacement = SignalReplacement(outgoingSignal, outputPort)

		if (outputPort.net == null || outgoingSignal?.isPartiallyUndefined == false) {
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