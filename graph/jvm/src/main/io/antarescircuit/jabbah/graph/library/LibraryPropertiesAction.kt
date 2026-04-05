package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

class LibraryPropertiesAction(
	controller: LibraryTreeViewController,
	private val managementService: LibraryManagementService = LibraryModule.libraryManagementService
) : AbstractLibraryPropertiesAction(
	baseName = "library.action.properties",
	controller
) {
	override val opensDialog: Boolean get() = true

	override val dialogTitle: String get() = Translations.getString("library.dialog.properties.title", currentProperties.name.getTranslation())

	override fun exists(newName: TranslatableText): Boolean =
		managementService.existsName(newName, except = library.uuid)

	override fun update(properties: LibraryProperties) {
		managementService.update(library, properties)
	}
}