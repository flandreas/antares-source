package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.dsl.Interpreter
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.dsl.ScriptMetaData
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.execution.actor.ActorState
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.dsl.GraphDslInterpreter
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.graph.model.param.GraphParamValues
import ch.scorpion.jabbah.graph.model.vertice.GraphReferenceState.*
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.BrokenReferenceView
import ch.scorpion.jabbah.io.*

/** Represents the possible states of a [GraphReference]. */
private enum class GraphReferenceState {

	/** The [Graph] has not yet been loaded.*/
	OPEN,

	/** Successfully loaded.*/
	RESOLVED,

	/** Tried to load the [Graph], but the reference was broken.*/
	BROKEN
}

/**
 * Used by [SubGraphVerticeRef] for deferred referencing of its [Graph].
 * In addition, remembers whether a [Graph] could not be loaded due to a
 * broken reference, if the referenced [Graph] had been deleted in the meantime.
 */
private data class GraphReference(
	val state: GraphReferenceState,
	val graph: Graph?
) {
	companion object {
		fun open(): GraphReference = GraphReference(OPEN, null)
		fun resolved(graph: Graph): GraphReference = GraphReference(RESOLVED, graph)
		fun broken(): GraphReference = GraphReference(BROKEN, null)
	}
}

/**
 * A [SubGraphVertice] implementation that is part of one [Graph] and references another [Graph] in the [Library].
 */
