package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.view.vertice.BrokenReferenceView

/**
 * A [SubGraphVertice] implementation that is part of one [Graph] and references another [Graph] in the [Library].
 */
class SubGraphVerticeRef(
    graphUUID: UUID? = null,
    private val repository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
    private val scriptGateway: ScriptGateway = ScriptModule.scriptGateway
) : CalculatingVertice("library.element.SubGraphVerticeRef", CALCULATOR), SubGraphVertice {

    companion object {

        val CALCULATOR = object : VerticeCalculator<SubGraphVerticeRef> {
            override fun calculate(vertice: SubGraphVerticeRef, data: GraphActorData, signalHandler: SignalHandler) {
                if (data.isInput && !vertice.isDeepExecution(signalHandler)) {
                    vertice.scriptGateway.exec(Script(code = vertice.getGraphIfPresent()!!.script!!, origin = "SubGraph '${vertice.name}'", context = "Logic"), vertice, data, signalHandler)
                }
            }
        }

        val LOG by logger(SubGraphVerticeRef::class)

        /** Creates a new [SubGraphVerticeRef] using the data in the specified [SubGraphVertice].*/
        fun fromSubGraphVertice(
            subGraphVertice: SubGraphVertice,
            repository: MetaGraphRepository,
            scriptGateway: ScriptGateway
        ): SubGraphVerticeRef {
            val verticeRef = SubGraphVerticeRef(null, repository, scriptGateway)
            verticeRef.graphUUID = subGraphVertice.graphUUID
	        verticeRef.translatableName = subGraphVertice.translatableName
	        verticeRef.translatableDescription = subGraphVertice.translatableDescription
            verticeRef.fillFrom(subGraphVertice)
            return verticeRef
        }
    }

    private var graph: Graph? = null

	/** Can be set during [read] if reference to [MetaGraph] is broken. */
	private var _designError: DesignError? = null

    private val hasDesignError: Boolean get() = designError != null

	override val designError: DesignError? get() = _designError

	/**
	 * Represents a short description that is valid for this very instance of [SubGraphVerticeRef].
	 * Generally, the description of a [SubGraphVerticeRef] is the short description of the referenced [Graph].
	 * The property [descriptionProperty] is used to overrride [shortDescription] with a more specific value.
	 */
	var descriptionProperty: TextProperty = TextProperty()

    /** ---- [GraphElement] interface */

    override fun accept(visitor: HierarchyVisitor): Boolean {
        if (visitor.visitEnter(this)) {
            graph?.accept(visitor)
        }
        return visitor.visitLeave(this)
    }

	/** ---- [AbstractVertice] */

	/** Represents the [translatableName] in the current system [Language].*/
	override var name: String?
		get() = translatableName.getTranslation()
		set(value) {
			if (StringUtils.isNotEmpty(value)) {
				translatableName = translatableName.withTranslation(value!!)
			}
		}

    /** ---- [SubGraphVertice] */

    override var graphUUID: UUID? = graphUUID

	override var translatableName: TranslatableText = TranslatableText()

	override var translatableDescription: TranslatableText = TranslatableText()

    override fun <T: Any> propagateOutput(outputPort: SubGraphOutputPort<T>, signal: T, signalHandler: SignalHandler) {
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
	    descriptionProperty.text?.let { writer.writeString("desc", descriptionProperty.text!!) }
    }

    override fun read(reader: StoreReader) {
        graphUUID = UUID(reader.readString("uuid"))
	    descriptionProperty = TextProperty(reader.readOptionalString("desc"))

        val metaGraph = repository.getOptionalMetaGraph(graphUUID!!)
        if (metaGraph != null) {
            name = metaGraph.name
	        translatableName = metaGraph.containerDrawing.model.translatableName
	        translatableDescription = metaGraph.containerDrawing.model.translatableDescription
            fillFrom(metaGraph.containerDrawing.createSubGraphVertice())

            super.read(reader)

            if (metaGraph.graph.model!!.propagationDelay != null) {
                propagationDelay = metaGraph.graph.model!!.propagationDelay!!
            }
        } else {
	        // Broken reference to library component
	        LOG.warn("SubGraphVerticeRef: broken reference $graphUUID")
	        translatableName = BrokenReferenceView.NAME
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
			    requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, null, true))
		    }
	    }
    }

    override fun executionStopped(signalHandler: SignalHandler) {
        super.executionStopped(signalHandler)
        graph?.executionStopped(signalHandler)
    }

    /** ---- [AbstractVertice] */

    override var shortDescription: String?
	    get() {
		    if (descriptionProperty.isNotEmpty()) {
			    return descriptionProperty.text
		    } else {
			    return translatableDescription.getOptionalTranslation() ?: super.shortDescription
		    }
	    }
        set(value) {super.shortDescription = value}

    override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
        if (isDeepExecution(signalHandler)) {
            val graphInput = (input as SubGraphInputPort<Any>).graphInput
            graphInput!!.setIncomingSignal(input.getIncomingSignal(), signalHandler)
        }
        // This will eventually call the VerticeCalculator which will execute the script (if not deeply executing).
        // Event if deeply executing, we need to request acting, because only that will initiate
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
		    graph = getGraph(repository, storableCreator)
		    graph!!.bind(repository, storableCreator)
	    }
    }

    override fun getGraph(repository: MetaGraphRepository, storableCreator: StorableCreator): Graph {
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

    private fun fillFrom(model: SubGraphVertice) {
        for (port in model.getPorts()) {
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
        return getOutputs().map { it as SubGraphOutputPort<Any>}.toImmutableList()
    }
}