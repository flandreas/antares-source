package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.MetaGraph

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