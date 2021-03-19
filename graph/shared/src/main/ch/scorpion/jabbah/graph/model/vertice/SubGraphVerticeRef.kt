package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.execution.actor.ActorData
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
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
	private val repository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
	private val scriptGateway: ScriptGateway = ScriptModule.scriptGateway
) : CalculatingVertice(CALCULATOR), SubGraphVertice {

	companion object {

		private val LOG by logger(SubGraphVerticeRef::class)

		val CALCULATOR = object : VerticeCalculator<SubGraphVerticeRef> {
			override fun calculate(vertice: SubGraphVerticeRef, data: GraphActorData, signalHandler: SignalHandler) {
				if (data.isInput && !vertice.isDeepExecution(signalHandler)) {
					vertice.scriptGateway.runVerticeExecutionScript(vertice.graphUUID!!, data, vertice.execParams)
				}
			}
		}

		private fun wrappedScript(vertice: SubGraphVerticeRef): Script {
			return Script(
				code = vertice.getGraphIfPresent()!!.script!!,
				origin = vertice.type,
				context = Translations.getString("graph.property.GraphViewImpl.script.name"))
		}

		/** Creates a new [SubGraphVerticeRef] using the data in the specified [SubGraphVertice].*/
		fun fromSubGraphVertice(
			subGraphVertice: SubGraphVertice,
			repository: MetaGraphRepository,
			scriptGateway: ScriptGateway
		): SubGraphVerticeRef {
			val verticeRef = SubGraphVerticeRef(null, repository, scriptGateway)
			verticeRef.fillFrom(subGraphVertice)
			return verticeRef
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

	private var graph: Graph? = null

	/** Can be set during [read] if reference to [MetaGraph] is broken. */
	private var _designError: DesignError? = null

	private val hasDesignError: Boolean get() = designError != null

	private lateinit var execParams: Any

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
		LOG.trace("SubGraphVerticeRef: propagateOutput for Output '${outputPort.name}'")
		// Invoke SignalHandler in order to enable breakpoint on the SubGraphOutputPort
		signalHandler.requestActingAfter(this, 1, VerticeActorData(outputPort, isInput = false))
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
			LOG.warn("SubGraphVerticeRef: broken reference $graphUUID")
			graphName = Name(BrokenReferenceView.NAME)
			super.read(reader)
			_designError = DesignError("Broken reference")

		}
	}

	/** ---- [Actor] */

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		if (!hasDesignError) {
			if (isDeepExecution(signalHandler)) {
				graph?.executionStarted(signalHandler)
			} else {
				// This will define the same script for all SubGraphVerticeRef instances of the same Graph
				// which is unnecessary, but so be it for the moment
				execParams = scriptGateway.defineVerticeExecutionScript(graphUUID!!, wrappedScript(this), this, signalHandler)

				requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, null, true))
			}
		}
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		graph?.executionStopped(signalHandler)
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
				graphOutput.setSubGraphOutputPort(output)
			} else {
				LOG.error("SubGraphVerticeRef: Cannot find GraphOutput '${output.name}' in '${graph!!.name}' (${graph!!.uuid})")
				// TODO Throw exception for global error display
			}
		}

		return graph!!
	}

	/** ---- [SubGraphVerticeRef] */

	private fun fillFrom(subGraphVertice: SubGraphVertice) {
		graphUUID = subGraphVertice.graphUUID

		type = subGraphVertice.graphName.value
		_typeDesc = subGraphVertice.description.value

		for (port in subGraphVertice.getPorts()) {
			addPort(port, port.portId)
		}
	}

	private fun isDeepExecution(signalHandler: SignalHandler): Boolean {
		return signalHandler.isDeepExecution || graph!!.script == null || graph!!.script == ""
	}

	private fun getSubGraphInputPorts(): ImmutableList<SubGraphInputPort<Any>> {
		return getInputs().map { it as SubGraphInputPort<Any> }.toImmutableList()
	}

	private fun getSubGraphOutputPorts(): ImmutableList<SubGraphOutputPort<Any>> {
		return getOutputs().map { it as SubGraphOutputPort<Any> }.toImmutableList()
	}
}