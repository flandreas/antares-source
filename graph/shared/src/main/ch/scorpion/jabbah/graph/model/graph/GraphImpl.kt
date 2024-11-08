package ch.scorpion.jabbah.graph.model.graph

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.SymbolTable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyOwner
import ch.scorpion.jabbah.base.event.PropertyOwnerImpl
import ch.scorpion.jabbah.base.event.VetoException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.parser.Parser
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.*
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.Graph.Companion.PROP_DESCRIPTION
import ch.scorpion.jabbah.graph.model.Graph.Companion.PROP_NAME
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.nonvolatile.NonVolatileStorable
import ch.scorpion.jabbah.graph.model.oscilloscope.Oscilloscope
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.graph.model.param.GraphParamValues
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.*

/**
 * A standard implementation of the [Graph] interface.
 */
open class GraphImpl(
	name: TranslatableText = TranslatableText(Translations.getString("graph.name.unknown")),
	type: GraphType = GraphModelModule.graphTypeRegistry.default,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val propertyOwner: PropertyOwner<Any> = PropertyOwnerImpl()
) : AbstractStorable(), Graph, Namable, Describable, PropertyOwner<Any> by propertyOwner {

	companion object {
		private val LOG by logger(GraphImpl::class)
	}

	private val _elements = mutableListOf<GraphElement>()

	/** Listens for [GraphElementEvent]s from child [GraphElement]s. */
	private val elementListener = GraphElementListener()

	/** Forwards signal changes of a [OscilloscopeProbeVertice] to the [Oscilloscope].*/
	private val oscilloscopeProbeHandler = OscilloscopeProbeHandler()

	init {
	    propertyOwner.source = this
	}

	override fun dispose() {}

	/** ---- [Namable], [Describable] interfaces */

	override var name: Name by observableName(Name(name)) {
		if (it.isEmpty || StringUtils.isBlank(it.value)) {
			throw IllegalArgumentException(Translations.getString("edit.property.name.empty.error"))
		}
		propertyOwner.fire(PROP_NAME, null, it)
	}

	override var description: Description by observableDescription(Description("")) {
		propertyOwner.fire(PROP_DESCRIPTION, null, it)
	}

	/** ---- [Graph] interface */

	override var type: GraphType = type

	override var uuid: UUID = System.createUUID()

	override var overallPropagationDelay: Long? = null

	override var startupTime: Long? = null
		set(value) {
			field = if (value == 0L) {
				null
			} else {
				if (value != null) {
					require(value >= 0) {"Startup time must be greater than 0"}
				}
				value
			}
		}

	override var script: String? = null

	override var purelyScripted: Boolean = false

	override val elementsCount: Int
		get() = _elements.size

	override val symbolTable: SymbolTable by lazy { GraphSymbolTable(this) }

	override var parameterDefinitions: GraphParamDefinitions = GraphParamDefinitions()
		set(value) {
			if (field !== value) {
				field = value
				parameterValues = GraphParamValues.withDefaults(field)
			}
		}

	override var parameterValues: GraphParamValues = GraphParamValues()
		set(value) {
			field = value
			_elements.forEach {
				try {
					it.graphParamsChanged(this)
				} catch (e: DslError) {
					eventBus.post(IssueImpl(
						severity = IssueSeverity.Error,
						name = Translations.getString("base.dsl.scriptError.msg"),
						description = e.message,
						origin = "${it.type} ${it.id}",
						context = null))
				}
			}
		}

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
			graphElement.addGraphElementListener(elementListener)
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
			graphElement.removeGraphElementListener(elementListener)
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

	override fun bind(deep: Boolean, repository: MetaGraphRepository) {
		_elements.forEach { it.bind(deep, repository) }
	}

	override fun formNet(signalHandler: SignalHandler) {
		_elements.forEach { it.formNet(signalHandler) }
	}

	override fun checkDesign(signalHandler: SignalHandler, eventBus: EventBus): Boolean {
		val issues = _elements
			.filter { it.designError != null }
			.groupBy { it }
			.map { it.value.first() }
			.map {
				IssueImpl(
					severity = IssueSeverity.Error,
					name = Translations.getString("graph.designError.name"),
					description = it.designError?.description,
					origin = "${it.type} (${it.id})",
					context = name.value
				)
			}
		issues.forEach { eventBus.post(it) }

		val hasChildIssues = _elements
			.filterIsInstance<SubGraphVerticeRef>()
			.map {
				if (it.isDeepExecution(signalHandler.isDeepExecution)) {
					it.getGraph()
				} else {
					null
				}
			}
			.map { it?.checkDesign(signalHandler, eventBus) }
			.any { it == false }

		return issues.isEmpty() && !hasChildIssues
	}

	override fun executionInitialize(signalHandler: SignalHandler, nonVolatileData: NonVolatileStorable?) {
		signalHandler.executionContext = createGraphExecutionContext<Any>()
		_elements.forEach {
			it.executionInitializeNonVolatile(signalHandler, nonVolatileData?.getChild(it.id))
		}
	}

	override fun executionStart(signalHandler: SignalHandler, graphView: GraphView?) {
		_elements.forEach { it.executionStart(signalHandler) }
	}

	override fun executionStopped(signalHandler: SignalHandler, nonVolatileData: NonVolatileStorable?) {
		_elements.forEach { it.executionStoppedNonVolatile(signalHandler, nonVolatileData) }
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

	override fun createParser(program: String, semanticAnalyser: SemanticAnalyser?): Parser =
		BaseModule.parserFactory(
			program,
			BaseModule.semanticAnalyserFactory(symbolTable))

	override fun handleSubGraphNameChanged(uuid: UUID) {
		_elements
			.filterIsInstance<SubGraphVerticeRef>()
			.filter { it.graphUUID == uuid }
			.forEach { vRef ->
				vRef.handleTypeChanged()
			}
	}

	/** ---- [Storable] interface */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	override fun write(writer: StoreWriter) {
		writer.writeString("type", type.customName)
		script?.let { writer.writeString("script", it) }
		if (purelyScripted) {
			writer.writeBoolean("purelyScripted", purelyScripted)
		}
		overallPropagationDelay?.let { writer.writeLong("propDelay", it) }
		writer.writeStorables("elements", _elements.iterator())
		startupTime?.let {
			if (it > 0) {
				writer.writeLong("startupTime", it)
			}
		}
	}

	override fun read(reader: StoreReader) {
		if (reader.hasAttribute("type")) {
			type = GraphModelModule.graphTypeRegistry.withCustomName(reader.readString("type"))
		}
		if (reader.hasAttribute("script")) {
			script = reader.readString("script")
		}
		if (reader.hasAttribute("purelyScripted")) {
			purelyScripted = reader.readBoolean("purelyScripted")
		}
		if (reader.hasAttribute("propDelay")) {
			overallPropagationDelay = reader.readLong("propDelay")
		}
		if (reader.hasAttribute("startupTime")) {
			startupTime = reader.readLong("startupTime")
		}
		_elements.clear()
		reader.readStorables<GraphElement>("elements").forEach {
			_elements.add(it)
			it.addGraphElementListener(elementListener)
			handleGraphElementAdded(it)
		}
	}

	/** ---- [GraphImpl] */

	protected open fun <T: Any> createGraphExecutionContext(): GraphExecutionContext<T> = GraphExecutionContext()

	private fun handle(event: GraphPortNameChanged<Any>) {
		if (event.newName != null &&  contains(event.graphPort)) {
			if (existsGraphPortNameExcluding(event.newName, event.graphPort)) {
				if (parameterDefinitions.contains(event.newName)) {
					throw VetoException(Translations.getString("graph.port.nameConflictsWithParam.msg"))
				}
				throw VetoException(Translations.getString("graph.port.nameAlreadyExists.msg"))
			}
		}
	}

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
			|| parameterDefinitions.contains(name)
	}

	private fun getOscilloscope(): Oscilloscope? {
		return elements.firstOrNull() { it is Oscilloscope } as Oscilloscope?
	}

	private inner class GraphElementListener : GraphElementAdapter() {
		override fun checkStateChange(e: GraphElementEvent) {
			if (e is GraphPortNameChanged<*>) {
				handle(e)
			}
		}
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