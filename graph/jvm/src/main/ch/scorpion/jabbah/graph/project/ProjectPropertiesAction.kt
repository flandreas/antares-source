package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.AbstractLibraryPropertiesAction
import ch.scorpion.jabbah.graph.library.LibraryProperties

class ProjectPropertiesAction(
	private val projectHolder: ProjectHolder = ProjectModule.projectHolder,
	private val managementService: ProjectManagementService = ProjectModule.projectManagementService.invoke()
) : AbstractLibraryPropertiesAction(
	baseName = "project.action.properties"
) {

	override val isEditable: Boolean get() = Authorizer.isCurrentUserAuthorizedTo(Operation.Change, projectHolder.project!!)

	override val isSystem: Boolean get() = false

	override val currentProperties: LibraryProperties get() = projectHolder.project!!.properties

	override val dialogTitle: String get() = Translations.getString("project.dialog.properties.title", currentProperties.name.getTranslation())

	override val emptyMessage: String get() = Translations.getString("library.emptyName.msg")

	override fun duplicateMessage(newName: String): String = Translations.getString("library.duplicate.msg", newName)

	override fun exists(newName: TranslatableText): Boolean =
		managementService.existsName(newName, except = projectHolder.project?.uuid)

	override fun update(properties: LibraryProperties) {
		managementService.update(properties)
	}
}