package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.element.AbstractGraphElement
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * An abstract base implementation of the [Vertice] interface.
 */
abstract class AbstractVertice(
	name: String? = null
) : AbstractGraphElement(), Vertice {

	/** Contains all [Port]s of this [Vertice].*/
	private val ports = mutableListOf<Port<*>>()

	/** Caches property [inputCount] during simulation for faster, very often used access. */
	private var cachedInputCount: Int = 0

	/** ---- [Vertice] interface */

	override var name: String? = name
		set(value) {
			if (value != field) {
				field = value
				stateChanged()
			}
		}

	override val portsCount: Int get() = ports.size

	override val inputCount: Int get() = if (executionRunning) cachedInputCount else ports.count { it.portType.isInput }

	override val outputCount: Int get() = ports.count { it.portType.isOutput }

	override val isConnected: Boolean get() = ports.any { it.isConnected }

	override val hasAnyOutput: Boolean get() = ports.any { it.portType.isOutput }

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler, force: Boolean) {
		if (signalHandler.isLogTrace) {
			signalHandler.logActorTrace(this) { "input changed to ${dataToString()}, will calculate at ${signalHandler.executionTime + propagationDelay} ns" }
		}

		requestActingAfter(signalHandler, propagationDelay, createActorData(input, force))
		stateChanged(signalHandler, Vertice.STATE_CHANGE_INPUT)
	}

	override fun outputChanged(output: OutputPort<*>, signalHandler: SignalHandler) {
		stateChanged(signalHandler, Vertice.STATE_CHANGE_OUTPUT)
	}

	override fun <T : Any> addPort(port: Port<T>) {
		addPort(port, freePortId)
	}

	private val freePortId: Int get() {
		// For efficiency reasons don't try to find holes in the sequence
		return (ports.map { it.portId }.maxOrNull() ?: 0) + 1
	}

	override fun removePort(port: Port<*>) {
		port.owner = null
		port.net?.unconnect(port)
		ports.remove(port)
	}

	override fun <T : Any> getPort(): Port<T> {
		@Suppress("UNCHECKED_CAST")
		return ports[0] as Port<T>
	}

	override fun hasPort(name: String?): Boolean =
		ports.any { name != null && it.name == name}

	override fun hasPort(id: Int): Boolean =
		ports.any { it.portId == id }

	override fun getPorts(): List<Port<*>> = ports

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> getPort(name: String): Port<T> =
		ports.first { it.name == name } as Port<T>

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> getPort(id: Int): Port<T> =
		ports.first { it.portId == id } as Port<T>

	override fun hasInput(name: String?): Boolean =
		ports.any { it.portType.isInput && name != null && it.name == name }

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> getInput(): InputPort<T> =
		ports.first { it.portType.isInput } as InputPort<T>

	override fun getInputs(): List<InputPort<*>> =
		ports.filter { it.portType.isInput }.map { it as InputPort<*> }

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> getInput(name: String): InputPort<T> =
		ports.first { it.portType.isInput && it.name == name } as InputPort<T>

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> getInput(id: Int): InputPort<T> =
		ports.first { it.portType.isInput && it.portId == id } as InputPort<T>

	override fun hasOutput(name: String?): Boolean =
		ports.any { it.portType.isOutput && name != null && it.name == name }

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> getOutput(): OutputPort<T> =
		ports.first { it.portType.isOutput } as OutputPort<T>

	override fun getOutputs(): List<OutputPort<*>> =
		ports.filter { it.portType.isOutput }.map { it as OutputPort<*> }

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> getOutput(name: String): OutputPort<T> =
		ports.first { it.portType.isOutput && it.name == name } as OutputPort<T>

	@Suppress("UNCHECKED_CAST")
	override fun <T : Any> getOutput(id: Int): OutputPort<T> =
		ports.first { it.portType.isOutput && it.portId == id } as OutputPort<T>

	override fun <T : Any> replaceUndefinedOutput(signal: T?) { }

	override fun <T : Any> notifyResendSignal(port: OutputPort<T>, signalHandler: SignalHandler) { }

	/** ---- [Storable] interface */

	/**
	 * Determines whether this [AbstractVertice] and its subclasses store the [name] property as [Storable].
	 * The default is `true`. Subclasses that translate the [name] property using [TranslatableText] will
	 * return `false` and store the [TranslatableText] instead.
	 */
	protected open val storesName: Boolean get() = true

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (storesName && name != null) {
			writer.writeString("name", name!!)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (storesName && reader.hasAttribute("name")) {
			name = reader.readString("name")
		}
	}

	/** ---- [Actor] interface */

	override fun executionInitialize(signalHandler: SignalHandler) {
		cachedInputCount = inputCount
		super.executionInitialize(signalHandler)
		ports.forEach { it.executionStarted(signalHandler) }
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		ports.forEach { it.executionStopped(signalHandler) }
	}

	override fun formNet(signalHandler: SignalHandler) {
		super.formNet(signalHandler)

		if (!(this is NetCombiner && this.isNetCombiner)  || this.requiresCombinedNets(signalHandler)) {
			getOutputs().forEach {
				it.formNet(signalHandler)
			}
		}
	}

	override val isBreakpoint: Boolean get() = super.isBreakpoint && hasAnyOutput

	/** ---- [AbstractVertice] */

	/** Visible for testing. */
	open fun createActorData(inputPort: InputPort<*>?, force: Boolean = false, graphView: GraphView? = null): GraphActorData =
		ActualPortValueActorData(inputPort, true, force = force, graphView = graphView)

	/**
	 * Clears all [Port]s, i.e. removes them from this [AbstractVertice].
	 * This method should only be called when instantiating [AbstractVertice]s as [Storable]s, which must
	 * use the default constructor of leaf classes and therefore redo the standard [Port] setup in non-default
	 * constructors.
	 */
	protected fun clearPorts() {
		ports.forEach { it.owner = null }
		ports.clear()
	}

	protected fun <T : Any> addPort(port: Port<T>, portId: Int) {
		require(!ports.contains(port)) { "Port already contained" }
		require(!hasPort(port.name)) { "Port with name '${port.name}' already contained" }
		require(ports.none { it.portId == portId }) { "Port with ID $portId already contained" }
		ports.add(port)
		port.portId = portId
		port.owner = this
	}

	private fun dataToString(): String {
		val s = StringBuilder()
		for (input in getInputs()) {
			s.append("${input.portId}: ${input.getIncomingSignal()} ")
		}
		return s.toString()
	}

	/**
	 * A [GraphActorData] implementation that forwards the request for the signal to the corresponding [Port],
	 * hence using the actual [Port] values at this very moment, instead of using values as they were
	 * when this [Vertice] requested calculation.
	 */
	private inner class ActualPortValueActorData(
		override val changedPort: Port<*>?,
		override val isInput: Boolean = true,
		override val immediatePort: Port<*>? = changedPort,
		override val force: Boolean,
		override val graphView: GraphView? = null
	) : GraphActorData {

		override fun <T : Any> getSignal(portId: Int): T? =
			getInput<T>(portId).getIncomingSignal()

		override fun dataToString(): String = this@AbstractVertice.dataToString()
	}
}