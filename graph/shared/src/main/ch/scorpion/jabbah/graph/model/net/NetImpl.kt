package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.app.SystemMalfunctionEvent
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.ExecutionError
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.element.AbstractGraphElement
import ch.scorpion.jabbah.io.*

/**
 * Standard implementation of the [Net] interface.
 */
open class NetImpl<T : Any> : AbstractGraphElement(), Net<T> {

	companion object {
		private val LOG by logger(NetImpl::class)
		private const val baseResourceKey = "graph.styleType.edge"
		private val type = Translations.getString("$baseResourceKey.name")
		private val typeDesc = null

		private val BROKEN_REF_DESIGN_ERROR = DesignError(Translations.getString("graph.designError.brokenPortRef.text"))
	}

	/** Internal representation of the [ports] property.*/
	private val _ports = mutableListOf<Port<T>>()

	private val portPropertyListener = PortPropertyListener()

	/**
	 * Set if any of the [Port] in this [NetImpl] could not be property resolved to its owning [Vertice].
	 * Only set while reading this [NetImpl] from persistence store. Represents a model inconsistency,
	 * and therefore a software bug to be notified as [SystemMalfunctionEvent].
	 */
	private var brokenRefDesignError: DesignError? = null

	/** ---- [GraphElement] interface */

	override val type: String get() = NetImpl.type
	override val typeDesc: String? get() = NetImpl.typeDesc
	override val designError: DesignError? get() = brokenRefDesignError

	/** ---- [Net] interface */

	/** Non-property variable in order to access field while allowing to override getter of [signal] in subclasses.*/
	private var _signal: T? = null

	override val signal: T?
		get() = _signal

	override var signalBuffer: T? = null

	override val portsCount: Int
		get() = _ports.size

	override val ports: ImmutableList<Port<T>>
		get() = ImmutableList(_ports)

	override val weakOutputPorts: Collection<OutputPort<T>>
		get() = _ports.filterIsInstance<OutputPort<T>>().filter { it.weakBehaviour != null }

	override fun connect(port: Port<T>) {
		check(!_ports.contains(port)) { "Net already connected to specified Port" }
		LOG.trace("connect ${port.portId}")
		_ports.add(port)
		port.connectTo(this)
		port.addPropertyChangeListener(portPropertyListener)

		stateChanged()
	}

	override fun unconnect(port: Port<*>) {
		check(_ports.contains(port)) { "Net not connected with specified Port" }
		LOG.trace("unconnect ${port.portId}")
		_ports.remove(port)
		port.disconnect()
		port.removePropertyChangeListener(portPropertyListener)

		stateChanged()
	}

	override fun isConnectedWith(port: Port<out T>): Boolean {
		return _ports.contains(port)
	}

	override fun setSignal(signal: T?, origin: OutputPort<T>, immediatePort: OutputPort<T>, signalHandler: SignalHandler, force: Boolean) {
		signalBuffer = signal
		val data = StoringGraphActorData(origin, signal, immediatePort = immediatePort, force = force)
		requestActingAfter(signalHandler, 0, data)
	}

	override fun cloneEmpty(): Net<T> = NetImpl()

	override fun splitOff(ports: Set<Port<T>>): Net<T> {
		val newNet = cloneEmpty()
		this.ports
			.filter { ports.contains(it) }
			.forEach {
				unconnect(it)
				newNet.connect(it)
			}
		return newNet
	}

