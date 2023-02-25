package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.help.HelpIdProvider
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A [LibraryElement] that represents a basic, non-composed [GraphElementView].
 *
 * [LibraryElement] supports two different ways to decide which [GraphElementView] will be instantiated:
 * - Provide the class of the [GraphElementView] to be instantiated
 * - Provide a supplier that knows how to instantiate the [GraphElementView]. Use this method if you need to
 * change the created [GraphElementView] after instantiation.
 *
 * @property id the ID of an entry in [BaseLibraryElementRepository] containing the referenced [GraphElementView].
 * @property repository the [BaseLibraryElementRepository] that contains the [GraphElementView] referenced by [name].
 */
class BaseLibraryElement(
	private var id: String = "",
	private val repository: BaseLibraryElementRepository = LibraryModule.baseLibraryElementRepository
) : LibraryElement(), HelpIdProvider {

	/** ---- [HelpIdProvider] */

	override val helpId: HelpId? get() = repository.getHelpId(id)

	/** ---- [Namable] interface */

	override var name: Name
		get() = Name(TranslatableText(Translations.getString("${repository.getTranslationKey(id)!!}.name")))
		set(@Suppress("UNUSED_PARAMETER") value) {
			throw UnsupportedOperationException()
		}

	/** ---- [LibraryItem] */

    override val isFixed: Boolean get() = true

	override val iconPath: String? get() = repository.getIconPath(id)

	/** ---- [Any] */

    override fun toString(): String = name.value

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        writer.writeString("id", id)
    }

    override fun read(reader: StoreReader) {
        id = reader.readString("id")
    }

    /** ---- [LibraryElement] */

    override fun <T : GraphElement> getNewInstance(): GraphElementView<T> {
	    return repository.getNewInstance(id)
    }
}