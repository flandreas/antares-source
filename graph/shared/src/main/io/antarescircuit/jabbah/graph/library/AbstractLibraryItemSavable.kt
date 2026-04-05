package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.auth.Authorizer
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.project.Project

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