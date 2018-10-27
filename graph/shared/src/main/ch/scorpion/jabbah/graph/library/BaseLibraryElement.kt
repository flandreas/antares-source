package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StorableCreator
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [LibraryElement] that represents a basic, non-composed [GraphElementView].
 * [LibraryElement] supports two different ways to decide which [GraphElementView] will be instantiated:
 * - Provide a [StorableCreator] to be used for instantiation, and the class of the [GraphElementView] to be instantiated
 * - Provide a supplier that knows how to instantiate the [GraphElementView]. Use this method if you need to
 * change the created [GraphElementView] after instantiation.
 * The supplier takes precedence over the [StorableCreator].
 */
class BaseLibraryElement(
	override var name: String = "",
	private val repository: BaseLibraryElementRepository = LibraryModule.baseLibraryElementRepository
) : LibraryElement() {

    override val isFixed: Boolean get() = true

	override val iconPath: String? get() = repository.getIconPath(name)

    /** ---- [Any] */

    override fun toString(): String {
	    return Translations.getString("${repository.getTranslationKey(name)!!}.name")
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        writer.writeString("name", name)
    }

    override fun read(reader: StoreReader) {
        name = reader.readString("name")
    }

    /** ---- [LibraryElement] */

    override fun <T : GraphElement> getNewInstance(): GraphElementView<T> {
	    return repository.getNewInstance(name)
    }
}