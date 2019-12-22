package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.checkState
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.event.PropertyChangeSupport
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.DescribableImpl
import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.Port.Companion.PROP_NAME
import ch.scorpion.jabbah.graph.model.Port.Companion.PROP_PORT_TYPE
import kotlin.reflect.KClass

/**
 * A standard [Port] implementation that can act both as an input and as an output.
 * @param T the type of signal handled by this [PortImpl]
 */
open class PortImpl<T : Any>(
	portType: PortType,
	override val signalClass: KClass<T>? = null,
	name: String?,
	private val describable: Describable = DescribableImpl()
) : BidirectionalPort<T>, Describable by describable {

	constructor(portType: PortType, signalClass: KClass<T>? = null) : this(portType, signalClass, null)

	companion object {

		val LOG by logger(PortImpl::class)

		fun <T : Any> createInput(signalClass: KClass<T>? = null, name: String? = null): PortImpl<T> =
			PortImpl(PortType.INPUT, signalClass, name)

		fun <T : Any> createOutput(signalClass: KClass<T>? = null, name: String? = null): PortImpl<T> =
			PortImpl(PortType.OUTPUT, signalClass, name)

		@Suppress("unused")
		fun <T : Any> createInOut(signalClass: KClass<T>? = null, name: String? = null): PortImpl<T> =
			PortImpl(PortType.INOUT, signalClass, name)
	}

	protected val changeSupport = PropertyChangeSupport<Any>(this)

	override fun toString(): String {
		return "PortImpl ${portType.name} '$name'"
	}

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
		// empty
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
		}
	}

	/** ---- [OutputPort] interface */

	private var _outgoingSignal: T? = null

	override fun getOutgoingSignal(): T? {
		return _outgoingSignal ?: getDefaultSignal()
	}

	override val isOutputUndefined: Boolean
		get() = _outgoingSignal == null


	override fun setOutgoingSignalBuffered(signal: T?, signalHandler: SignalHandler) {
		storeOutgoingSignal(signal)
	}

	override fun setOutgoingSignal(signal: T?, signalHandler: SignalHandler) {
		storeOutgoingSignal(signal)
		forwardSignal(signal, signalHandler, withDelay = false)
	}

	override fun flush(signalHandler: SignalHandler) {
		forwardSignal(getOutgoingSignal(), signalHandler, withDelay = true)
	}

	/** ---- [PortImpl] */

	/**
	 * Returns the default signal value to be used as input or output value if they are `null`.
	 * Returns `null` by default. Can be overwritten by subclasses if they can define more meaningful defaults.
	 */
	protected open fun getDefaultSignal(): T? {
		return null
	}

	protected fun storeIncomingSignal(signal: T?) {
		_incomingSignal = signal
	}

	protected fun storeOutgoingSignal(signal: T?) {
		_outgoingSignal = signal
	}

	protected fun clear() {
		_incomingSignal = null
		_outgoingSignal = null
	}

	private fun forwardSignal(signal: T?, signalHandler: SignalHandler, withDelay: Boolean) {
		if (net == null) {
			return
		}

		if (net!!.inconsistent) {
			signalHandler.logTrace(System.getClass(this), portId) { "inconsistent net signal $signal" }
			LOG.debug("Inconsistent net signal $signal from port $portId in ${owner?.id}")
			net!!.executionError = InconsistentNetError()
			return
		}

		// Net is consistent
		if (net!!.executionError == null) {
			if (isOutputUndefined) {
				withdrawSignal(signal, signalHandler, withDelay)
			} else {
				signalHandler.logActorTrace(net!!) { "forwarding signal $signal into net" }
				net!!.setSignal(signal, this, signalHandler, withDelay)
			}
			return
		}

		// Net has become consistent and has to recover from execution error
		if (!isOutputUndefined) {
			signalHandler.logTrace(System.getClass(this), portId) { "recover net by forwarding defined signal $signal into net '${net!!.id}'" }
			net!!.executionError = null
			net!!.setSignal(signal, this, signalHandler, withDelay)
			return
		}

		// Net has become consistent by withdrawing an inconsistent signal and asserting an undefined signal
		// Check if there is a Port that asserts a defined signal to the net, and let it re-assert its signal
		withdrawSignal(signal, signalHandler, withDelay)
	}

	private fun withdrawSignal(signal: T?, signalHandler: SignalHandler, withDelay: Boolean) {
		val consistentPort = net!!.getConsistentSignalPort()
		if (consistentPort != null) {
			signalHandler.logTrace(System.getClass(this), portId) { "withdrawing signal by re-asserting signal of consistent Port" }
			net!!.executionError = null
			consistentPort.flush(signalHandler)
		} else {
			signalHandler.logTrace(System.getClass(this), portId) { "forwarding undefined signal into net '${net!!.id}'" }
			net!!.executionError = null
			net!!.setSignal(signal, this, signalHandler, withDelay)
		}
	}
}

/** Signals that a [PortImpl] tried to assign a signal to its [Net] that turns the [Net] inconsistent.*/
class InconsistentNetError : ExecutionError