package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import java.awt.Component

class LibraryImportProcess(
	managementService: AbstractLibraryManagementService,
	parentComponent: Component,
	dialogTitle: String,
	successHandler: (Library) -> Unit
): AbstractLibraryImportProcess(managementService, parentComponent, dialogTitle, successHandler) {

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