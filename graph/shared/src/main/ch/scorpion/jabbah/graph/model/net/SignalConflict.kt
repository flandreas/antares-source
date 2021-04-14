package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.graph.model.OutputPort

/**
 * Represents a conflict of signals clashing while trying to forward a signal
 * along a [SignalPropagationChain].
 */
data class SignalConflict<T: Any>(
	val convertedSignal: T?,
	val destinationOutputPort: OutputPort<T>
)