	override fun combine(other: Net<T>) {
		for (port in other.ports.toList()) {
			other.unconnect(port)
			connect(port)
		}
	}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		super.executionInitialize(signalHandler)
		_signal = null
		signalBuffer = null
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		_signal = null
		signalBuffer = null
	}

	override var executionError: ExecutionError?
		get() = super.executionError
		set(value) {
			if (value != super.executionError) {
				super.executionError = value
				stateChanged()
			}
		}

	protected fun updateSignal(value: T?) {
		_signal = value
		signalBuffer = _signal
	}

	override fun actingDone(signalHandler: SignalHandler, data: ActorData?) {
		super.actingDone(signalHandler, data)

		updateSignal((data as GraphActorData).getSignal(1))

		stateChanged(signalHandler, Net.STATE_CHANGE_SIGNAL)

		// Tuning: Faster this way than with stream, filter and map
		for (i in 0 until _ports.size) {
			val port = _ports[i]
			if (port.portType.isInput && port !== data.changedPort) {
				(port as InputPort).setIncomingSignal(signal, signalHandler, data.force)
			} else if (port.portType.isOutput && port.owner is WeakOutputPortBehaviour<*>) {
				(port.owner as WeakOutputPortBehaviour<*>).handleNetChanged(signalHandler)
			}
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		val portRefs = _ports.map { PortRef(it) }.sortedBy { it.verticeId }
		if (portRefs.isNotEmpty()) {
			writer.writeStorables("ports", portRefs.iterator())
		}
		if (brokenRefDesignError != null) {
			// Necessary to make this persistence because Graph is cloned before displayed
			writer.writeBoolean("brokenRefDesignError", true)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		for (portRef in reader.readStorables<PortRef<*>>("ports")) {
			reader.requestResolution(this, Reference(
				name = "portRef",
				referenceId = portRef.verticeId,
				additionalInfo = portRef,
				resolveAfter = listOf(portRef.verticeId)
			))
		}
		if (reader.hasAttribute("brokenRefDesignError")) {
			brokenRefDesignError = BROKEN_REF_DESIGN_ERROR
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		super.resolve(reference, referenceResolver)
		if (reference.name == "portRef") {
			var vertice: Vertice? = referenceResolver.getStorable(reference.referenceId)

			val portId = (reference.additionalInfo as PortRef<T>).portId

			// TEST BEGIN
			// Simulate port reference problem (possible cause of bug #584)
			/*
			if (vertice != null && vertice.id == 2 && portId == 1) {
				vertice = null
			}
			*/
			// TEST END

			if (vertice == null) {
				LOG.warn("Couldn't resolve Vertice ${reference.referenceId} to connect to Net")
				brokenRefDesignError = BROKEN_REF_DESIGN_ERROR
				BaseModule.eventBus.post(SystemMalfunctionEvent("Broken port reference: verticeID=${reference.referenceId}, portId=$portId"))
				return
			}
			try {
				val port = vertice.getPort<T>(portId)
				_ports.add(port)
				port.connectTo(this)
			} catch (e: NoSuchElementException) {
				// If vertice has a DesignError, we assume that it is a SubGraphVerticeRef with a broken reference,
				// and we can't connect this Net that Vertice
				if (vertice.designError == null) {
					LOG.warn("Couldn't resolve Port $portId of Vertice ${System.getClassName(vertice)}")
					brokenRefDesignError = BROKEN_REF_DESIGN_ERROR
					BaseModule.eventBus.post(SystemMalfunctionEvent("Unresolvable port: verticeID=${reference.referenceId}, portId=$portId"))
				}
			}
		}
	}

	/** ---- [NetImpl] */

	private fun getOutputPorts(): Collection<OutputPort<T>> {
		return _ports
			.filter { it.portType.isOutput }
			.map { it as OutputPort<T> }
	}

	/** Used for storing references to the [Port]s of a [Net].*/
	class PortRef<T : Any>(val port: Port<T>? = null) : AbstractStorable() {

		var verticeId: Int = -1
		var portId: Int = -1

		override fun write(writer: StoreWriter) {
			writer.writeInt("verticeId", writer.provideIdentity(port!!.owner!!))
			writer.writeInt("portId", port.portId)
		}

		override fun read(reader: StoreReader) {
			verticeId = reader.readInt("verticeId")
			portId = reader.readInt("portId")
		}

		override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
			// empty
		}
	}

	private inner class PortPropertyListener : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			stateChanged()
		}
	}
}