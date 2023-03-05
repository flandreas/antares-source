package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*

/**
 * Implemented by [Vertice]s whose input [Net]s are to be combined with its output [Net]
 * to form larger [CombinedNet]s. These [Vertice] are typically pure net components, i.e.
 * they act without a propagation delay and don't perform any particular logic besides
 * signal forwarding or splitting.
 */
interface NetCombiner : Vertice {

	/**
	 * Used to enable subclasses of [NetCombiner] to refuse combining nets despite
	 * their superclass does.
	 */
	val isNetCombiner: Boolean get() = true

	/**
	 * Determines whether this [NetCombiner] as a [Vertice] requires [CombinedNet]s that start
	 * at its [OutputPort]s. This property is typically `false`, because a [NetCombiner]'s output [Net]s
	 * are combined with the incoming [Net]s.
	 */
	fun requiresCombinedNets(signalHandler: SignalHandler): Boolean

	/**
	 * Creates the [CombinedNet]s of [outputPort] given that a signal is coming from [inputPort].
	 * Can recursively create [CombinedNet] even if [requiresCombinedNets] is `true`.
	 */
	fun <T : Any> createCombinedNetsFor(
		outputPort: OutputPort<T>,
		inputPort: InputPort<T>,
		signalHandler: SignalHandler
	): Collection<CombinedNet<T>>
}

/**
 * Defines how an [OutputPort] sends and receives signals to and from a [CombinedNet].
 * Can be overridden by subclasses if a [CombinedNet] transports only a part of
 * the signal produces by an [OutputPort], which is e.g. the case when using bus splitting.
 *
 * [CombinedNetAccess]s are maintained and owned by [CombinedNet].
 *
 * @param T the type of signal
 * @param port the [OutputPort] that accesses the [CombinedNet] that owns this [CombinedNetAccess].
 */
open class CombinedNetAccess<T : Any>(
	val port: OutputPort<T>
) {
	/** Returns the signal that the [OutputPort] currently asserts to the [CombinedNet].*/
	open val assertedSignal: T? get() = port.getOutgoingSignal()

	open fun isConsistentWith(signal: T?, signalHandler: SignalHandler): Boolean =
		port.isOutputFullyUndefined || SignalUtil.equals(port.getOutgoingSignal(), signal)

	open val isFullyUndefined: Boolean get() = port.isOutputFullyUndefined

	open val isPartiallyUndefined: Boolean get() = port.isOutputPartiallyUndefined

	/**
	 * Returns the signal that results when replacing all undefined parts of [origin] with [replacement].
	 */
	open fun replaceUndefinedFrom(origin: T?, replacement: T?, signalHandler: SignalHandler): T? {
		return if (isFullyUndefined) {
			replacement
		} else {
			origin
		}
	}

	/** Copy constructor for exchanging the [OutputPort], using during net formation.*/
	open fun withOutputPort(newPort: OutputPort<T>): CombinedNetAccess<T> = CombinedNetAccess(newPort)
}

/**
 * Combines all [Net]s from an origin [OutputPort] to all destination [OutputPort] reachable by the origin.
 * Created during net formation by origin [OutputPort] and used for checking for conflicts when the origin
 * [OutputPort] is about to send a signal into the [Net] it is attached to.
 *
 * @param T the type of signal
 */
class CombinedNet<T : Any> {

	companion object {

		/** Used for preventing endless loops.*/
		private val visitStack = Stack<OutputPort<*>>()

		/**
		 * Creates all [CombinedNet]s of an [OutputPort]. Can by called recursively by [NetCombiner]s.
		 */
		fun <T: Any> createFor(
			outputPort: OutputPort<T>,
			signalHandler: SignalHandler
		): Collection<CombinedNet<T>> {
			val combinedNets = mutableListOf<CombinedNet<T>>()

			if (visitStack.contains(outputPort)) {
				return combinedNets
			}
			visitStack.push(outputPort)

			outputPort.net?.ports
				?.filter { it !== outputPort}
				?.forEach { port ->
					val combinedNetsOfPort = mutableListOf<CombinedNet<T>>()
					if (port.portType.isInput && (port.owner is NetCombiner && (port.owner as NetCombiner).isNetCombiner)) {
						combinedNetsOfPort.addAll((port.owner as NetCombiner).createCombinedNetsFor(outputPort, port as InputPort<T>, signalHandler))
					}
					if (port.portType.isOutput && (!(port.owner is NetCombiner && (port.owner as NetCombiner).isNetCombiner) || combinedNetsOfPort.isEmpty())) {
						val combinedNet = CombinedNet<T>()
						combinedNet.addAccess((port as OutputPort<T>).createAccess())
						combinedNet.addAccess(outputPort.createAccess())
						combinedNetsOfPort.add(combinedNet)
					}

					if (port.owner is NetTopologyChanger) {
						if (combinedNetsOfPort.isEmpty()) {
							combinedNetsOfPort.add(CombinedNet())
						}
						combinedNetsOfPort.forEach { it.addNetTopologyChanger(port.owner as NetTopologyChanger) }
					}

					combinedNets.addAll(combinedNetsOfPort)
				}

			outputPort.net?.let { net ->
				combinedNets.forEach { it.addNet(net) }
			}

			visitStack.pop()

			return combinedNets
		}
	}

