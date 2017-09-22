package ch.scorpion.jabbah.graph.model.vertice

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.Actor
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.logger

/**
 * A [SubGraphVertice] implementation that is part of one [Graph] and references another [Graph] in the [Library].
 */
class SubGraphVerticeRef(
    val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
    val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
    val scriptGateway: ScriptGateway = ScriptModule.scriptGateway
) : CalculatingVertice(CALCULATOR), SubGraphVertice {

    companion object {

        val CALCULATOR = object : VerticeCalculator<SubGraphVerticeRef> {
            override fun calculate(vertice: SubGraphVerticeRef, data: GraphActorData, signalHandler: SignalHandler) {
                vertice.scriptGateway.exec(vertice.getGraphIfPresent()!!.script!!, vertice, data, signalHandler)
            }
        }

        val LOG by logger(SubGraphVerticeRef::class)

        /** Creates a new [SubGraphVerticeRef] using the data in the specified [SubGraphVertice].*/
        fun fromSubGraphVertice(
            subGraphVertice: SubGraphVertice,
            storableCloner: StorableCloner,
            libraryHolder: LibraryHolder,
            scriptGateway: ScriptGateway
        ): SubGraphVerticeRef {
            val verticeRef = SubGraphVerticeRef(storableCloner, libraryHolder, scriptGateway)
            verticeRef.graphUUID = subGraphVertice.graphUUID
            verticeRef.name = subGraphVertice.name
            verticeRef.fillFrom(subGraphVertice)
            return verticeRef
        }
    }

    private var graph: Graph? = null

    /** ---- [GraphElement] interface */

    override fun accept(visitor: HierarchyVisitor): Boolean {
        if (visitor.visitEnter(this)) {
            graph?.accept(visitor)
        }
        return visitor.visitLeave(this)
    }

    /** ---- [SubGraphVertice] */

    override var graphUUID: UUID? = null

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("uuid", graphUUID!!.id)
    }

    override fun read(reader: StoreReader) {
        graphUUID = UUID(reader.readString("uuid"))

        val metaGraph = libraryHolder.library.getMetaGraph(graphUUID!!)
        val containerDrawing = metaGraph.containerDrawing
        name = metaGraph.name
        shortDescription = metaGraph.graph!!.model!!.shortDescription
        fillFrom(containerDrawing!!.createSubGraphVertice())

        super.read(reader)

        if (metaGraph.graph!!.model!!.propagationDelay != null) {
            propagationDelay = metaGraph.graph!!.model!!.propagationDelay!!
        }
    }

    /** ---- [Actor] */

    override fun executionStarted(signalHandler: SignalHandler) {
        super.executionStarted(signalHandler)
        if (isDeepExecution(signalHandler)) {
            graph?.executionStarted(signalHandler)
        } else {
            requestActingAfter(signalHandler, propagationDelay, GraphActorDataImpl(null, null))
        }
    }

    override fun executionStopped(signalHandler: SignalHandler) {
        super.executionStopped(signalHandler)
        graph?.executionStopped(signalHandler)
    }

    /** ---- [AbstractVertice] */

    override var shortDescription: String?
        get() = graph?.shortDescription ?: super.shortDescription
        set(value) {super.shortDescription = value}

    override fun inputChanged(input: InputPort<*>, signalHandler: SignalHandler) {
        if (isDeepExecution(signalHandler)) {
            val graphInput = (input as SubGraphInputPort<Any>).graphInput
            graphInput!!.setIncomingSignal(input.getIncomingSignal(), signalHandler)
        } else {
            // This will eventually call the VerticeCalculator which executes the script
            super.inputChanged(input, signalHandler)
        }
    }

    /** ---- [SubGraphVertice] */

    override fun getGraphIfPresent(): Graph? {
        return graph
    }

    override fun bind(library: Library, storableCreator: StorableCreator) {
        super.bind(library, storableCreator)
        graph = getGraph(library, storableCreator)
        graph!!.bind(library, storableCreator)
    }

    override fun getGraph(library: Library, storableCreator: StorableCreator): Graph {
        if (graph != null) {
            return graph!!
        }
        val subGraph = library.getMetaGraph(graphUUID!!)
        val cloneGraph = storableCloner.clonePreservingIdentities(subGraph.graph!!.model!!, storableCreator)
        graph = cloneGraph as Graph

        for (input in getSubGraphInputPorts()) {
            input.graphInput = graph!!.getGraphInput<Any>(input.name!!)
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