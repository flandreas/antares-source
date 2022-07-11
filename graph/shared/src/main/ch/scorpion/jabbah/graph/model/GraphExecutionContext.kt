package ch.scorpion.jabbah.graph.model

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

	/**
	 * Calculates the resulting signal when applying [signal] coming from [excludePort]
	 * to [net].
	 */
	fun calculateSignal(signal: T?, net: Net<T>, excludePort: OutputPort<T>): T?
}

class DefaultNetSignalApplier<T : Any> : NetSignalApplier<T> {

	override fun signalsAreConsistent(a: T?, b: T?): Boolean =
		SignalUtil.equals(a, b)

	override fun calculateSignal(signal: T?, net: Net<T>, excludePort: OutputPort<T>): T? = signal
}