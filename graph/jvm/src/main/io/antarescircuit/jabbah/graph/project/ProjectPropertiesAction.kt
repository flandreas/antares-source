package io.antarescircuit.jabbah.graph.project

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.library.AbstractLibraryPropertiesAction
import io.antarescircuit.jabbah.graph.library.LibraryProperties
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

class ProjectPropertiesAction(
	controller: LibraryTreeViewController,
	private val managementService: ProjectManagementService = ProjectModule.projectManagementService
) : AbstractLibraryPropertiesAction(
	baseName = "project.action.properties",
	controller
) {
	override val opensDialog: Boolean get() = true

	override val dialogTitle: String get() = Translations.getString("project.dialog.properties.title", currentProperties.name.getTranslation())

	override fun exists(newName: TranslatableText): Boolean =
		managementService.existsName(newName, except = library.uuid)

	override fun update(properties: LibraryProperties) {
		managementService.update(properties)
	}
}