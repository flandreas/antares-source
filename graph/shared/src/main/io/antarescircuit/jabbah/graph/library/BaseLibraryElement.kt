package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.help.HelpIdProvider
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.edit.model.text.description.Namable
import io.antarescircuit.jabbah.graph.model.GenericGraphType
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

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
	graphType: GraphType = GenericGraphType,
	id: String = "",
	private val repository: BaseLibraryElementRepository = LibraryModule.baseLibraryElementRepository
) : LibraryElement(), HelpIdProvider {

	var id: String = id
		private set

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
	    writer.writeString("type", graphType.customName)
    }

    override fun read(reader: StoreReader) {
        id = reader.readString("id")
	    graphType = if (reader.hasAttribute("type")) {
			GraphModelModule.graphTypeRegistry.withCustomName(reader.readString("type"))
	    } else {
			// Backward compatibility
			GraphModelModule.defaultGraphType
	    }
    }

    /** ---- [LibraryElement] */

	override var graphType: GraphType = graphType
		private set

    override fun <T : GraphElement> getNewInstance(): GraphElementView<T> {
	    return repository.getNewInstance(id)
    }
}