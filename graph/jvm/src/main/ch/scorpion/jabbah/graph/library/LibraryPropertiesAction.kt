package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

class LibraryPropertiesAction(
	controller: LibraryTreeViewController,
	private val managementService: LibraryManagementService = LibraryModule.libraryManagementService
) : AbstractLibraryPropertiesAction(
	baseName = "library.action.properties",
	controller
) {
	override val dialogTitle: String get() = Translations.getString("library.dialog.properties.title", currentProperties.name.getTranslation())

	override fun exists(newName: TranslatableText): Boolean =
		managementService.existsName(newName, except = library.uuid)

	override fun update(properties: LibraryProperties) {
		managementService.update(library, properties)
	}
}