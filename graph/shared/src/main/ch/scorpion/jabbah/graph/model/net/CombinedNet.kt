package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*

/**
 * Represents a [Vertice] that combines the [Net]s connected to its [InputPort]s with
 * the [Net]s connected to its [OutputPort], thus forming a [CombinedNet] in terms of
 * signal propagation conflict resolution.
 */
interface NetCombiner: Vertice {

	/**
	 * Returns all [SignalPropagationChain]s that connect [inputPort] with the destination [OutputPort]s
	 * of the returned [SignalPropagationChain]s.
	 */
	fun <T : Any> getSignalPropagationChains(inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<SignalPropagationChain<T>>
}

/**
 * A collection of all [OutputPort]s whose outgoing signals might be in conflict with the signal
 * that an origin [OutputPort] is about to assert to the [Net] it is connected to.
 * The [Net]s these [OutputPort]s are connected to form a "combined [Net]" in which new signals
 * are negotiated, including [WeakOutputPortBehaviour] and recovering from [Net] error states.
 *
 * Evaluation of [CombinedNet]s only considers [OutputPort]s that can produce undefined signals
 * as defined by [OutputPort.canBeUndefined]. Other [OutputPort]s cannot be connected to the same [Net],
 * which is a restriction that must be enforced by other parts of the system.
 *
 * [CombinedNet]s can span multiple [NetCombiner]s that have propagation delay 0 and are therefore
 * executed within the same simulation slot.
 *
 * Signals might be transformed while traveling along the stages of a [CombinedNet], which is why
 * a destination [OutputPort] is associated with a chain of [SignalConverter]s that represent
 * these transformations.
 */
class CombinedNet<T : Any>
	private constructor(originOutputPort: OutputPort<T>, signalHandler: SignalHandler)
{

	companion object {

		fun <T: Any> fromOutputPort(originOutputPort: OutputPort<T>, signalHandler: SignalHandler): CombinedNet<T> {
			return CombinedNet(originOutputPort, signalHandler)
		}

		fun <T : Any> createChains(originOutputPort: OutputPort<T>, signalHandler: SignalHandler): Collection<SignalPropagationChain<T>> {
			val createdChains = mutableListOf<SignalPropagationChain<T>>()
			if (originOutputPort.net == null) {
				return createdChains
			}
			originOutputPort.net!!.ports
				.filter { it !== originOutputPort }
				.forEach {
					if (it.portType.isOutput) {
						createdChains.add(SignalPropagationChain(it as OutputPort<T>))
					}
					if (it.portType.isInput && it.owner is NetCombiner) {
						createdChains.addAll((it.owner as NetCombiner).getSignalPropagationChains(it as InputPort<T>, signalHandler))
					}
				}

			return createdChains
		}
	}

	/** There might lead multiple different [SignalPropagationChain]s to the same [OutputPort].*/
	val chains: Collection<SignalPropagationChain<T>> = createChains(originOutputPort, signalHandler)

	val outputPorts: Collection<OutputPort<T>> get() = chains.map { it.destinationOutputPort }

	fun getChainTo(outputPort: OutputPort<T>): SignalPropagationChain<T>?
		= chains.find { it.destinationOutputPort === outputPort }

	/**
	 * Returns the single [OutputPort] of this [CombinedNet] that produces a defined, non-weak signal, if any.
	 * Returns `null` if there are no or multiple such [OutputPort]s.
	 */
	val consistentSignalPort: OutputPort<*>? get() {
		var consistentPort: OutputPort<*>? = null
		chains
			.forEach {
				if (it.destinationOutputPort.weakBehaviour == null && !it.destinationOutputPort.isOutputFullyUndefined) {
					if (consistentPort == null) {
						consistentPort = it.destinationOutputPort
					} else {
						if (!it.isConsistentWith(consistentPort!!.getOutgoingSignal() as T?)) {
							return null
						}
					}
				}
			}
		return consistentPort
	}

	/**
	 * Determines whether all [OutputPort]s of this [CombinedNet] are consistent with the current
	 * output value of the specified [originOutputPort].
	 */
	fun isConsistentWith(originOutputPort: OutputPort<T>): Boolean {
		if (originOutputPort.isOutputFullyUndefined) {
			return true
		}
		val signal = originOutputPort.getOutgoingSignal()
		return chains.none { !it.isConsistentWith(signal) }
	}

	fun setExecutionError(error: ExecutionError?) {
		chains.forEach { it.setExecutionError(error) }
	}
}