	private val _nets = mutableSetOf<Net<T>>()

	private val _accesses = mutableListOf<CombinedNetAccess<T>>()

	private val _netTopologyChanger = mutableListOf<NetTopologyChanger>()

	val accesses: Collection<CombinedNetAccess<T>> get() = _accesses

	val nets: Collection<Net<T>> get() = _nets

	val weakOutputPorts: Collection<OutputPort<T>> get() = _nets.flatMap { it.weakOutputPorts }

	val netTopologyChanger: Collection<NetTopologyChanger> get() = _netTopologyChanger

	fun accessOf(outputPort: OutputPort<T>): CombinedNetAccess<T>? = _accesses.firstOrNull { it.port === outputPort }

	fun addAccess(access: CombinedNetAccess<T>) {
		_accesses.add(access)
	}

	fun addNetTopologyChanger(netTopologyChanger: NetTopologyChanger) {
		_netTopologyChanger.add(netTopologyChanger)
	}

	fun replaceAccess(port: OutputPort<*>, access: CombinedNetAccess<T>) {
		_accesses
			.find { it.port === port }
			.let { _accesses.remove(it) }
		_accesses.add(access)
	}

	fun replaceAccessPort(port: OutputPort<T>, newPort: OutputPort<T>) {
		accessOf(port)?.let { replaceAccess(port, it.withOutputPort(newPort)) }
	}

	fun addNet(net: Net<T>) {
		_nets.add(net)
	}

	/**
	 * Determines whether all [OutputPort]s of this [CombinedNet] are consistent with the current
	 * output value of the specified [originOutputPort].
	 * @return [SignalConflict] if a conflict is detected, `null` otherwise
	 */
	fun checkAllForConflict(originOutputPort: OutputPort<T>, signalHandler: SignalHandler): SignalConflict<T>? {
		if (accessOf(originOutputPort)!!.isFullyUndefined) {
			return null
		}

		val signal = accessOf(originOutputPort)!!.assertedSignal

		return _accesses
			.filterNot { it.port === originOutputPort }
			.firstNotNullOfOrNull { checkForConflict(signal, it, signalHandler) }
	}

	private fun checkForConflict(signal: T?, access: CombinedNetAccess<T>, signalHandler: SignalHandler): SignalConflict<T>? =
		if (access.isConsistentWith(signal, signalHandler)) {
			null
		} else {
			SignalConflict(signal, this, access.port)
		}

	fun setExecutionError(error: ExecutionError?) {
		_nets.forEach { it.executionError = error }
	}

	fun revokeSignal() {
		_nets.forEach { net ->
			net.ports
				.filter { it.portType.isInput }
				.map { it as InputPort<*> }
				.forEach { it.revokeSignal() }
		}
	}

	val hasExecutionError: Boolean get() = _nets.any { it.executionError != null }

	/**
	 * Returns the single [CombinedNetAccess] of this [CombinedNet] that produces a defined, non-weak signal
	 * that doesn't conflict with any other [OutputPort]s.
	 * Returns `null` if there are no or multiple such [CombinedNetAccess]es.
	 */
	fun getConsistentAccess(signalHandler: SignalHandler): CombinedNetAccess<T>? {
		var result: CombinedNetAccess<T>? = null
		accesses
			.forEach {
				if (it.port.weakBehaviour == null && !it.isFullyUndefined) {
					if (result == null) {
						result = it
					} else {
						if (checkAllForConflict(result!!.port, signalHandler) != null) {
							return null
						}
					}
				}
			}
		return result
	}
}