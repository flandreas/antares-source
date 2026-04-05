package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.dsl.Node
import io.antarescircuit.jabbah.base.dsl.ScriptMetaData
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Represents a request to open the [MetaGraph] of a [ContainerLibraryElement].
 * It is up to higher level application classes to decide how the
 * [MetaGraph] of the selected [ContainerLibraryElement] is to be presented to the user.
 *
 * @property element the [ContainerLibraryElement] whose [MetaGraph] is to be opened
 * @property focusVerticeViewId the ID of the [VerticeView] to focus on (bring to the user's attention)
 * after having opened [element]
 */
data class OpenContainerLibraryElementRequest(
	val element: ContainerLibraryElement,
	val focusVerticeViewId: Int? = null
)

/**
 * A [LibraryElement] that contains a [MetaGraph].
 *
 * @property uuid the UUID of the reference [MetaGraph]
 */
class ContainerLibraryElement(
	var uuid: UUID = UUID("undefined"),
	graphType: GraphType = GraphModelModule.defaultGraphType,
	initialName: TranslatableText = TranslatableText(),
	iconPath: String? = null,
	val eventBus: EventBus = BaseModule.eventBus
) : LibraryElement(initialName, iconPath), UndoableStateLibraryItem<MetaGraph> {

	companion object {
		val LOG by logger(ContainerLibraryElement::class)
	}

	/** Uses as long as [storable] has not yet been instantiated, e.g. when displaying a [Library] in the UI.*/
	override var graphType: GraphType = graphType
		private set

	/** Lazily initialized instance of the referenced [MetaGraph]. */
	override var storable: MetaGraph? = null
		private set(value) {
			if (field != value) {
				val oldValue = field
				field = value
				executionScriptASTCache.reset()
				drawSymbolScriptASTCache.reset()
				oldValue?.dispose()
			}
		}

	private val executionScriptASTCache = resettableLazy {
		storable?.graph?.model?.script?.let {
			LOG.trace("Parsing script of '${storable!!.name}'")
			storable!!.graph.model!!
				.createParser(it, null)
				.parseCatching(ScriptMetaData(storable!!.name, Translations.getString("graph.property.GraphViewImpl.script.name")))
		}
	}

	/** Returns the abstract syntax tree of the script for executing this [ContainerLibraryElement]'s [Graph].*/
	val executionScriptAST: Node? get() = executionScriptASTCache.value

	private val drawSymbolScriptASTCache = resettableLazy {
		storable?.containerDrawing?.execDrawScript?.script?.let {
			LOG.trace("Parsing symbol drawing script of '${storable!!.name}'")
			storable!!.containerDrawing.
				createDrawSymbolScriptParser(it, null)
				.parseCatching(ScriptMetaData(storable!!.name, Translations.getString("graph.property.ContainerDrawing.execDrawScript.name")))
		}
	}

	/** Returns the abstract syntax tree of the script for enhancing drawing this [ContainerLibraryElement]'s symbol. */
	val drawSymbolAST: Node? get() = drawSymbolScriptASTCache.value

	/** ---- [LibraryItem] */

	override val isFixed: Boolean get() = false

	override fun open(eventBus: EventBus) {
		eventBus.post(OpenContainerLibraryElementRequest(this))
	}

	override fun dispose() {
		super.dispose()
		storable?.dispose()
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		writer.writeString("uuid", uuid.toString())
		name.write("name", writer)
		writer.writeString("type", graphType.customName)
	}

	override fun read(reader: StoreReader) {
		uuid = UUID(reader.readString("uuid"))
		if (reader.hasAttribute("name")) {
			// backward compatibility
			name = Name(reader.readString("name"))
		}
		if (reader.hasElement("name")) {
			name = Name.read("name", reader)
		}
		graphType = if (reader.hasAttribute("type")) {
			GraphModelModule.graphTypeRegistry.withCustomName(reader.readString("type"))
		} else {
			// Backward compatibility
			GraphModelModule.defaultGraphType
		}
	}

	/** ---- [LibraryElement] */

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			if (storable != null) {
				storable!!.accept(visitor)
			}
		}
		return visitor.visitLeave(this)
	}

	override fun <T : GraphElement> getNewInstance(): GraphElementView<T> {
		library!!.libraryService.getMetaGraph(library!!, this)

		LOG.trace("Create new GraphElementView of '$name' MetaGraph with ID ${storable!!.hashCode()} in Library with ID ${library.hashCode()}")
		val instance = storable!!.containerDrawing.createSubGraphVerticeView(graphType)
		if (storable!!.graph.model!!.overallPropagationDelay != null) {
			instance.model.propagationDelay = LongValueImpl(storable!!.graph.model!!.overallPropagationDelay!!)
		} else if (storable!!.graph.model!!.calculatedPropagationDelay != null) {
			instance.model.propagationDelay = LongValueImpl(storable!!.graph.model!!.calculatedPropagationDelay!!)
		}
		@Suppress("UNCHECKED_CAST")
		return instance as GraphElementView<T>
	}

	/** ---- [UndoableStateLibraryItem] */

	override fun updateStorable(storable: MetaGraph) {
		uuid = storable.uuid
		name = Name(storable.translatableName)
		this.storable = storable
	}

	override fun createSavable(): Savable = library!!.createSavable(this)
}