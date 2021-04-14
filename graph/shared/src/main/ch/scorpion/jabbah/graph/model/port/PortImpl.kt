package ch.scorpion.jabbah.graph.model.port

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.event.PropertyChangeSupport
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.model.text.description.observableDescription
import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.issue.IssueImpl
import ch.scorpion.jabbah.execution.issue.IssueSeverity
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.Port.Companion.PROP_NAME
import ch.scorpion.jabbah.graph.model.Port.Companion.PROP_PORT_TYPE
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.SignalConflict
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
		_combinedNet = null
	}

	override fun formNet(signalHandler: SignalHandler) {
		if (portType.isOutput) {
			ensureCombinedNet(signalHandler)
		}
	}

	private fun ensureCombinedNet(signalHandler: SignalHandler): CombinedNet<T> {
		if (_combinedNet == null) {
			_combinedNet = CombinedNet.fromOutputPort(this, signalHandler)
		}
		return _combinedNet!!
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

	private var _combinedNet: CombinedNet<T>? = null

	override val combinedNet: CombinedNet<T>? get() = _combinedNet

	override fun getOutgoingSignal(): T? {
		return _outgoingSignal ?: getDefaultSignal()
	}

	override val isOutputFullyUndefined: Boolean
		get() = _outgoingSignal == null

	override val isOutputPartiallyUndefined: Boolean
		get() = _outgoingSignal == null

	override fun isOutgoingSignalConsistentWith(signal: T?): Boolean =
		isOutputFullyUndefined || SignalUtil.equals(_outgoingSignal, signal)

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
		LOG.trace("Storing incoming signal $signal in port $portId of ${owner?.id}")
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
		combinedNet: CombinedNet<T>,
		conflict: SignalConflict<T>,
		signalHandler: SignalHandler
	) {
		val error = InconsistentNetError(this, combinedNet, conflict)

		val logMsg = "Inconsistent net signal ${conflict.convertedSignal} from port $portId in ${owner?.id}. Conflict with " +
			"${conflict.destinationOutputPort.getOutgoingSignal()} from ${conflict.destinationOutputPort.portId} " +
			"in ${conflict.destinationOutputPort.owner!!.id}"

		LOG.debug(logMsg)
		signalHandler.logTrace(System.getClass(this), portId) { logMsg }

		combinedNet.setExecutionError(error)

		signalHandler.deferExecutionError(error)
	}

	// TODO: Shouldn't this logic be part of NetImpl?
	private fun forwardSignal(signal: T?, signalHandler: SignalHandler, withDelay: Boolean) {
		if (net == null) {
			return
		}

		signalHandler.logTrace(System.getClass(this), portId) { "forwarding signal $signal from port $portId in ${owner?.id}" }

		val combinedNet = ensureCombinedNet(signalHandler)

		if (combinedNet.checkForConflict(this) != null) {

			// Try to withdraw all weak signal in the net that might be the cause of inconsistency
			if (!isOutputFullyUndefined) {
				withdrawWeakSignals(signalHandler)
			}

			combinedNet.checkForConflict(this)?.let {
				raiseInconsistentNetError(combinedNet, it, signalHandler)
				return
			}
		}

		// Net is consistent
		if (net!!.executionError == null) {
			if (isOutputPartiallyUndefined) {
				withdrawSignal(combinedNet, signal, signalHandler, withDelay)
			} else {
				signalHandler.logActorTrace(net!!) { "setting signal $signal on net ${net!!.id}" }
				net!!.setSignal(signal, this, signalHandler, withDelay)
			}
			return
		}

		// Net has become consistent and has to recover from execution error
		if (!isOutputFullyUndefined) {
			signalHandler.logTrace(System.getClass(this), portId) { "recover net by forwarding defined signal $signal into net '${net!!.id}'" }
			combinedNet.setExecutionError(null)
			net!!.setSignal(signal, this, signalHandler, withDelay)
			return
		}

		// Net has become consistent by withdrawing an inconsistent signal and asserting a fully undefined signal
		// Check if there is a Port that asserts a defined signal to the net, and let it re-assert its signal
		withdrawSignal(combinedNet, signal, signalHandler, withDelay)
	}

	private fun withdrawSignal(combinedNet: CombinedNet<T>, signal: T?, signalHandler: SignalHandler, withDelay: Boolean) {
		val consistentPort = combinedNet.consistentSignalPort
		if (consistentPort != null) {
			signalHandler.logTrace(System.getClass(this), portId) { "withdrawing signal by re-asserting signal of consistent Port" }
			combinedNet.setExecutionError(null)
			// TODO Set Net signal directly instead of executing forwarding logic again
			consistentPort.flush(signalHandler)
		} else {
			// TODO We only regard the first weak OutputPort, but what is if there are multiple weak
			// OutputPorts at the same Net, possibly with conflicting weak values? Should that be
			// considered a design error?
			val weakPortToActivate = net!!.weakOutputPorts.firstOrNull()
			if (weakPortToActivate != null) {
				signalHandler.logTrace(System.getClass(this), portId) { "forwarding weak signal into net '${net!!.id}'" }
				val weakSignal = weakPortToActivate.weakBehaviour!!.activateWeakOutput(signal, weakPortToActivate, signalHandler)
				net!!.setSignal(weakSignal, this, signalHandler, withDelay)
			} else {
				signalHandler.logTrace(System.getClass(this), portId) { "forwarding undefined signal into net '${net!!.id}'" }
				combinedNet.setExecutionError(null)
				net!!.setSignal(signal, this, signalHandler, withDelay)
			}
		}
	}

	private fun withdrawWeakSignals(signalHandler: SignalHandler) {
		net?.weakOutputPorts?.forEach { port ->
			port.weakBehaviour!!.withdrawWeakOutput(port, signalHandler)
		}
	}
}

/** Signals that a [PortImpl] tried to assign a signal to its [Net] that turns the [Net] inconsistent.*/
class InconsistentNetError(
	private val originPort: OutputPort<*>,
	private val combinedNet: CombinedNet<*>,
	private val conflict: SignalConflict<*>
) : ExecutionError {

	override fun reevaluate(signalHandler: SignalHandler) {
		if (combinedNet.hasExecutionError) {
			post()
		}
	}

	private fun post() {
		val description = Translations.getString(
			"graph.inconsistentNetError.description",
			"${conflict.convertedSignal}, ${conflict.destinationOutputPort.getOutgoingSignal()}")
		val originDesc = Translations.getString(
			"graph.inconsistentNetError.origin",
			"${originPort.owner!!.type} (${originPort.owner!!.id})",
			"${conflict.destinationOutputPort.owner!!.type} (${conflict.destinationOutputPort.owner!!.id})")

		BaseModule.eventBus.post(IssueImpl(
			IssueSeverity.Error,
			Translations.getString("graph.inconsistentNetError.name"),
			description = description,
			origin = originDesc,
			context = null
		))
	}
}