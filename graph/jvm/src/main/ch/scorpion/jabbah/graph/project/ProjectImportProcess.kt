package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.library.AbstractLibraryImportProcess
import ch.scorpion.jabbah.graph.library.AbstractLibraryManagementService
import ch.scorpion.jabbah.graph.library.Library
import java.awt.Component

class ProjectImportProcess(
	managementService: AbstractLibraryManagementService,
    parentComponent: Component,
    dialogTitle: String,
	successHandler: (Library) -> Unit
): AbstractLibraryImportProcess(managementService, parentComponent, dialogTitle, successHandler) {

	override val logName: String get() = "project"

	override fun getImportSuccessMsg(name: String): String =
		Translations.getString("project.dialog.import.success.msg", name)

	override fun getAlreadyExistsMsg(name: String): String =
		Translations.getString("project.dialog.import.alreadyExists.msg", name)

	override fun getInvalidMsg(name: String): String =
		Translations.getString("project.dialog.import.invalid.msg", name)

	override fun getStaleReferenceMsg(name: String): String =
		Translations.getString("project.dialog.import.staleLibraryReference.msg", name)

	override fun getUuidAlreadyExistsMsg(): String =
		Translations.getString("project.dialog.import.uuidAlreadyExists.msg")
}