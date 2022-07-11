package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.event.PropertyChangeSupport
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.model.text.description.observableDescription
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.Port.Companion.PROP_NAME
import ch.scorpion.jabbah.graph.model.Port.Companion.PROP_PORT_TYPE
import ch.scorpion.jabbah.graph.model.net.*

/**
 * A standard [Port] implementation that can act both as an input and as an output.
 * @param T the type of signal handled by this [PortImpl]
 */
open class PortImpl<T : Any>(
	portType: PortType,
	name: String?,
	description: TranslatableText = TranslatableText(),
	override var customCanBeUndefined: Boolean = false,
	override val weakBehaviour: WeakOutputPortBehaviour<T>? = null
) : BidirectionalPort<T>, Describable, NetTopologyChangeListener {

	constructor(portType: PortType) : this(portType, null)

	companion object {

		val LOG by logger(PortImpl::class)

		fun <T : Any> createInput(name: String? = null): PortImpl<T> =
			PortImpl(PortType.INPUT, name)

		fun <T : Any> createOutput(name: String? = null, canBeUndefined: Boolean = false): PortImpl<T> =
			PortImpl(PortType.OUTPUT, name, customCanBeUndefined = canBeUndefined)

		@Suppress("unused")
		fun <T : Any> createInOut(name: String? = null, canBeUndefined: Boolean = false): PortImpl<T> =
			PortImpl(PortType.INOUT, name, customCanBeUndefined = canBeUndefined)
	}

	protected val changeSupport = PropertyChangeSupport<Any>(this)

	override fun toString(): String {
		return "PortImpl ${portType.name} '$name'"
	}

	override var description: Description by observableDescription(Description(description))

	/** ---- [Port] interface */

	override var portId: Int = 0

	override var portType: PortType = portType
		set(value) {
			if (value == field) {
				return
			}
			val oldValue = field
			field = value
			changeSupport.fire(PROP_PORT_TYPE, oldValue, field)
		}

	override var name: String? = name
		set(value) {
			if (value == field) {
				return
			}
			if (owner != null && value != null) {
				if (owner!!.requireUniquePortNames && owner!!.hasPort(value)) {
					throw IllegalArgumentException("Port name $value not unique in Vertice")
				}
			}
			val oldValue = field
			field = value
			changeSupport.fire(PROP_NAME, oldValue, field)
		}

	override var owner: Vertice? = null

	override var net: Net<T>? = null

	override fun accept(visitor: HierarchyVisitor) {
		visitor.visit(this)
	}

	override fun addPropertyChangeListener(l: PropertyChangeListener<Any>) {
		changeSupport.add(l)
	}

	override fun removePropertyChangeListener(l: PropertyChangeListener<Any>) {
		changeSupport.remove(l)
	}

	override fun connectTo(net: Net<T>) {
		check(this.net == null) { "Port already connected" }
		this.net = net
	}

	override fun disconnect() {
		net = null
	}

	override fun executionStarted(signalHandler: SignalHandler) {
		storeIncomingSignal(null)
		storeOutgoingSignal(null)
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		// Reset CombinedNet because the structure might be changed before the next start of execution
		clearCombinedNets()
	}

	override fun formNet(signalHandler: SignalHandler) {
		if (portType.isOutput) {
			clearCombinedNets()
			CombinedNet
				.createFor(this, signalHandler)
				.forEach { combinedNet ->
					if (combinedNet.accessOf(this) != null) {
						_combinedNets.add(combinedNet)
						if (combinedNet.netTopologyChanger.isNotEmpty()) {
							combinedNet.netTopologyChanger.forEach { it.addNetTopologyChangeListener(this) }
						}
					}
				}
		}
	}

	private fun clearCombinedNets() {
		_combinedNets.forEach { combinedNet ->
			combinedNet.netTopologyChanger.forEach { it.removeNetTopologyChangeListener(this) }
		}
		_combinedNets.clear()
	}

	/** ---- [InputPort] interface */

	protected var _incomingSignal: T? = null
		private set

	private var incomingSignalRevoked: Boolean = false

	override fun getIncomingSignal(): T? {
		return _incomingSignal ?: getDefaultSignal()
	}

	override fun setIncomingSignal(signal: T?, signalHandler: SignalHandler, force: Boolean) {
		if (force || differingInput(signal) || incomingSignalRevoked && portType == PortType.INOUT) {
			storeIncomingSignal(signal)
			owner?.inputChanged(this, signalHandler, force)
		} else {
			signalHandler.logActorTrace(owner!!) { "Ignoring incoming signal $signal, already present"}
		}
	}

	protected open fun differingInput(signal: T?): Boolean =
		SignalUtil.differ(signal, _incomingSignal)

	override fun revokeSignal() {
		incomingSignalRevoked = true
	}

	/** ---- [OutputPort] interface */

	private var _outgoingSignal: T? = null

	private val _combinedNets = mutableListOf<CombinedNet<T>>()

	/** Created on-demand in [formNet]. Used to redo net formation during execution after net topology has changed.*/
	private var netTopologyChangeListener: NetTopologyChangeListener? = null

	override val combinedNets: Collection<CombinedNet<T>> get() = _combinedNets

	override fun getOutgoingSignal(): T? {
		return _outgoingSignal ?: getDefaultSignal()
	}

	override val isOutputFullyUndefined: Boolean
		get() = _outgoingSignal == null

	override val isOutputPartiallyUndefined: Boolean
		get() = _outgoingSignal == null

	override fun createAccess(): CombinedNetAccess<T> =
		CombinedNetAccess(this)

	override fun isOutgoingSignalConsistentWith(signal: T?): Boolean =
		isOutputFullyUndefined || SignalUtil.equals(_outgoingSignal, signal)

	override fun setOutgoingSignalBuffered(signal: T?, signalHandler: SignalHandler) {
		storeOutgoingSignal(signal)
	}

	override fun setOutgoingSignal(signal: T?, signalHandler: SignalHandler) {
		storeOutgoingSignal(signal)
		forwardSignal(signalHandler, force = false)
	}

	override fun flush(signalHandler: SignalHandler, force: Boolean) {
		forwardSignal(signalHandler, force)
	}

	fun syncIncomingSignalWithNegotiatedOutgoingSignal() {
		// Don't synchronize with Net signal, as that one is not available before the
		// next simulation cycle
		storeIncomingSignal(_outgoingSignal)
	}

	/** ---- [BidirectionalPort] */

	override var isOutputDominant: Boolean = true

	override val dominantSignal: T
		get() {
			return if (isOutputDominant) {
				getOutgoingSignal()!!
			} else {
				getIncomingSignal()!!
			}
		}

	/** ---- [NetTopologyChangeListener] */

	override fun handle(event: NetTopologyChangeEvent) {
		formNet(event.signalHandler)
	}

	override fun resendSignal(signalHandler: SignalHandler) {
		owner?.notifyResendSignal(this, signalHandler)
		flush(signalHandler, force = true)
	}

	/** ---- [PortImpl] */

	/**
	 * Returns the default signal value to be used as input or output value if they are `null`.
	 * Returns `null` by default. Can be overwritten by subclasses if they can define more meaningful defaults.
	 */
	protected open fun getDefaultSignal(): T? = null

	protected fun storeIncomingSignal(signal: T?) {
		_incomingSignal = signal
		incomingSignalRevoked = false
	}

	protected fun storeOutgoingSignal(signal: T?) {
		_outgoingSignal = signal
	}

	protected fun clear() {
		storeIncomingSignal(null)
		storeOutgoingSignal(null)
	}

	private fun raiseInconsistentNetError(
		conflict: SignalConflict<T>,
		signalHandler: SignalHandler
	) {

		val error = InconsistentNetError(this, conflict, signalHandler.executionTime)

		val logMsg = "Inconsistent net signal ${conflict.signal} from port $portId in ${owner?.id}. Conflict with " +
			"${conflict.destinationPort.getOutgoingSignal()} from ${conflict.destinationPort.portId} " +
			"in ${conflict.destinationPort.owner!!.id}"
		LOG.trace(logMsg)
		signalHandler.logTrace(System.getClass(this), portId) { logMsg }

		conflict.combinedNet.setExecutionError(error)

		signalHandler.deferExecutionError(error)
	}

	private fun resetExecutionError() {
		combinedNets.forEach { it.setExecutionError(null) }
		net!!.executionError = null
	}

	private fun forwardSignal(signalHandler: SignalHandler, force: Boolean) {
		if (net == null) {
			return
		}

		var anyConflict = false
		combinedNets.forEach {
			if (it.checkAllForConflict(this, signalHandler) != null) {
				// Try to withdraw all weak signal in the net that might be the cause of inconsistency
				withdrawWeakSignals(it, signalHandler)

				it.checkAllForConflict(this, signalHandler)?.let { conflict ->
					anyConflict = true
					raiseInconsistentNetError(conflict, signalHandler)
				}
			}
		}

		if (anyConflict) {
			return
		}

		// All CombinedNets are consistent
		if (net!!.executionError == null) {
			val replacement = replaceOwnUndefinedSignals(signalHandler)
			assertSignalToNet(replacement.signal, replacement.originPort, signalHandler, force)
			return
		}

		// Net has become consistent and has to recover from execution error
		if (!isOutputFullyUndefined) {
			signalHandler.logTrace(System.getClass(this), portId) { "recover net by forwarding defined signal $_outgoingSignal into net '${net!!.id}'" }
			resetExecutionError()
			assertSignalToNet(_outgoingSignal, this, signalHandler, force)
			return
		}

		// Net has become consistent by withdrawing an inconsistent signal and asserting a fully undefined signal
		// Check if there is a Port that asserts a defined signal to the net, and let it re-assert its signal
		val replacement = replaceOwnUndefinedSignals(signalHandler)
		resetExecutionError()

		assertSignalToNet(replacement.signal, replacement.originPort, signalHandler, force)
	}

	private fun assertSignalToNet(signal: T?, originPort: OutputPort<T>, signalHandler: SignalHandler, force: Boolean) {
		val appliedSignal = if (signalHandler.executionContext is GraphExecutionContext<*>) {
			(signalHandler.executionContext as GraphExecutionContext<T>).netSignalApplier.calculateSignal(
				signal,
				net!!,
				this)
		} else {
			signal
		}

		net!!.setSignal(appliedSignal, originPort, this, signalHandler, force)
	}

	private fun withdrawWeakSignals(combinedNet: CombinedNet<T>, signalHandler: SignalHandler) {
		val signal = combinedNet.accessOf(this)!!.assertedSignal
		combinedNet.nets.forEach { net ->
			net.weakOutputPorts.forEach { port ->
				port.weakBehaviour!!.withdrawWeakOutput(signal, port, signalHandler)
			}
		}
	}

	private data class SignalReplacement<T: Any>(
		val signal: T?,
		val originPort: OutputPort<T>
	)

	private fun replaceOwnUndefinedSignals(signalHandler: SignalHandler): SignalReplacement<T> {
		var replacement = SignalReplacement(_outgoingSignal, this)

		// First replace undefined signal with signals from other consistent accesses
		combinedNets.forEach { combinedNet ->
			val thisAccess = combinedNet.accessOf(this)!!
			if (thisAccess.isPartiallyUndefined) {
				combinedNet.getConsistentAccess(signalHandler)?.let { consistentAccess ->
					signalHandler.logTrace(System.getClass(this), portId) { "withdrawing signal and using signal of consistent Port" }
					replacement = SignalReplacement(
						thisAccess.replaceUndefinedFrom(replacement.signal, consistentAccess.assertedSignal, signalHandler),
						consistentAccess.port)
				}
				combinedNet.revokeSignal()
			}
		}

		// Then replace undefined signals with signals from other weak accesses
		combinedNets.forEach { combinedNet ->
			val thisAccess = combinedNet.accessOf(this)!!
			if (thisAccess.isPartiallyUndefined) {
				combinedNet.weakOutputPorts.firstOrNull()?.let { weakPortToActivate ->
					signalHandler.logTrace(System.getClass(this), portId) { "forwarding weak signal into net '${net!!.id}'" }
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
