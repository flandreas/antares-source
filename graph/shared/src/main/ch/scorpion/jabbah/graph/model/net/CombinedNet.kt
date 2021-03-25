package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.SignalUtil
import ch.scorpion.jabbah.graph.model.WeakOutputPortBehaviour

/**
 * A [CombinedNet] is a collection of all [OutputPort]s whose outgoing signals might be in conflict
 * with the signal that an origin [OutputPort] is about to assert to the [Net] it is connected to.
 * The [Net]s to which these [OutputPort]s are connected form a combined [Net] in which new signals
 * are negotiated, including [WeakOutputPortBehaviour] and recovering from [Net] error states.
 *
 * Evaluation of [CombinedNet]s only considers [OutputPort]s that can produce undefined signals
 * as defined by [OutputPort.canBeUndefined]. Other [OutputPort]s cannot be connected to the same [Net],
 * which is a restriction that must be enforced by other parts of the system.
 *
 * [CombinedNet]s can span multiple [Vertice]s that have propagation delay 0 and are therefore
 * executed within the same simulation slot.
 *
 * [CombinedNet]s are considered stable during execution and could therefore be cached.
 */
class CombinedNet private constructor(net: Net<*>?, excluding: OutputPort<*>? = null) {

	val outputPorts = mutableListOf<OutputPort<*>>()

	companion object {
		fun fromOutputPort(originOutputPort: OutputPort<*>): CombinedNet =
			CombinedNet(originOutputPort.net, originOutputPort)

		fun fromNet(net: Net<*>?, excluding: OutputPort<*>? = null): CombinedNet =
			CombinedNet(net, excluding)
	}

	init {
		build(net, excluding)
	}

	/**
	 * Returns the single [OutputPort] of this [CombinedNet] that produces a defined, non-weak signal, if any.
	 * Returns `null` if there are multiple such [OutputPort]s.
	 */
	val consistentSignalPort: OutputPort<*>? get() {
		var consistentPort: OutputPort<*>? = null
		outputPorts.forEach { port ->
			if (port.weakBehaviour == null && !port.isOutputUndefined) {
				if (consistentPort == null) {
					consistentPort = port
				} else {
					if (SignalUtil.differ(consistentPort!!.getOutgoingSignal(), port.getOutgoingSignal())) {
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
	fun isConsistentWith(originOutputPort: OutputPort<*>): Boolean {
		if (originOutputPort.isOutputUndefined) {
			return true
		}
		return outputPorts.none {
			// We would like to do without comparing signals, because they can differ naturally when being split
			// by intermediate Vertices, for which there is yet no procedure to deal with. However, without comparing
			// signals, some transistor circuits that connect drain outputs will result in error states.
			!it.isOutputUndefined && it.weakBehaviour == null && SignalUtil.differ(originOutputPort.getOutgoingSignal(), it.getOutgoingSignal())
		}
	}

	/**
	 * Sets the specified [ExecutionError] on all [Net] connected to any of this [CombinedNet]'s
	 * [OutputPort]s.
	 */
	fun setExecutionError(error: ExecutionError?) {
		outputPorts
			.map { it.net }
			.toSet()
			.forEach { it?.executionError = error }
	}

	private fun build(net: Net<*>?, excluding: OutputPort<*>? = null) {
		if (net == null) {
			return
		}
		net.ports
			.filter { it !== excluding && it.portType.isOutput }
			.map { it as OutputPort<*> }
			.forEach {
				outputPorts.add(it)
				outputPorts.addAll(it.owner!!.getCombinedNetOutputPorts(it))
			}
	}
}