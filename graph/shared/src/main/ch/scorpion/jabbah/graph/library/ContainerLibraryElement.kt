package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.GraphElement
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
	initialName: TranslatableText = TranslatableText(),
	iconPath: String? = null,
	val eventBus: EventBus = BaseModule.eventBus
) : LibraryElement(initialName, iconPath) {

	companion object {
		val LOG by logger(ContainerLibraryElement::class)
	}

	/** Lazily initialized instance of the referenced [MetaGraph]. */
	var metaGraph: MetaGraph? = null
		private set(value) {
			if (field != value) {
				field?.dispose()
				field = value
			}
		}

	/** ---- [LibraryItem] */

	override val isFixed: Boolean get() = false

	override fun dispose() {
		super.dispose()
		metaGraph?.dispose()
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		writer.writeString("uuid", uuid.toString())
		name.write("name", writer)
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
	}

	/** ---- [LibraryElement] */

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			if (metaGraph != null) {
				metaGraph!!.accept(visitor)
			}
		}
		return visitor.visitLeave(this)
	}

	override fun <T : GraphElement> getNewInstance(): GraphElementView<T> {
		library!!.libraryService.getMetaGraph(library!!, this)

		LOG.trace("Create new GraphElementView of '$name' MetaGraph with ID ${metaGraph!!.hashCode()} in Library with ID ${library.hashCode()}")
		val instance = metaGraph!!.containerDrawing.createSubGraphVerticeView()
		if (metaGraph!!.graph.model!!.propagationDelay != null) {
			instance.model.propagationDelay = metaGraph!!.graph.model!!.propagationDelay!!
		}
		@Suppress("UNCHECKED_CAST")
		return instance as GraphElementView<T>
	}

	/** ---- [ContainerLibraryElement] */

	fun updateMetaGraph(metaGraph: MetaGraph) {
		uuid = metaGraph.uuid
		name = Name(metaGraph.translatableName)
		this.metaGraph = metaGraph
	}
}