class SubGraphVerticeRef(
	override var graphUUID: UUID? = null,
	var graphType: GraphType = GraphModelModule.defaultGraphType,
	private val repository: MetaGraphRepository = LibraryModule.libraryHolder
) : CalculatingVertice(CALCULATOR), SubGraphVertice, NetCombiner {

	companion object {

		private val LOG by logger(SubGraphVerticeRef::class)

		val CALCULATOR = object : VerticeCalculator<SubGraphVerticeRef> {
			override fun calculate(vertice: SubGraphVerticeRef, data: GraphActorData, signalHandler: SignalHandler) {
				if (data.isInput && !vertice.isDeepExecution(signalHandler.isDeepExecution)) {
					vertice.runExecutionScript(data)
				}
			}
		}

		/** Creates a new [SubGraphVerticeRef] using the data in the specified [SubGraphVertice].*/
		fun fromSubGraphVertice(
			graphType: GraphType,
			subGraphVertice: SubGraphVertice,
			repository: MetaGraphRepository
		): SubGraphVerticeRef {
			val verticeRef = SubGraphVerticeRef(null, graphType, repository)
			verticeRef.fillFrom(subGraphVertice)
			return verticeRef
		}
	}

	private var graphReference = GraphReference.open()

	/**
	 * Instantiated for execution purposes if demanded by [GraphType.needsGraphViewForExecution]
	 * at the moment when [graph] is instantiated.
	 */
	private var graphView: GraphView? = null

	/** Can be set during [read] if reference to [MetaGraph] is broken. */
	private var _designError: DesignError? = null

	private val hasDesignError: Boolean get() = designError != null

	/** Interprets the script in [Graph.script] during execution (if required by system parameters). */
	private var interpreter: Interpreter? = null

	private var isDeepExecutionCache: Boolean? = null

	var paramValues = GraphParamValues()
		private set(value) {
			field = value
			getGraphIfPresent()?.let {
				it.parameterValues = field
			}
			synchronizePorts()
			stateChanged(null)
		}

	private val executionMetaData by lazy {
		ScriptMetaData(type, Translations.getString("graph.property.GraphViewImpl.script.name"))
	}

	/** ---- [GraphElement] interface */

	override val designError: DesignError? get() = _designError

	override var type: String = "" // lateinit not possible with custom setter
		set(value) {
			if (field != value) {
				field = value
				stateChanged(null, Vertice.STATE_CHANGE_TYPE)
			}
		}

	private var _typeDesc: String? = null
	override val typeDesc: String? get() = _typeDesc

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			graphReference.graph?.accept(visitor)
		}
		return visitor.visitLeave(this)
	}

	override fun graphParamsChanged(graph: Graph) {
		var newParamValues: GraphParamValues = paramValues
		var changed = false
		for (paramValue in paramValues.values) {
			newParamValues = newParamValues.withValue(paramValue.evaluateIn(graph))
			changed = true
		}
		if (changed) {
			paramValues = newParamValues
		}
	}

	/** ---- [SubGraphVertice] */

	override var graphName: Name = Name(TranslatableText())

	override fun <T : Any> propagateOutput(outputPort: SubGraphOutputPort<T>, signal: T, signalHandler: SignalHandler) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("propagateOutput for Output '${outputPort.name}'")
		}
		if (state == ActorState.Waiting) {
			signalHandler.actPrematurely(this, null)
		}
		outputPort.setOutgoingSignalBuffered(signal, signalHandler)
		outputChanged(outputPort, signalHandler)
	}

	/** ---- [Storable] interface */

	override val storesName: Boolean get() = false

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("uuid", graphUUID!!.id)
		writer.writeString("type", graphType.customName)
		if (paramValues.isNotEmpty) {
			writer.writeStorable("params", paramValues)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		graphUUID = UUID(reader.readString("uuid"))

		reader.requestResolution(this, Reference("metaGraph"))

		// Establish GraphParamValues AFTER GraphElements have been read
		if (reader.hasElement("params")) {
			//paramValues = reader.readStorable("params")
			reader.requestResolution(this, Reference("params", additionalInfo = reader.readStorable("params")))
		}
		graphType = if (reader.hasAttribute("type")) {
			GraphModelModule.graphTypeRegistry.withCustomName(reader.readString("type"))
		} else {
			// Backward compatibility
			GraphModelModule.defaultGraphType
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		super.resolve(reference, referenceResolver)

		if (reference.name == "params") {
			paramValues = reference.additionalInfo as GraphParamValues
		}

		if (reference.name == "metaGraph") {
			val metaGraph = repository.getOptionalMetaGraph(graphUUID!!)
			if (metaGraph != null) {
				name = metaGraph.name
				graphName = metaGraph.containerDrawing.model.graphName
				fillFrom(metaGraph.containerDrawing.createSubGraphVertice())

				if (paramValues.isNotEmpty) {
					getGraph(repository).parameterValues = paramValues
					synchronizePorts()
				}

				if (metaGraph.graph.model!!.overallPropagationDelay != null) {
					propagationDelay = metaGraph.graph.model!!.overallPropagationDelay!!
				}
			} else {
				// Broken reference to library component
				LOG.warn("broken reference $graphUUID")
				graphName = Name(BrokenReferenceView.NAME)
				type = graphName.value
				_designError = DesignError("graph.designError.brokenSubGraphRef.text")
				graphReference = GraphReference.broken()
			}
		}
	}

	/** ---- [Actor] */

	override fun executionInitialize(signalHandler: SignalHandler) {
		isDeepExecutionCache = null

		super.executionInitialize(signalHandler)
		if (!hasDesignError) {
			if (isDeepExecution(signalHandler.isDeepExecution)) {
				graphReference.graph?.executionInitialize(signalHandler)
			}
		}
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		if (!hasDesignError) {
			if (isDeepExecution(signalHandler.isDeepExecution)) {
				graphReference.graph?.executionStart(signalHandler, graphView)
			} else {
				interpreter = createInterpreter(signalHandler)
				if (interpreter is GraphDslInterpreter) {
					(interpreter as GraphDslInterpreter).executionStarted()
				}
				requestActingAfter(signalHandler, propagationDelay, createActorData(null))
			}
		}
	}

	/**
	 * Adds a global context [SubGraphVerticeRefActivationRecord] to the created [Interpreter] that
	 * allows scripts to access input and output values as variables.
	 */
	private fun createInterpreter(signalHandler: SignalHandler): Interpreter? {
		return repository.getContainerLibraryElement(graphUUID!!).let { cle ->
			cle?.executionScriptAST?.let { ast ->
				BaseModule.interpreterFactory(
					ast,
					Memory(GraphModelModule.subGraphVerticeRefActivationRecordFactory(this, signalHandler)))
			}
		}
	}

	override fun actImpl(signalHandler: SignalHandler, data: ActorData) {
		if ((data as GraphActorData).isInput && !isDeepExecution(signalHandler.isDeepExecution)) {
			super.actImpl(signalHandler, data)
		}
		// With deep execution, no execution logic by the [SubGraphVerticeRef] has to take place.
		// In particular, the OutputPort must NOT be flushed, because that would lead to outgoing signal animations
		// even before the inner Graph has been executed.
		// Note however that acting MUST have been requested by inputChanged() even if it supposed to do nothing, because
		// only this triggers execution animations of the [SubGraphVerticeRef] on the view level.
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		if (isDeepExecution(signalHandler.isDeepExecution)) {
			graphReference.graph?.executionStopped(signalHandler)
		}
		isDeepExecutionCache = null
	}

	override fun formNet(signalHandler: SignalHandler) {
		super.formNet(signalHandler)
		if (!hasDesignError) {
			if (isDeepExecution(signalHandler.isDeepExecution)) {
				graphReference.graph?.formNet(signalHandler)
			}
		}
	}

	private fun runExecutionScript(data: GraphActorData?) {
		interpreter?.interpretCatching(executionMetaData, params = data)
	}

	/** ---- [AbstractVertice] */

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler, force: Boolean) {
		if (isDeepExecution(signalHandler.isDeepExecution)) {
			val graphInput = (input as SubGraphInputPort<Any>).graphInput
			signalHandler.logActorTrace(this) {
				"input port ${input.portId} of SubGraphVertice changed to ${input.getIncomingSignal()}"
			}
			graphInput?.setIncomingSignal(
				graphType.adaptTo<Any, Any>(ensureGraph().type).convertIncomingSignal(input.getIncomingSignal()),
				signalHandler,
				force)
		}
		// This will eventually call the VerticeCalculator which will execute the script (if not deeply executing).
		// Even if deeply executing, we need to request acting, because only that will initiate
		// calculation animations for this [SubGraphVerticeRef] on the view layer.
		super.inputChanged(input, signalHandler, force)
	}

	/** ---- [SubGraphVertice] */

	override fun getGraphIfPresent(): Graph? = graphReference.graph

	override fun getGraphIfNotBroken(): Graph? {
		return when (graphReference.state) {
			OPEN -> ensureGraph()
			RESOLVED -> graphReference.graph
			BROKEN -> null
		}
	}

	override fun bind(deep: Boolean, repository: MetaGraphRepository) {
		super.bind(deep, repository)
		if (!hasDesignError && isDeepExecution(deep)) {
			ensureGraph().bind(deep, repository)
		}
	}

	override fun getGraph(repository: MetaGraphRepository): Graph {
		return ensureGraph()
	}

	private fun ensureGraph(): Graph {
		if (graphReference.graph != null) {
			return graphReference.graph!!
		}

		if (graphReference.state == BROKEN) {
			throw IllegalStateException("broken SubGraphVerticeRef")
		}

		val subMetaGraph = repository.getMetaGraph(graphUUID!!)
		graphReference = GraphReference.resolved(subMetaGraph.cloneGraphModel())

		if (graphReference.graph!!.type.needsGraphViewForExecution) {
			graphView = subMetaGraph.graph.graphView.cloneForExistingModel(graphReference.graph!!)
		}

		for (input in getSubGraphInputPorts()) {
			input.graphInput = graphReference.graph!!.getGraphInput(input.name!!)
			input.graphInput!!.subGraphInputPort = input
		}
		for (output in getSubGraphOutputPorts()) {
			val graphOutput = graphReference.graph!!.getGraphOutput<Any>(output.name!!)
			if (graphOutput != null) {
				graphOutput.subGraphOutputPort = output
			} else {
				LOG.error("Cannot find GraphOutput '${output.name}' in '${graphReference.graph!!.name}' (${graphReference.graph!!.uuid})")
				// TODO Throw exception for global error display
			}
		}

		return graphReference.graph!!
	}

	/** ---- [NetCombiner] */

	override fun <T : Any> createCombinedNetsFor(outputPort: OutputPort<T>, inputPort: InputPort<T>, signalHandler: SignalHandler): Collection<CombinedNet<T>> {
		if (!isDeepExecution(signalHandler.isDeepExecution)) {
			return emptyList()
		}
		val graphInput = ensureGraph().getGraphInput<T>(inputPort.name!!)
		val innerOutputPort = graphInput!!.getOutput<T>()
		val result = CombinedNet.createFor(innerOutputPort, signalHandler)

		result.forEach { it.replaceAccessPort(innerOutputPort, outputPort) }

		return result
	}

	override val isNetCombiner: Boolean
		get() = super.isNetCombiner && ensureGraph().type.isCombiningNets

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean =
		!isDeepExecution(signalHandler.isDeepExecution) && (graphReference.graph == null || graphReference.graph!!.type.isCombiningNets)

	/** ---- [SubGraphVerticeRef] */

	fun setParamValue(paramValue: GraphParamValue<*>) {
		val newParamValues = paramValues.withValue(paramValue)

		// First forward to Graph. Local update will then inform Views to sync their state,
		// which depends on the Graph.
		getGraph(repository).parameterValues = newParamValues

		paramValues = newParamValues
	}

	private fun fillFrom(subGraphVertice: SubGraphVertice) {
		graphUUID = subGraphVertice.graphUUID

		type = subGraphVertice.graphName.value
		_typeDesc = subGraphVertice.description.value
		graphName = subGraphVertice.graphName

		for (port in subGraphVertice.getPorts()) {
			addPort(port, port.portId)
		}

		setDefaultParamValues()
	}

	fun isDeepExecution(deepExecution: Boolean): Boolean {
		if (isDeepExecutionCache == null) {
			isDeepExecutionCache = (graphReference.graph ?: repository.getMetaGraph(graphUUID!!).graph.model!!).let {
				!it.purelyScripted && deepExecution || StringUtils.isEmpty(it.script)
			}
		}
		return isDeepExecutionCache!!
	}

	private fun getSubGraphInputPorts(): ImmutableList<SubGraphInputPort<Any>> =
		getInputs().map { it as SubGraphInputPort<Any> }.toImmutableList()

	private fun getSubGraphOutputPorts(): ImmutableList<SubGraphOutputPort<Any>> =
		getOutputs().map { it as SubGraphOutputPort<Any> }.toImmutableList()

	private fun setDefaultParamValues() {
		graphUUID?.let { uuid ->
			repository.getMetaGraph(uuid).graph.model?.parameterValues?.let {
				paramValues = it
			}
		}
	}

	private fun synchronizePorts() {
		if (paramValues.isNotEmpty) {
			with(ensureGraph()) {
				for (port in getPorts()) {
					val graphPort = getGraphPort<Any>(port.name!!)
					if (graphPort != null && port is SubGraphPort<*>) {
						port.handleGraphPortChanged(graphPort)
					}
				}
			}
		}
	}
}