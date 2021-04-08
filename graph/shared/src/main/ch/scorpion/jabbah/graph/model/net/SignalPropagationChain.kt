package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.OutputPort

/**
 * Propagates a signal through a chain of [SignalConverter]s to a destination
 * [OutputPort].
 */
class SignalPropagationChain<T : Any>(
	val destinationOutputPort: OutputPort<T>
) {

	companion object {
		private val LOG by logger(SignalPropagationChain::class)
	}

	/** If empty, no signal conversion takes place.*/
	val converters = mutableListOf<SignalConverter<T>>()

	/** The [Net]s visited by propagating signals. Uses for setting [ExecutionError]s. */
	private val nets = mutableSetOf<Net<T>>()

	init {
		destinationOutputPort.net?.let { nets.add(it) }
	}

	/**
	 * Extends this [SignalPropagationChain] by adding an optional [SignalConverter] to the head
	 * of the existing chain and registering the [Net]s of the specified [InputPort] and [OutputPort].
	 */
	fun extendHead(converter: SignalConverter<T>?, inputPort: InputPort<T>, outputPort: OutputPort<T>) {
		converter?.let { converters.add(0, it) }
		inputPort.net?.let { nets.add(it) }
		outputPort.net?.let { nets.add(it) }
	}

	/**
	 * Checks if converting [signal] by all [SignalConverter]s results in a value
	 * that is consistent with the value currently outgoing at [destinationOutputPort].
	 */
	fun isConsistentWith(signal: T?): Boolean {
		try {
			val convertedSignal = convertSignal(signal)
			return destinationOutputPort.isOutgoingSignalConsistentWith(convertedSignal)
		} catch (e: Throwable) {
			logError()
			throw e
		}
	}

	fun setExecutionError(error: ExecutionError?) {
		nets.forEach { it.executionError = error }
	}

	private fun logError() {
		LOG.error("SignalPropagationChain for port ${destinationOutputPort.portId} in ${destinationOutputPort.owner?.id}")
		converters.reversed().forEach {
			LOG.error("-> $it")
		}
	}

	private fun convertSignal(signal: T?): T? {
		var s = signal
		converters.forEach { s = it.convert(s) }
		return s
	}
}