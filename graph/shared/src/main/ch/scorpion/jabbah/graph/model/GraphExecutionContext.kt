package ch.scorpion.jabbah.graph.model

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.execution.SignalHandler

/**
 * Defines an execution context that can be set in [SignalHandler.executionContext]
 * used to detect signal conflicts on [Nets][Net].
 */
data class GraphExecutionContext<T: Any>(
	val netSignalApplier: NetSignalApplier<T> = DefaultNetSignalApplier()
)

interface NetSignalApplier<T : Any> {

	/**
	 * Returns `true` if signals [a] and [b] can be applied onto the same [Net]
	 * by different [OutputPorts][OutputPort] at the same time.
	 */
	fun signalsAreConsistent(a: T?, b: T?): Boolean

	fun replaceOwnUndefinedSignals(outputPort: OutputPort<T>, outgoingSignal: T?, signalHandler: SignalHandler): SignalReplacement<T>
}

open class DefaultNetSignalApplier<T : Any> : NetSignalApplier<T> {

	override fun signalsAreConsistent(a: T?, b: T?): Boolean =
		SignalUtil.equals(a, b)

	override fun replaceOwnUndefinedSignals(
		outputPort: OutputPort<T>,
		outgoingSignal: T?,
		signalHandler: SignalHandler
	): SignalReplacement<T> {

		var replacement = SignalReplacement(outgoingSignal, outputPort)

		// First replace undefined signal with signals from other consistent accesses
		outputPort.combinedNets.forEach { combinedNet ->
			val thisAccess = combinedNet.accessOf(outputPort)!!
			if (thisAccess.isPartiallyUndefined) {
				combinedNet.getConsistentAccess(signalHandler)?.let { consistentAccess ->
					signalHandler.logTrace(System.getClass(this), outputPort.portId) { "withdrawing signal and using signal of consistent Port" }
					replacement = SignalReplacement(
						thisAccess.replaceUndefinedFrom(replacement.signal, consistentAccess.assertedSignal, signalHandler),
						consistentAccess.port)
				}
				combinedNet.revokeSignal()
			}
		}

		// Then replace undefined signals with signals from other weak accesses
		outputPort.combinedNets.forEach { combinedNet ->
			val thisAccess = combinedNet.accessOf(outputPort)!!
			if (thisAccess.isPartiallyUndefined) {
				combinedNet.weakOutputPorts.firstOrNull()?.let { weakPortToActivate ->
					signalHandler.logTrace(System.getClass(this), outputPort.portId) { "forwarding weak signal into net '${outputPort.net!!.id}'" }
					val weakSignal = weakPortToActivate.weakBehaviour!!.activateWeakOutput(thisAccess.assertedSignal, weakPortToActivate, signalHandler)
					replacement = SignalReplacement(
						thisAccess.replaceUndefinedFrom(replacement.signal, weakSignal, signalHandler),
						weakPortToActivate
					)
				}
				combinedNet.revokeSignal()
			}
		}

		return replacement
	}
}