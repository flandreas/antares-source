package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.element.AbstractGraphElement
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.DescribableImpl

/**
 * An abstract base implementation of the [Vertice] interface.
 */
abstract class AbstractVertice(
	name: String? = null,
	private val describable: Describable = DescribableImpl()
) : AbstractGraphElement(), Vertice, Describable by describable {

	companion object {
		private val LOG by logger(AbstractVertice::class)
	}

	init {
		describable.description.changeHandler = { _, _ -> stateChanged() }
	}

	/** Contains all [Port]s of this [Vertice].*/
	private val ports = mutableListOf<Port<*>>()

	/** ---- [Vertice] interface */

	override var name: String? = name
		set(value) {
			if (value != field) {
				field = value
				stateChanged()
			}
		}

	override val portsCount: Int
		get() = ports.size

	override val inputCount: Int
		get() = getInputs().size

	override val outputCount: Int
		get() = getOutputs().size

	override val isConnected: Boolean
		get() = ports.any { it.isConnected }

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
		if (LOG.isTraceEnabled()) {
			signalHandler.logTrace(
				System.getClass(this), id) {
				"input changed to ${dataToString()}, will calculate at ${signalHandler.executionTime + propagationDelay} ns"
			}
		}

		requestActingAfter(signalHandler, propagationDelay, createActorData(input))
		stateChanged(signalHandler)
	}

	override fun outputChanged(output: OutputPort<*>, signalHandler: SignalHandler) {
		stateChanged()
	}

	override fun <T : Any> addPort(port: Port<T>) {
		addPort(port, ports.size + 1)
	}

	override fun removePort(port: Port<*>) {
		port.owner = null
		ports.remove(port)
	}

	override fun <T : Any> getPort(): Port<T> {
		return ports[0] as Port<T>
	}

	override fun getPorts(): ImmutableList<Port<*>> {
		return ImmutableList(ports)
	}

	override fun <T : Any> getPort(name: String): Port<T> {
		return ports.first { it.name == name } as Port<T>
	}

	override fun <T : Any> getPort(id: Int): Port<T> {
		return ports.first { it.portId == id } as Port<T>
	}

	override fun <T : Any> getInput(): InputPort<T> {
		return getInputs()[0] as InputPort<T>
	}

	override fun getInputs(): ImmutableList<InputPort<*>> {
		return ImmutableList(ports.filter { it.portType.isInput }.map { it as InputPort<*> })
	}

	override fun <T : Any> getInput(name: String): InputPort<T> {
		return getInputs().first { it.name == name } as InputPort<T>
	}

	override fun <T : Any> getInput(id: Int): InputPort<T> {
		return getInputs().first { it.portId == id } as InputPort<T>
	}

	override fun <T : Any> getOutput(): OutputPort<T> {
		return getOutputs()[0] as OutputPort<T>
	}

	override fun getOutputs(): ImmutableList<OutputPort<*>> {
		return ImmutableList(ports.filter { it.portType.isOutput }.map { it as OutputPort<*> })
	}

	override fun <T : Any> getOutput(name: String): OutputPort<T> {
		return getOutputs().first { it.name == name } as OutputPort<T>
	}

	override fun <T : Any> getOutput(id: Int): OutputPort<T> {
		return getOutputs().first { it.portId == id } as OutputPort<T>
	}

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
		description.write("desc", writer)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (storesName && reader.hasAttribute("name")) {
			name = reader.readString("name")
		}
		description.read("desc", reader)
	}

	/** ---- [Actor] interface */

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		ports.forEach { it.executionStarted(signalHandler) }
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		ports.forEach { it.executionStopped(signalHandler) }
	}

	/** ---- [AbstractVertice] */

	/**
	 * Visible for testing.
	 */
	fun createActorData(inputPort: InputPort<*>?): VerticeActorData {
		return VerticeActorData(inputPort, true)
	}

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
		checkArgument(!ports.contains(port), "isPort already contained")
		checkArgument(!hasPort(port.name), "isPort with name '${port.name}' already contained")
		ports.add(port)
		port.portId = portId
		port.owner = this
	}

	/** Checks whether this [AbstractVertice] has a [Port] with the specified name.*/
	private fun hasPort(name: String?): Boolean {
		return ports.find { name != null && it.name == name } != null
	}

	private fun dataToString(): String {
		val s = StringBuilder()
		for (input in getInputs()) {
			s.append("${input.portId}: ${input.getIncomingSignal()} ")
		}
		return s.toString()
	}

	/** A "virtual" [GraphActorData] implementation that forwards the request for the signal to the corresponding [Port].*/
	inner class VerticeActorData(override val changedPort: Port<*>?, override val isInput: Boolean = true) : GraphActorData {

		override fun <T : Any> getSignal(portId: Int): T {
			return getInput<T>(portId).getIncomingSignal() as T
		}

		override fun dataToString(): String {
			return this@AbstractVertice.dataToString()
		}
	}
}