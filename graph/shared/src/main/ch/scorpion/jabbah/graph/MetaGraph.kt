package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.graph.view.container.ContainerDrawing
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.base.UUID


/**
 * Combines a [GraphStorable] and a [ContainerDrawing] as a [Storable].
 */
class MetaGraph(graph: GraphStorable? = null, containerDrawing: ContainerDrawing? = null) : Storable {
    constructor(): this(GraphStorable(), ContainerDrawing())

    var graph: GraphStorable? = graph
        private set

    var containerDrawing: ContainerDrawing? = containerDrawing
        private set

    val name: String get() = graph!!.model!!.name

    val uuid: UUID get() = graph!!.model!!.uuid

    init {
        if (containerDrawing != null) {
            containerDrawing.model.name = name
            containerDrawing.model.graphUUID = uuid
            containerDrawing.initialize()
        }
    }

    /** ---- [Storable] interface */

    override var storableId: Int = 0

    override fun write(writer: StoreWriter) {
        writer.writeStorable("graph", graph!!)
        writer.writeStorable("container", containerDrawing!!)
    }

    override fun read(reader: StoreReader) {
        val aGraph = reader.readStorable("graph") as GraphStorable
        reader.requestResolution(this, Reference(
                name = "graph",
                referenceId = aGraph.storableId,
                additionalInfo = aGraph,
                resolveAfter = listOf(aGraph.storableId)))

        val aContainerDrawing = reader.readStorable("container") as ContainerDrawing
        reader.requestResolution(this, Reference(
                name = "container",
                referenceId = aContainerDrawing.storableId,
                additionalInfo = aContainerDrawing,
                resolveAfter = listOf(aContainerDrawing.storableId)))
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return listOf(graph!!, containerDrawing!!).iterator()
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        if (reference.name == "graph") {
            graph = reference.additionalInfo as GraphStorable
        }
        if (reference.name == "container") {
            containerDrawing = reference.additionalInfo as ContainerDrawing
        }
    }

    /** ---- [MetaGraph] */

    fun accept(visitor: HierarchyVisitor): Boolean {
        if (visitor.visitEnter(this)) {
            if (!graph!!.accept(visitor)) {
                return visitor.visitLeave(this)
            }
            if (!containerDrawing!!.accept(visitor)) {
                return visitor.visitLeave(this)
            }
        }
        return visitor.visitLeave(this)
    }
}