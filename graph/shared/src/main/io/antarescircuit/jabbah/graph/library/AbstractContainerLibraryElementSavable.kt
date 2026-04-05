package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.graph.MetaGraph

/**
 * Saves the edited [MetaGraph] of a [ContainerLibraryElement] in the containing [LibraryDirectory].
 */
abstract class AbstractContainerLibraryElementSavable(
	element: ContainerLibraryElement,
) : AbstractLibraryItemSavable(element) {

	override val typeName: String
		get() = Translations.getString("graph.savable.name")

	val element: ContainerLibraryElement get() = item as ContainerLibraryElement

	/** ---- [Savable] */

	override val supportsMostRecent: Boolean get() = true
}