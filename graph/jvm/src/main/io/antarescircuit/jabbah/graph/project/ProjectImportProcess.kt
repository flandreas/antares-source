package io.antarescircuit.jabbah.graph.project

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.auth.User
import io.antarescircuit.jabbah.edit.auth.UserHolder
import io.antarescircuit.jabbah.graph.library.AbstractLibraryImportProcess
import io.antarescircuit.jabbah.graph.library.AbstractLibraryManagementService
import io.antarescircuit.jabbah.graph.library.Library
import java.awt.Component
import java.awt.Frame

class ProjectImportProcess(
	managementService: AbstractLibraryManagementService = ProjectModule.projectManagementService,
	userHolder: UserHolder<User> = EditAuthModule.userHolder,
	parentComponent: Component = Frame.getFrames()[0],
	dialogTitle: String = Translations.getString("project.dialog.import.action.name"),
	successHandler: (Library, AbstractLibraryImportProcess) -> Unit
): AbstractLibraryImportProcess(managementService, userHolder, parentComponent, dialogTitle, successHandler) {

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