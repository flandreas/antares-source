package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.edit.auth.UserHolder
import java.awt.Component
import java.awt.Frame

class LibraryImportProcess(
	managementService: AbstractLibraryManagementService = LibraryModule.libraryManagementService,
	userHolder: UserHolder<User> = EditAuthModule.userHolder,
	parentComponent: Component = Frame.getFrames()[0],
	dialogTitle: String = Translations.getString("library.dialog.import.action.name"),
	successHandler: (Library, AbstractLibraryImportProcess) -> Unit
): AbstractLibraryImportProcess(managementService, userHolder, parentComponent, dialogTitle, successHandler) {

	override val logName: String get() = "library"

	override fun getImportSuccessMsg(name: String): String =
		Translations.getString("library.dialog.import.success.msg", name)

	override fun getAlreadyExistsMsg(name: String): String =
		Translations.getString("library.dialog.import.alreadyExists.msg", name)

	override fun getInvalidMsg(name: String): String =
		Translations.getString("library.dialog.import.invalid.msg", name)

	override fun getStaleReferenceMsg(name: String): String =
		Translations.getString("library.dialog.import.staleLibraryReference.msg", name)

	override fun getUuidAlreadyExistsMsg(): String =
		Translations.getString("library.dialog.import.uuidAlreadyExists.msg")
}