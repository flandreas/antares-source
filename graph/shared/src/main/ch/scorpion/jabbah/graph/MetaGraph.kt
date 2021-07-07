package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.DescriptionChangedEvent
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.model.text.description.NameChangedEvent
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphPortNameChanged
import ch.scorpion.jabbah.graph.model.GraphPortTypeChanged
import ch.scorpion.jabbah.io.*

/**
 * Combines a [GraphStorable] and a [ContainerDrawing] as a [Storable].
 */
class MetaGraph(
	graph: GraphStorable = GraphStorable(Translations.getString("graph.name.unknown")),
	containerDrawing: ContainerDrawing = ContainerDrawing(Translations.getString("graph.name.unknown")),
	private val eventBus: EventBus = BaseModule.eventBus
) : Storable {

	companion object {
		private val LOG by logger(MetaGraph::class)

		fun withName(name: String): MetaGraph {
			val metaGraph = MetaGraph()
			metaGraph.graph.model!!.name = Name(name)
			metaGraph.containerDrawing.model.name = name
			return metaGraph
		}

		private fun copyGraphDataFromContainerModel(graph: Graph, containerDrawing: ContainerDrawing) {
			graph.uuid = containerDrawing.model.graphUUID!!
			graph.name = containerDrawing.model.graphName
			graph.description = containerDrawing.model.description
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

	private val graphPortTypeHandler: EventHandler<GraphPortTypeChanged<*>> = {
		handle(it)
	}

	init {
		LOG.trace("Instantiated new MetaGraph with ID ${hashCode()}")
		eventBus.register(NameChangedEvent::class, graphNameHandler)
		eventBus.register(DescriptionChangedEvent::class, graphDescHandler)
		eventBus.register(GraphPortNameChanged::class, graphPortNameHandler)
		eventBus.register(GraphPortTypeChanged::class, graphPortTypeHandler)
		containerDrawing.model.graphUUID = uuid
		containerDrawing.initialize()
	}

	fun dispose() {
		eventBus.unregister(NameChangedEvent::class, graphNameHandler)
		eventBus.unregister(DescriptionChangedEvent::class, graphDescHandler)
		eventBus.unregister(GraphPortNameChanged::class, graphPortNameHandler)
		eventBus.unregister(graphDescHandler)
		graph.dispose()
		containerDrawing.dispose()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		writer.writeStorable("graph", graph)
		writer.writeStorable("container", containerDrawing)
	}

	override fun read(reader: StoreReader) {
		val aGraph = reader.readStorable("graph") as GraphStorable
		val globalGraphId = reader.getGlobalId(aGraph)
		reader.requestResolution(this, Reference(
			name = "graph",
			referenceId = globalGraphId,
			additionalInfo = aGraph,
			resolveAfter = listOf(globalGraphId)))

		val aContainerDrawing = reader.readStorable("container") as ContainerDrawing
		val globalContainerId = reader.getGlobalId(aContainerDrawing)
		reader.requestResolution(this, Reference(
			name = "container",
			referenceId = globalContainerId,
			additionalInfo = aContainerDrawing,
			resolveAfter = listOf(globalContainerId, globalGraphId)))
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

		copyGraphDataFromContainerModel(duplicate.graph.model!!, duplicate.containerDrawing)

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
		if (event.owner === graph.model) {
			containerDrawing.model.graphName = Name(event.name.translation)
		}
	}

	private fun handle(event: DescriptionChangedEvent) {
		if (event.owner === graph.model) {
			containerDrawing.model.description = event.description
		}
	}

	private fun handle(event: GraphPortNameChanged<*>) {
		if (graph.graphView.graph!!.contains(event.graphPort)) {
			containerDrawing.getPortViewComponent(event.oldName!!)?.let {
				it.portView!!.setPortName(event.newName!!) }
		}
	}

	private fun handle(event: GraphPortTypeChanged<*>) {
		if (graph.graphView.graph!!.contains(event.graphPort)) {
			containerDrawing.getPortViewComponent(event.graphPort.name!!)?.let {
				it.portView!!.port.portType = event.newPortType
			}
		}
	}

	private fun copyGraphDataFromContainerModel(graph: Graph) {
		copyGraphDataFromContainerModel(graph, containerDrawing)
	}
}