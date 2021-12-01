package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.VetoException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.description.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.io.*

/**
 * A standard implementation of the [Graph] interface.
 */
open class GraphImpl(
	name: String = Translations.getString("graph.name.unknown"),
	private val eventBus: EventBus = BaseModule.eventBus
) : Graph, Namable, Describable {

	companion object {
		private val LOG by logger(GraphImpl::class)
	}

	private val _elements = mutableListOf<GraphElement>()

	/** Forwards signal changes of a [OscilloscopeProbeVertice] to the [Oscilloscope].*/
	private val oscilloscopeProbeHandler = OscilloscopeProbeHandler()

	private val graphPortNameChangedHandler: EventHandler<GraphPortNameChanged<Any>> = {
		LOG.trace("handling GraphPortNameChanged")
		if (it.newName != null && contains(it.graphPort) && existsGraphPortNameExcluding(it.newName, it.graphPort)) {
			throw VetoException(Translations.getString("graph.port.nameAlreadyExists.msg"))
		}
	}

	init {
		eventBus.register(GraphPortNameChanged::class, graphPortNameChangedHandler)
	}

	override fun dispose() {
		eventBus.unregister(GraphPortNameChanged::class, graphPortNameChangedHandler)
	}

	/** ---- [Namable], [Describable] interfaces */

	override var name: Name by observableName(Name(name))

	override var description: Description by observableDescription(Description(""))

	/** ---- [Graph] interface */

	override var uuid: UUID = System.createUUID()

	override var propagationDelay: Long? = null

	override var script: String? = null

	override var purelyScripted: Boolean = false

	override val elementsCount: Int
		get() = _elements.size

	override var parameterDefinitions: GraphParamDefinitions = GraphParamDefinitions()

	override val elements: ImmutableList<GraphElement>
		get() = ImmutableList(_elements)

	override val graphInputs: ImmutableList<GraphInput<*>>
		get() = ImmutableList(_elements
			.filter { it is GraphInput<*> && it.portType == PortType.INPUT }
			.map { it as GraphInput<*> })

	override val graphOutputs: ImmutableList<GraphOutput<*>>
		get() = ImmutableList(_elements
			.filter { it is GraphOutput<*> && it.portType == PortType.OUTPUT }
			.map { it as GraphOutput<*> })

	override val graphInOuts: ImmutableList<BidirectionalGraphPort<*>>
		get() = ImmutableList(_elements
			.filter { it is BidirectionalGraphPort<*> && it.portType == PortType.INOUT }
			.map { it as BidirectionalGraphPort<*> })

	override val graphPorts: ImmutableList<GraphPort<*>>
		get() = ImmutableList(_elements
			.filterIsInstance<GraphPort<*>>())

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			for (e in _elements) {
				if (!e.accept(visitor)) {
					break
				}
			}
		}
		return visitor.visitLeave(this)
	}

	override fun add(graphElement: GraphElement): Graph {
		if (!_elements.contains(graphElement)) {
			graphElement.id = getMaxId() + 1
			ensureUniqueGraphPortName(graphElement)
			_elements.add(graphElement)
			handleGraphElementAdded(graphElement)
			eventBus.post(GraphElementAddedEvent(this, graphElement))
		}
		return this
	}

	override fun remove(graphElement: GraphElement): Graph {
		if (_elements.contains(graphElement)) {
			if (graphElement is Net<*>) {
				handleNetRemoved(graphElement)
			} else if (graphElement is Vertice) {
				handleVerticeRemoved(graphElement)
			}
			_elements.remove(graphElement)
			handleGraphElementRemoved(graphElement)
			eventBus.post(GraphElementRemovedEvent(this, graphElement))
		}
		return this
	}

	override fun clear(): Graph {
		val iter = _elements.iterator()
		for (e in iter) {
			iter.remove()
			handleGraphElementRemoved(e)
			eventBus.post(GraphElementRemovedEvent(this, e))
		}
		return this
	}

	override fun contains(graphElement: GraphElement): Boolean {
		return _elements.contains(graphElement)
	}

	override fun withId(id: Int): GraphElement? {
		return _elements.firstOrNull { it.id == id }
	}

	override fun bind(repository: MetaGraphRepository, storableCreator: StorableCreator) {
		_elements.forEach { it.bind(repository, storableCreator) }
	}

	override fun formNet(signalHandler: SignalHandler) {
		_elements.forEach { it.formNet(signalHandler) }
	}

	override fun executionInitialize(signalHandler: SignalHandler) {
		_elements.forEach { it.executionInitialize(signalHandler) }
	}

	override fun executionStart(signalHandler: SignalHandler) {
		_elements.forEach { it.executionStart(signalHandler) }
	}


	override fun executionStopped(signalHandler: SignalHandler) {
		_elements.forEach { it.executionStopped(signalHandler) }
	}

	override fun <T : Any> getGraphPort(name: String): GraphPort<T>? {
		return _elements.firstOrNull { it is GraphPort<*> && it.name == name } as GraphPort<T>?
	}

	override fun <T : Any> getGraphInput(name: String): GraphInput<T>? {
		val input = graphInputs.firstOrNull { it.name == name }
		if (input != null) {
			return input as GraphInput<T>
		}
		return getGraphInputOutput(name) as GraphInput<T>?
	}

	override fun <T : Any> getGraphOutput(name: String): GraphOutput<T>? {
		val output = graphOutputs.firstOrNull { it.name == name }
		if (output != null) {
			return output as GraphOutput<T>
		}
		return getGraphInputOutput(name) as GraphOutput<T>?
	}

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	override fun write(writer: StoreWriter) {
		script?.let { writer.writeString("script", it) }
		if (purelyScripted) {
			writer.writeBoolean("purelyScripted", purelyScripted)
		}
		propagationDelay?.let { writer.writeLong("propDelay", it) }
		if (parameterDefinitions.isNotEmpty) {
			writer.writeStorable("params", parameterDefinitions)
		}
		writer.writeStorables("elements", _elements.iterator())
	}

	override fun read(reader: StoreReader) {
		if (reader.hasAttribute("script")) {
			script = reader.readString("script")
		}
		if (reader.hasAttribute("purelyScripted")) {
			purelyScripted = reader.readBoolean("purelyScripted")
		}
		if (reader.hasAttribute("propDelay")) {
			propagationDelay = reader.readLong("propDelay")
		}
		if (reader.hasElement("params")) {
			parameterDefinitions = reader.readStorable("params")
		}
		_elements.clear()
		reader.readStorables<GraphElement>("elements").forEach {
			_elements.add(it)
			handleGraphElementAdded(it)
		}
	}

	/** ---- [GraphImpl] */

	/** Called by this [GraphImpl] when a [GraphElement] has been added or read as [Storable].*/
	protected open fun handleGraphElementAdded(graphElem: GraphElement) {
		if (graphElem is OscilloscopeProbeVertice<*>) {
			LOG.trace("added OscilloscopeProbeVertice ${graphElem.getInput<Any>().name} to GraphImpl")
			graphElem.addGraphElementListener(oscilloscopeProbeHandler)
		}
	}

	protected open fun handleGraphElementRemoved(graphElem: GraphElement) {
		if (graphElem is OscilloscopeProbeVertice<*>) {
			LOG.trace("removed OscilloscopeProbeVertice ${graphElem.getInput<Any>().name} from GraphImpl")
			graphElem.removeGraphElementListener(oscilloscopeProbeHandler)
		}
	}

	private fun handleNetRemoved(net: Net<*>) {
		net.ports.toList().forEach { net.unconnect(it) }
	}

	private fun handleVerticeRemoved(vertice: Vertice) {
		vertice.getInputs().filter { it.net != null }.forEach { it.net!!.unconnect(it) }
		vertice.getOutputs().filter { it.net != null }.forEach { it.net!!.unconnect(it) }
	}

	private fun getMaxId(): Int {
		if (elementsCount == 0) {
			return 0
		}
		return _elements.maxByOrNull { it.id }!!.id
	}

	private fun getGraphInputOutput(name: String): BidirectionalGraphPort<*>? {
		return graphInOuts.firstOrNull { it.name == name }
	}

	/** Creates unique names for [GraphPort]s.*/
	private fun ensureUniqueGraphPortName(graphElement: GraphElement) {
		if (graphElement is GraphPort<*>) {
			when (graphElement.portType) {
				PortType.INPUT -> ensureUniqueGraphPortName("I", graphInputs, graphElement)
				PortType.OUTPUT -> ensureUniqueGraphPortName("O", graphOutputs, graphElement)
				PortType.INOUT -> ensureUniqueGraphPortName("IO", graphInOuts, graphElement)
			}
		}
	}

	private fun ensureUniqueGraphPortName(prefix: String, graphPorts: ImmutableList<GraphPort<*>>, graphPort: GraphPort<*>) {
		var name = graphPort.name
		if (name == null || existsGraphPortNameExcluding(name, graphPort)) {
			name = prefix + (graphPorts.size + 1)
		}
		while (existsGraphPortNameExcluding(name, graphPort)) {
			name += "1"
		}
		if (graphPort.name != name) {
			graphPort.name = name
		}
	}

	private fun existsGraphPortNameExcluding(name: String, excludedGraphPort: GraphPort<Any>): Boolean {
		return graphPorts.any { it != excludedGraphPort && it.name == name }
	}

	private fun getOscilloscope(): Oscilloscope? {
		return elements.firstOrNull() { it is Oscilloscope } as Oscilloscope?
	}

	/** Forwards signal changes of a [OscilloscopeProbeVertice] to the [Oscilloscope].*/
	private inner class OscilloscopeProbeHandler : GraphElementAdapter() {
		override fun stateChanged(e: GraphElementEvent) {
			val probePort = (e.element as OscilloscopeProbeVertice<*>).getInput<Any>()
			if (probePort.getIncomingSignal() != null && e.signalHandler != null) {
				val oscilloscopePort = getOscilloscope()?.getPort<Any>(probePort.name!!) as InputPort<Any>
				oscilloscopePort.setIncomingSignal(probePort.getIncomingSignal(), e.signalHandler)
			}
		}
	}
}