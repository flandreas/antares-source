package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.dsl.ScriptMetaData
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Represents a request to open the [MetaGraph] of a [ContainerLibraryElement].
 * It is up to higher level application classes to decide how the
 * [MetaGraph] of the selected [ContainerLibraryElement] is to be presented to the user.
 *
 * @property element the [ContainerLibraryElement] whose [MetaGraph] is to be opened
 */
data class OpenContainerLibraryElementRequest(val element: ContainerLibraryElement)

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