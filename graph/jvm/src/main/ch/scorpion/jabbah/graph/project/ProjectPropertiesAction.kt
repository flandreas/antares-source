package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.AbstractLibraryPropertiesAction
import ch.scorpion.jabbah.graph.library.LibraryProperties
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

class ProjectPropertiesAction(
	controller: LibraryTreeViewController,
	private val managementService: ProjectManagementService = ProjectModule.projectManagementService
) : AbstractLibraryPropertiesAction(
	baseName = "project.action.properties",
	controller
) {
	override val dialogTitle: String get() = Translations.getString("project.dialog.properties.title", currentProperties.name.getTranslation())

	override fun exists(newName: TranslatableText): Boolean =
		managementService.existsName(newName, except = library.uuid)

	override fun update(properties: LibraryProperties) {
		managementService.update(properties)
	}
}