package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Savable

abstract class AbstractLibraryItemSavable(
	val item: LibraryItem
) : Savable {

	override val defined: Boolean get() = true

	override val supportsMostRecent: Boolean get() = false
}