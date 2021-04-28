package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.checkState
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
import kotlin.reflect.KClass

/**
 * A standard [Port] implementation that can act both as an input and as an output.
 * @param T the type of signal handled by this [PortImpl]
 */
open class PortImpl<T : Any>(
	portType: PortType,
	override val signalClass: KClass<T>? = null,
	name: String?,
	description: TranslatableText = TranslatableText(),
	override var canBeUndefined: Boolean = false,
	override val weakBehaviour: WeakOutputPortBehaviour<T>? = null
) : BidirectionalPort<T>, Describable {

	constructor(portType: PortType, signalClass: KClass<T>? = null) : this(portType, signalClass, null)

	companion object {

		val LOG by logger(PortImpl::class)

		fun <T : Any> createInput(signalClass: KClass<T>? = null, name: String? = null): PortImpl<T> =
			PortImpl(PortType.INPUT, signalClass, name)

		fun <T : Any> createOutput(signalClass: KClass<T>? = null, name: String? = null, canBeUndefined: Boolean = false): PortImpl<T> =
			PortImpl(PortType.OUTPUT, signalClass, name, canBeUndefined = canBeUndefined)

		@Suppress("unused")
		fun <T : Any> createInOut(signalClass: KClass<T>? = null, name: String? = null, canBeUndefined: Boolean = false): PortImpl<T> =
			PortImpl(PortType.INOUT, signalClass, name, canBeUndefined = canBeUndefined)
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
				if (owner!!.hasPort(value)) {
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
		checkState(this.net == null, "Port already connected")
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
		_combinedNets.clear()
	}

	override fun formNet(signalHandler: SignalHandler) {
		if (portType.isOutput) {
			_combinedNets.clear()
			CombinedNet
				.createFor(this, signalHandler)
				.forEach {
					if (it.accessOf(this) != null) {
						_combinedNets.add(it)
					}
				}
		}
	}

	/** ---- [InputPort] interface */

	private var _incomingSignal: T? = null

	override fun getIncomingSignal(): T? {
		return _incomingSignal ?: getDefaultSignal()
	}

	override fun setIncomingSignal(signal: T?, signalHandler: SignalHandler) {
		if (signal != _incomingSignal) {
			storeIncomingSignal(signal)
			owner?.inputChanged(this, signalHandler)
		} else {
			signalHandler.logActorTrace(owner!!) { "Ignoring incoming signal $signal, already present"}
		}
	}

	/** ---- [OutputPort] interface */

	private var _outgoingSignal: T? = null

	private val _combinedNets = mutableListOf<CombinedNet<T>>()

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
		forwardSignal(signalHandler, withDelay = false)
	}

	override fun flush(signalHandler: SignalHandler) {
		forwardSignal(signalHandler, withDelay = true)
	}

	fun syncIncomingSignalWithNegotiatedOutgoingSignal() {
		// Don't synchronize with Net signal, as that one is not available before the
		// next simulation cycle
		storeIncomingSignal(_outgoingSignal)
	}

	/** ---- [PortImpl] */

	/**
	 * Returns the default signal value to be used as input or output value if they are `null`.
	 * Returns `null` by default. Can be overwritten by subclasses if they can define more meaningful defaults.
	 */
	protected open fun getDefaultSignal(): T? = null

	protected fun storeIncomingSignal(signal: T?) {
		_incomingSignal = signal
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
		LOG.debug(logMsg)
		signalHandler.logTrace(System.getClass(this), portId) { logMsg }

		conflict.combinedNet.setExecutionError(error)

		signalHandler.deferExecutionError(error)
	}

	private fun forwardSignal(signalHandler: SignalHandler, withDelay: Boolean) {
		if (net == null) {
			return
		}

		var anyConflict = false
		combinedNets.forEach {
			try {
				if (it.checkAllForConflict(this) != null) {
					// Try to withdraw all weak signal in the net that might be the cause of inconsistency
					withdrawWeakSignals(it, signalHandler)

					it.checkAllForConflict(this)?.let { conflict ->
						anyConflict = true
						raiseInconsistentNetError(conflict, signalHandler)
					}
				}
			} catch (e: Throwable) {
				System.breakpoint()
			}
		}

		if (anyConflict) {
			return
		}

		// All CombinedNets are consistent
		if (net!!.executionError == null) {
			val signal = replaceOwnUndefinedSignals(signalHandler)
			net!!.setSignal(signal, this, signalHandler, withDelay)
			return
		}

		// Net has become consistent and has to recover from execution error
		if (!isOutputFullyUndefined) {
			signalHandler.logTrace(System.getClass(this), portId) { "recover net by forwarding defined signal $_outgoingSignal into net '${net!!.id}'" }
			combinedNets.forEach { it.setExecutionError(null) }
			net!!.setSignal(_outgoingSignal, this, signalHandler, withDelay)
			return
		}

		// Net has become consistent by withdrawing an inconsistent signal and asserting a fully undefined signal
		// Check if there is a Port that asserts a defined signal to the net, and let it re-assert its signal
		val signal = replaceOwnUndefinedSignals(signalHandler)
		combinedNets.forEach { it.setExecutionError(null) }
		net!!.setSignal(signal, this, signalHandler, withDelay)
	}

	private fun withdrawWeakSignals(combinedNet: CombinedNet<T>, signalHandler: SignalHandler) {
		val signal = combinedNet.accessOf(this)!!.assertedSignal
		combinedNet.nets.forEach { net ->
			net.weakOutputPorts.forEach { port ->
				port.weakBehaviour!!.withdrawWeakOutput(signal, port, signalHandler)
			}
		}
	}

	private fun replaceOwnUndefinedSignals(signalHandler: SignalHandler): T? {
		var signal = _outgoingSignal
		combinedNets.forEach { combinedNet ->
			val thisAccess = combinedNet.accessOf(this)!!
			if (thisAccess.isPartiallyUndefined) {
				val consistentAccess = combinedNet.consistentAccess

				if (consistentAccess != null && consistentAccess.port !== this) {
					signalHandler.logTrace(System.getClass(this), portId) { "withdrawing signal and using signal of consistent Port" }
					signal = thisAccess.replaceUndefinedFrom(signal, consistentAccess.assertedSignal, signalHandler)
				} else {
					val weakPortToActivate = combinedNet.weakOutputPorts.firstOrNull()
					if (weakPortToActivate != null) {
						signalHandler.logTrace(System.getClass(this), portId) { "forwarding weak signal into net '${net!!.id}'" }
						val weakSignal = weakPortToActivate.weakBehaviour!!.activateWeakOutput(thisAccess.assertedSignal, weakPortToActivate, signalHandler)
						signal = thisAccess.replaceUndefinedFrom(signal, weakSignal, signalHandler)
					}
				}
			}
		}
		return signal
	}
}
