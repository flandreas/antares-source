package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.repository.SubGraphVerticeLocator

/**
 * A [MetaGraphRepository] is a repository of reusable [MetaGraph].
 *
 * A [MetaGraph] containing [SubGraphVertice]s will use a [MetaGraphRepository] to get access to the
 * [MetaGraph] that is referenced by the [SubGraphVertice].
 */
interface MetaGraphRepository {

	/** Returns the entire [MetaGraph] with the specified [UUID], including the view representations. */
	fun getMetaGraph(uuid: UUID): MetaGraph

	/** Returns the entire [MetaGraph] with the specified [UUID] if it exists, or `null` otherwise. */
	fun getOptionalMetaGraph(uuid: UUID): MetaGraph?

	/** Checks whether a [MetaGraph] with [uuid] exists in this [MetaGraphRepository]. */
	fun containsMetaGraph(uuid: UUID): Boolean

	/**
	 * Determines whether a [Graph] contains directly or recursively a [GraphElement]
	 * with the specified UUID.
	 */
	fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID): Boolean

}

/** Combines the current [Library] and the current [Project] (if any) to a single [MetaGraphRepository].*/
class CombinedMetaGraphRepository(
	private val storableCreator: StorableCreator = IOModule.storableCreator
) : MetaGraphRepository {

	/** Returns the entire [MetaGraph] with the specified [UUID], including the view representations. */
	override fun getMetaGraph(uuid: UUID): MetaGraph {
		return LibraryModule.libraryHolder.library.getOptionalMetaGraph(uuid) ?: ProjectModule.projectHolder.project!!.getMetaGraph(uuid)
	}

	/** Returns the entire [MetaGraph] with the specified [UUID] if it exists, or `null` otherwise. */
	override fun getOptionalMetaGraph(uuid: UUID): MetaGraph? {
		return LibraryModule.libraryHolder.library.getOptionalMetaGraph(uuid) ?: ProjectModule.projectHolder.project!!.getOptionalMetaGraph(uuid)
	}

	/** Checks whether a [MetaGraph] with [uuid] exists in this [MetaGraphRepository]. */
	override fun containsMetaGraph(uuid: UUID): Boolean {
		return LibraryModule.libraryHolder.library.containsMetaGraph(uuid) || ProjectModule.projectHolder.project!!.containsMetaGraph(uuid)
	}

	override fun graphContainsRecursively(graphUUID: UUID, graphElementUUID: UUID): Boolean {
		val metaGraph = getMetaGraph(graphUUID)
		if (metaGraph.graph.model!!.uuid == graphElementUUID) {
			return true
		}
		return SubGraphVerticeLocator(
			graph = metaGraph.graph.model!!,
			repository = this,
			storableCreator = storableCreator
		).contains(graphElementUUID)
	}
}

/**
 * Combines a [GraphStorable] and a [ContainerDrawing] as a [Storable].
 */
class MetaGraph(
	graph: GraphStorable,
	containerDrawing: ContainerDrawing
) : Storable {

	constructor(): this(GraphStorable(Translations.getString("graph.name.unknown")), ContainerDrawing(Translations.getString("graph.name.unknown")))

	companion object {
		fun withName(name: String): MetaGraph {
			val metaGraph = MetaGraph()
			metaGraph.graph.model!!.name = name
			metaGraph.containerDrawing.model.name = name
			return metaGraph
		}
	}

    var graph: GraphStorable = graph
        private set

    var containerDrawing: ContainerDrawing = containerDrawing
        private set

    val name: String get() = graph.model!!.name

    val uuid: UUID get() = graph.model!!.uuid

    init {
        containerDrawing.model.graphUUID = uuid
        containerDrawing.initialize()
    }

	fun dispose() {
		graph.dispose()
		containerDrawing.dispose()
	}

    /** ---- [Storable] interface */

    override var storableId: Int = 0

    override fun write(writer: StoreWriter) {
        writer.writeStorable("graph", graph)
        writer.writeStorable("container", containerDrawing)
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
                resolveAfter = listOf(aContainerDrawing.storableId, aGraph.storableId)))
    }

    override fun getStorableChildren(): Iterator<Storable> {
        return listOf(graph, containerDrawing).iterator()
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        if (reference.name == "graph") {
            graph = reference.additionalInfo as GraphStorable
        }
        if (reference.name == "container") {
            containerDrawing = reference.additionalInfo as ContainerDrawing
            containerDrawing.completeFromGraph(graph.model!!)
        }
    }

    /** ---- [MetaGraph] */

    fun accept(visitor: HierarchyVisitor): Boolean {
        if (visitor.visitEnter(this)) {
            if (!graph.accept(visitor)) {
                return visitor.visitLeave(this)
            }
            if (!containerDrawing.accept(visitor)) {
                return visitor.visitLeave(this)
            }
        }
        return visitor.visitLeave(this)
    }
}