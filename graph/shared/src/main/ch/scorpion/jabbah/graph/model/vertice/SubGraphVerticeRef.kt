package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.dsl.Interpreter
import ch.scorpion.jabbah.base.dsl.Memory
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
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import ch.scorpion.jabbah.graph.model.net.NetCombiner
import ch.scorpion.jabbah.graph.view.vertice.BrokenReferenceView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [SubGraphVertice] implementation that is part of one [Graph] and references another [Graph] in the [Library].
 */
class SubGraphVerticeRef(
	graphUUID: UUID? = null,
	private val repository: MetaGraphRepository = GraphModelModule.metaGraphRepository
) : CalculatingVertice(CALCULATOR), SubGraphVertice, NetCombiner {

	companion object {

		private val LOG by logger(SubGraphVerticeRef::class)

		val CALCULATOR = object : VerticeCalculator<SubGraphVerticeRef> {
			override fun calculate(vertice: SubGraphVerticeRef, data: GraphActorData, signalHandler: SignalHandler) {
				if (data.isInput && !vertice.isDeepExecution(signalHandler)) {
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

	override val designError: DesignError? get() = _designError

	/** ---- [GraphElement] interface */

	override lateinit var type: String

	private var _typeDesc: String? = null
	override val typeDesc: String? get() = _typeDesc

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			graph?.accept(visitor)
		}
		return visitor.visitLeave(this)
	}

	/** ---- [SubGraphVertice] */

	override var graphUUID: UUID? = graphUUID

	override var graphName: Name = Name(TranslatableText())

	override fun <T : Any> propagateOutput(outputPort: SubGraphOutputPort<T>, signal: T, signalHandler: SignalHandler) {
		LOG.trace("propagateOutput for Output '${outputPort.name}'")
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
	}

	override fun read(reader: StoreReader) {
		graphUUID = UUID(reader.readString("uuid"))

		val metaGraph = repository.getOptionalMetaGraph(graphUUID!!)
		if (metaGraph != null) {
			name = metaGraph.name
			graphName = metaGraph.containerDrawing.model.graphName
			fillFrom(metaGraph.containerDrawing.createSubGraphVertice())

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
		super.executionInitialize(signalHandler)
		if (!hasDesignError) {
			if (isDeepExecution(signalHandler)) {
				graph?.executionInitialize(signalHandler)
			}
		}
	}

	override fun executionStart(signalHandler: SignalHandler) {
		super.executionStart(signalHandler)
		if (!hasDesignError) {
			if (isDeepExecution(signalHandler)) {
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
		if ((data as GraphActorData).isInput && !isDeepExecution(signalHandler)) {
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
	}

	override fun formNet(signalHandler: SignalHandler) {
		super.formNet(signalHandler)
		if (!hasDesignError) {
			if (isDeepExecution(signalHandler)) {
				graph?.formNet(signalHandler)
			}
		}
	}

	private fun runExecutionScript(data: GraphActorData?) {
		try {
			interpreter?.interpret(params = data)
		} catch (e: DslError) {
			// TODO I18N
			BaseModule.eventBus.post(IssueImpl(
				severity = IssueSeverity.Error,
				name = "Runtime Error",
				description = e.message,
				origin = type,
				context = "Subcircuit Logic"
			))
		}
	}

	/** ---- [AbstractVertice] */

	override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
		if (isDeepExecution(signalHandler)) {
			val graphInput = (input as SubGraphInputPort<Any>).graphInput
			signalHandler.logActorTrace(this) { "input port ${input.portId} of SubGraphVertice changed to ${input.getIncomingSignal()}" }
			graphInput!!.setIncomingSignal(input.getIncomingSignal(), signalHandler)
		}
		// This will eventually call the VerticeCalculator which will execute the script (if not deeply executing).
		// Even if deeply executing, we need to request acting, because only that will initiate
		// calculation animations for this [SubGraphVerticeRef] on the view layer.
		super.inputChanged(input, signalHandler)
	}

	/** ---- [SubGraphVertice] */

	override fun getGraphIfPresent(): Graph? {
		return graph
	}

	override fun bind(repository: MetaGraphRepository, storableCreator: StorableCreator) {
		super.bind(repository, storableCreator)
		if (!hasDesignError) {
			ensureGraph(storableCreator).bind(repository, storableCreator)
		}
	}

	override fun getGraph(repository: MetaGraphRepository, storableCreator: StorableCreator): Graph {
		return ensureGraph(storableCreator)
	}

	private fun ensureGraph(storableCreator: StorableCreator): Graph {
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
		if (!isDeepExecution(signalHandler)) {
			return emptyList()
		}
		val graphInput = graph!!.getGraphInput<T>(inputPort.name!!)
		val innerOutputPort = graphInput!!.getOutput<T>()
		val result = CombinedNet.createFor(innerOutputPort, signalHandler)

		result.forEach { it.replaceAccessPort(innerOutputPort, outputPort) }

		return result
	}

	override fun requiresCombinedNets(signalHandler: SignalHandler): Boolean =
		!isDeepExecution(signalHandler)

	/** ---- [SubGraphVerticeRef] */

	private fun fillFrom(subGraphVertice: SubGraphVertice) {
		graphUUID = subGraphVertice.graphUUID

		type = subGraphVertice.graphName.value
		_typeDesc = subGraphVertice.description.value

		for (port in subGraphVertice.getPorts()) {
			addPort(port, port.portId)
		}
	}

	private fun isDeepExecution(signalHandler: SignalHandler): Boolean =
		!graph!!.purelyScripted && signalHandler.isDeepExecution || graph!!.script == null || graph!!.script == ""

	private fun getSubGraphInputPorts(): ImmutableList<SubGraphInputPort<Any>> =
		getInputs().map { it as SubGraphInputPort<Any> }.toImmutableList()

	private fun getSubGraphOutputPorts(): ImmutableList<SubGraphOutputPort<Any>> =
		getOutputs().map { it as SubGraphOutputPort<Any> }.toImmutableList()
}