package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.edit.auth.UserHolder
import ch.scorpion.jabbah.graph.library.LibraryImportResultType.*
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectImportProcess
import org.apache.commons.io.FilenameUtils
import java.awt.Component
import javax.swing.JOptionPane

/**
 * A UI-oriented process for importing [Libraries][Library].
 * Subclasses distinguish between [Library] and [Project] imports.
 */
abstract class AbstractLibraryImportProcess(
	private val managementService: AbstractLibraryManagementService,
	private val userHolder: UserHolder<User> = EditAuthModule.userHolder,
	private val parentComponent: Component,
	private val dialogTitle: String,
	private val successHandler: (Library, AbstractLibraryImportProcess) -> Unit
) {

	companion object {

		private val LOG by logger(AbstractLibraryImportProcess::class)

		/** The name of the [String] property of the [Project] import file extension.*/
		const val PROP_PROJECT_FILE_EXTENSION = "graph.projectFileExtension"

		/** The name of the [String] property of the [Library] import file extension.*/
		const val PROP_LIBRARY_FILE_EXTENSION = "graph.libraryFileExtension"

		val projectFileExtension: String by lazy { BaseModule.properties.getString(PROP_PROJECT_FILE_EXTENSION) }
		val projectFileTypeName: String by lazy { Translations.getString("graph.projectFileTypeName") }

		val libraryFileExtension: String by lazy { BaseModule.properties.getString(PROP_LIBRARY_FILE_EXTENSION) }
		val libraryFileTypeName: String by lazy { Translations.getString("graph.libraryFileTypeName") }

		/**
		 * Creates the appropriate subclass instance of this [AbstractLibraryImportProcess]
		 * depending on the file name extension of [path].
		 * @param successHandler the code to be executed after successful import,
		 * such as open the imported [Library]
		 */
		fun forPath(
			path: String,
			successHandler: (Library, AbstractLibraryImportProcess) -> Unit
		): AbstractLibraryImportProcess? {
			return when (FilenameUtils.getExtension(path)) {
				projectFileExtension -> ProjectImportProcess(successHandler = successHandler)
				libraryFileExtension -> LibraryImportProcess(successHandler = successHandler)
				else -> null
			}
		}
	}

	protected abstract val logName: String
	protected abstract fun getImportSuccessMsg(name: String): String
	protected abstract fun getAlreadyExistsMsg(name: String): String
	protected abstract fun getInvalidMsg(name: String): String
	protected abstract fun getStaleReferenceMsg(name: String): String
	protected abstract fun getUuidAlreadyExistsMsg(): String

	suspend fun import(path: String) {
		val name = FilenameUtils.getBaseName(path)
		var replaceIfUuidExists = false
		do {
			var repeat = false
			LOG.userTrail("Import $logName '${name}', replace if UUID exists = $replaceIfUuidExists")
			val result = managementService.import(path, replaceIfUuidExists)
			when (result.type) {
				Success -> handleSuccessfulImport(name, result.library!!)
				NameAlreadyExists -> handleImportNameAlreadyExists(name)
				Invalid -> handleInvalidImportFile(name)
				StaleLibraryReference -> handleStaleLibraryReference(name)
				UuidAlreadyExists -> {
					replaceIfUuidExists = handleReplaceLibraryByImport()
					repeat = replaceIfUuidExists
				}
				QuotaExceeded -> handleQuotaExceeded(result.param!!)
			}
		} while (repeat)
	}

	fun open(library: Library) {
		managementService.open(LibraryIdentification(library.uuid, userHolder.user.identity))
	}

	private fun handleSuccessfulImport(libraryName: String, library: Library) {
		JOptionPane.showConfirmDialog(
			parentComponent,
			getImportSuccessMsg(libraryName),
			dialogTitle,
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.INFORMATION_MESSAGE)

		successHandler(library, this)
	}

	private fun handleImportNameAlreadyExists(libraryName: String) {
		JOptionPane.showConfirmDialog(
			parentComponent,
			getAlreadyExistsMsg(libraryName),
			dialogTitle,
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE)
	}

	private fun handleInvalidImportFile(libraryName: String) {
		JOptionPane.showConfirmDialog(
			parentComponent,
			getInvalidMsg(libraryName),
			dialogTitle,
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE)
	}

	private fun handleStaleLibraryReference(libraryName: String) {
		JOptionPane.showConfirmDialog(
			parentComponent,
			getStaleReferenceMsg(libraryName),
			dialogTitle,
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE)
	}

	private fun handleReplaceLibraryByImport(): Boolean {
		return JOptionPane.showConfirmDialog(
			parentComponent,
			getUuidAlreadyExistsMsg(),
			dialogTitle,
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION
	}

	private fun handleQuotaExceeded(msg: String) {
		JOptionPane.showConfirmDialog(
			parentComponent,
			msg,
			dialogTitle,
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE)
	}
}