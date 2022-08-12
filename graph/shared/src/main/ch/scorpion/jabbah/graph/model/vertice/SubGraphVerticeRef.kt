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
import ch.scorpion.jabbah.graph.view.vertice.BrokenReferenceView
import ch.scorpion.jabbah.io.*

/**
 * A [SubGraphVertice] implementation that is part of one [Graph] and references another [Graph] in the [Library].
 */
class SubGraphVerticeRef(
	graphUUID: UUID? = null,
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
			subGraphVertice: SubGraphVertice,
			repository: MetaGraphRepository
		): SubGraphVerticeRef {
			val verticeRef = SubGraphVerticeRef(null, repository)
			verticeRef.fillFrom(subGraphVertice)
			return verticeRef
		}
	}

	private var graph: Graph? = null

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

	override val designError: DesignError? get() = _designError

	/** ---- [GraphElement] interface */

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
			graph?.accept(visitor)
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

	override var graphUUID: UUID? = graphUUID

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
		if (paramValues.isNotEmpty) {
			writer.writeStorable("params", paramValues)
		}
	}

	override fun read(reader: StoreReader) {
		graphUUID = UUID(reader.readString("uuid"))

		val metaGraph = repository.getOptionalMetaGraph(graphUUID!!)
		if (metaGraph != null) {
			name = metaGraph.name
			graphName = metaGraph.containerDrawing.model.graphName
			fillFrom(metaGraph.containerDrawing.createSubGraphVertice())

			// Establish GraphParamValues AFTER GraphElements have been read
			if (reader.hasElement("params")) {
				paramValues = reader.readStorable("params")
			}

			if (paramValues.isNotEmpty) {
				getGraph(repository, IOModule.storableCreator).parameterValues = paramValues
				synchronizePorts()
			}

			super.read(reader)

			if (metaGraph.graph.model!!.propagationDelay != null) {
				propagationDelay = metaGraph.graph.model!!.propagationDelay!!
			}
		} else {
			// Broken reference to library component
			LOG.warn("broken reference $graphUUID")
			graphName = Name(BrokenReferenceView.NAME)
			type = graphName.value
			super.read(reader)
			_designError = DesignError("graph.designError.brokenSubGraphRef.text")
		}
	}

	/** ---- [Actor] */

	override fun executionInitialize(signalHandler: SignalHandler) {
		isDeepExecutionCache = null

		super.executionInitialize(signalHandler)
		if (!hasDesignError) {
			if (isDeepExecution(signalHandler.isDeepExecution)) {
				graph?.executionInitialize(signalHandler)
			}
		}
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		if (!hasDesignError) {
			if (isDeepExecution(signalHandler.isDeepExecution)) {
				graph?.executionStart(signalHandler)
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
					Memory(GraphModelModule.subGraphVerticeRefActivationRecordFactory.create(this, signalHandler)))
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
		graph?.executionStopped(signalHandler)
		isDeepExecutionCache = null
	}

	override fun formNet(signalHandler: SignalHandler) {
		super.formNet(signalHandler)
		if (!hasDesignError) {
			if (isDeepExecution(signalHandler.isDeepExecution)) {
				graph?.formNet(signalHandler)
			}
		}
	}

	private fun runExecutionScript(data: GraphActorData?) {
		interpreter?.interpretCatching(
			ScriptMetaData(type, Translations.getString("graph.property.GraphViewImpl.script.name")),
			params = data)
	}

	/** ---- [AbstractVertice] */

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler, force: Boolean) {
		if (isDeepExecution(signalHandler.isDeepExecution)) {
			val graphInput = (input as SubGraphInputPort<Any>).graphInput
			signalHandler.logActorTrace(this) { "input port ${input.portId} of SubGraphVertice changed to ${input.getIncomingSignal()}" }
			graphInput?.setIncomingSignal(input.getIncomingSignal(), signalHandler, force)
		}
		// This will eventually call the VerticeCalculator which will execute the script (if not deeply executing).
		// Even if deeply executing, we need to request acting, because only that will initiate
		// calculation animations for this [SubGraphVerticeRef] on the view layer.
		super.inputChanged(input, signalHandler, force)
	}

	/** ---- [SubGraphVertice] */

	override fun getGraphIfPresent(): Graph? = graph

	override fun bind(deep: Boolean, repository: MetaGraphRepository, storableCreator: StorableCreator) {
		super.bind(deep, repository, storableCreator)
		if (deep && !hasDesignError) {
			ensureGraph(storableCreator).bind(deep, repository, storableCreator)
		}
	}

	override fun getGraph(repository: MetaGraphRepository, storableCreator: StorableCreator): Graph {
		return ensureGraph(storableCreator)
	}

	private fun ensureGraph(storableCreator: StorableCreator = IOModule.storableCreator): Graph {
		if (graph != null) {
			return graph!!
		}

		val subGraph = repository.getMetaGraph(graphUUID!!)
		graph = subGraph.cloneGraphModel(storableCreator)

		for (input in getSubGraphInputPorts()) {
			input.graphInput = graph!!.getGraphInput(input.name!!)
			input.graphInput!!.subGraphInputPort = input
		}
		for (output in getSubGraphOutputPorts()) {
			val graphOutput = graph!!.getGraphOutput<Any>(output.name!!)
			if (graphOutput != null) {
				graphOutput.subGraphOutputPort = output
			} else {
				LOG.error("Cannot find GraphOutput '${output.name}' in '${graph!!.name}' (${graph!!.uuid})")
				// TODO Throw exception for global error display
			}
		}

		return graph!!
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

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean =
		!isDeepExecution(signalHandler.isDeepExecution)

	/** ---- [SubGraphVerticeRef] */

	fun setParamValue(paramValue: GraphParamValue<*>) {
		val newParamValues = paramValues.withValue(paramValue)

		// First forward to Graph. Local update will then inform Views to sync their state,
		// which depends on the Graph.
		getGraph(repository, IOModule.storableCreator).parameterValues = newParamValues

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
			isDeepExecutionCache = (graph ?: repository.getMetaGraph(graphUUID!!).graph.model!!).let {
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
			with(ensureGraph(IOModule.storableCreator)) {
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