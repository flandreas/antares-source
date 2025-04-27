package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.app.CurrentApplicationVersion
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.dsl.SyntaxError
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.richtext.RichTextParser
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.model.Document
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.io.*

/**
 * Combines a [GraphStorable] (graph's model and view definitions) and a [ContainerDrawing] (the symbol) as a [Storable].
 * The third part is the optional [Document] containing the textual documentation of the [MetaGraph].
 */
class MetaGraph(
	graph: GraphStorable = GraphStorable(TranslatableText(Translations.getString("graph.name.unknown"))),
	containerDrawing: ContainerDrawing = ContainerDrawing(Translations.getString("graph.name.unknown")),
	documentation: Document? = null
) : AbstractStorable(), Disposable {

	companion object {
		private val LOG by logger(MetaGraph::class)

		fun create(name: TranslatableText, type: GraphType): MetaGraph {
			val graph = GraphModelModule.graphFactory.create(name, type)
			val graphView = GraphViewModule.graphViewFactory.create(graph)
			val metaGraph = MetaGraph(GraphStorable(graphView))
			metaGraph.graph.model!!.name = Name(name)
			metaGraph.containerDrawing.model.name = name.getTranslation()
			return metaGraph
		}

		/**
		 * [MetaGraph] names contain rich-text. This method validates all translations in [name] for
		 * correct rich-text syntax.
		 * @throws [IllegalArgumentException] with a translated message saying why the name is invalid
		 */
		fun validateName(name: TranslatableText) {
			if (name.isEmpty) {
				throw IllegalArgumentException(Translations.getString("library.action.newGraph.emptyName.msg"))
			}
			for (t in name.allTranslations()) {
				validateName(t.text)
			}
		}

		fun validateName(name: String) {
			if (name.isEmpty()) {
				throw IllegalArgumentException(Translations.getString("library.action.newGraph.emptyName.msg"))
			}
			try {
				RichTextParser(name).parse()
			} catch (e: SyntaxError) {
				throw IllegalArgumentException(e.message)
			}
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
				field.model?.removePropertyChangeListener(graphListener)
				field.dispose()
				field = value
				field.model?.addPropertyChangeListener(graphListener)
			}
		}

	var containerDrawing: ContainerDrawing = containerDrawing
		private set(value) {
			if (field !== value) {
				field.dispose()
				field = value
			}
		}

	var parameterDefinitions: GraphParamDefinitions = GraphParamDefinitions()

	/**
	 * Determines whether the [ContainerDrawing] of this [MetaGraph] has been manually edited by the user.
	 * Set to `false` if the [ContainerDrawing] is automatically created by the system.
	 */
	var isManualContainer: Boolean = false

	val name: String get() = graph.model!!.name.value

	val uuid: UUID get() = graph.model!!.uuid

	val type: GraphType get() = graph.model!!.type

	val translatableName: TranslatableText get() = graph.model!!.name.translation

	var documentation: Document? = documentation

	private val graphListener = GraphPropertyListener()

	init {
		LOG.trace("Instantiated new MetaGraph with ID ${hashCode()}")
		graph.model?.addPropertyChangeListener(graphListener)
		containerDrawing.model.graphUUID = uuid
		containerDrawing.initialize()
	}

	/** ---- [Disposable] interface */

	override fun dispose() {
		graph.dispose()
		containerDrawing.dispose()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		writer.writeStorable("graph", graph)
		writer.writeStorable("container", containerDrawing)

		// Graph might have changedGraphParamDefinitions
		parameterDefinitions = graph.model!!.parameterDefinitions

		CurrentApplicationVersion.write(writer)
		if (parameterDefinitions.isNotEmpty) {
			writer.writeStorable("params", parameterDefinitions)
		}
		writer.writeBoolean("manualContainer", isManualContainer)
		documentation?.let { writer.writeStorable("documentation", it) }
	}

	override fun read(reader: StoreReader) {
		CurrentApplicationVersion.check(reader)

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

		if (reader.hasElement("params")) {
			parameterDefinitions = reader.readStorable("params")
		}
		isManualContainer = if (reader.hasAttribute("manualContainer")) {
			reader.readBoolean("manualContainer")
		} else {
			// The default value is `true` for backward compatibility reasons: Before this property was introduced,
			// the user edited all symbols manually
			true
		}
		if (reader.hasElement("documentation")) {
			documentation = reader.readStorable("documentation") as Document
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		if (reference.name == "graph") {
			graph = reference.additionalInfo as GraphStorable
			graph.model!!.parameterDefinitions = parameterDefinitions
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

		duplicate.containerDrawing.model.graphUUID = System.createUUID()
		duplicate.containerDrawing.model.graphName = Name(newName)

		copyGraphDataFromContainerModel(duplicate.graph.model!!, duplicate.containerDrawing)

		return duplicate
	}

	fun cloneGraphGraphStorable(): GraphStorable =
		StorableCloner
			.clone(graph)
			.also {
				it.model!!.parameterDefinitions = parameterDefinitions
				copyGraphDataFromContainerModel(it.graphView.graph!!, containerDrawing)
			}

	fun cloneGraphModel(): Graph {
		val clone = StorableCloner.clonePreservingIdentities(graph.model!!)
		clone.parameterDefinitions = parameterDefinitions
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

	private fun copyGraphDataFromContainerModel(graph: Graph) {
		copyGraphDataFromContainerModel(graph, containerDrawing)
	}

	private inner class GraphPropertyListener : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			when (e.name) {
				Graph.PROP_NAME -> {
					containerDrawing.model.graphName = Name((e.newValue as Name).translation)
				}
				Graph.PROP_DESCRIPTION -> {
					containerDrawing.model.description = e.newValue as Description
				}
			}
		}
	}
}