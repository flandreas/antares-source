package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.DescriptionChangedEvent
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.model.text.description.NameChangedEvent
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.model.GraphPortNameChanged
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
		return LibraryModule.libraryHolder.library.getOptionalMetaGraph(uuid)
			?: ProjectModule.projectHolder.project!!.getMetaGraph(uuid)
	}

	/** Returns the entire [MetaGraph] with the specified [UUID] if it exists, or `null` otherwise. */
	override fun getOptionalMetaGraph(uuid: UUID): MetaGraph? {
		return LibraryModule.libraryHolder.library.getOptionalMetaGraph(uuid)
			?: ProjectModule.projectHolder.project!!.getOptionalMetaGraph(uuid)
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
	containerDrawing: ContainerDrawing,
	private val eventBus: EventBus = BaseModule.eventBus
) : Storable {

	constructor() : this(
		GraphStorable(Translations.getString("graph.name.unknown")),
		ContainerDrawing(Translations.getString("graph.name.unknown"))
	)

	companion object {
		fun withName(name: String): MetaGraph {
			val metaGraph = MetaGraph()
			metaGraph.graph.model!!.name.value = name
			metaGraph.containerDrawing.model.name = name
			return metaGraph
		}
	}

	var graph: GraphStorable = graph
		private set(value) {
			if (field !== value) {
				field.dispose()
				field = value
			}
		}

	var containerDrawing: ContainerDrawing = containerDrawing
		private set(value) {
			if (field !== value) {
				field.dispose()
				field = value
			}
		}

	val name: String get() = graph.model!!.name.value

	val uuid: UUID get() = graph.model!!.uuid

	val translatableName: TranslatableText get() = graph.model!!.name.translation

	private val graphNameHandler: EventHandler<NameChangedEvent> = {
		handle(it)
	}

	private val graphDescHandler: EventHandler<DescriptionChangedEvent> = {
		handle(it)
	}

	private val graphPortNameHandler: EventHandler<GraphPortNameChanged<*>> = {
		handle(it)
	}

	init {
		eventBus.register(NameChangedEvent::class, graphNameHandler)
		eventBus.register(DescriptionChangedEvent::class, graphDescHandler)
		eventBus.register(GraphPortNameChanged::class, graphPortNameHandler)
		containerDrawing.model.graphUUID = uuid
		containerDrawing.initialize()
	}

	fun dispose() {
		eventBus.unregister(NameChangedEvent::class, graphNameHandler)
		eventBus.unregister(DescriptionChangedEvent::class, graphDescHandler)
		eventBus.unregister(GraphPortNameChanged::class, graphPortNameHandler)
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

	override fun resolutionDone() {
		super.resolutionDone()
		copyGraphDataFromContainerModel(graph.model!!)
	}

	/** ---- [MetaGraph] */

	fun duplicate(newName: TranslatableText): MetaGraph {
		val duplicate = StorableCloner.clone(this)

		duplicate.containerDrawing.model.graphUUID = ch.scorpion.jabbah.base.System.createUUID()
		duplicate.containerDrawing.model.graphName = Name(newName)
		copyGraphDataFromContainerModel(duplicate.graph.model!!)

		return duplicate
	}

	fun cloneGraphModel(storableCreator: StorableCreator): Graph {
		val clone = StorableCloner.clonePreservingIdentities(graph.model!!, storableCreator)
		copyGraphDataFromContainerModel(clone)
		return clone
	}

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

	private fun handle(event: NameChangedEvent) {
		if (event.name === graph.model?.name) {
			containerDrawing.model.graphName = Name(event.name.translation)
		}
	}

	private fun handle(event: DescriptionChangedEvent) {
		if (event.description == graph.model?.description) {
			containerDrawing.model.description.translation = event.description.translation
		}
	}

	private fun handle(event: GraphPortNameChanged<*>) {
		if (graph.graphView.graph!!.contains(event.graphPort)) {
			containerDrawing.getPortViewComponent(event.oldName!!)?.let { it.portView!!.setPortName(event.newName!!) }
		}
	}

	private fun copyGraphDataFromContainerModel(graph: Graph) {
		graph.uuid = containerDrawing.model.graphUUID!!
		graph.name.translation = containerDrawing.model.graphName.translation
		graph.description.translation = containerDrawing.model.description.translation

	}
}