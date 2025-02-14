package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.project.Project

abstract class AbstractLibraryItemSavable(
	val item: LibraryItem
) : Savable {

	override val defined: Boolean get() = true

	override val supportsMostRecent: Boolean get() = false

	override val description: String get() = if (item.library is Project) {
		"${Translations.getString("project.savable.prefix")} \"${item.name.getTranslation()}\""
	} else {
		"${Translations.getString("library.savable.prefix")} \"${item.name.getTranslation()}\""
	}

	override val editable: Boolean
		get() = item.library?.let { Authorizer.isCurrentUserAuthorizedTo(Operation.Change, it) } ?: false
}