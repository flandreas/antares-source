package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.library.LibraryImportResultType.*
import ch.scorpion.jabbah.graph.project.Project
import org.apache.commons.io.FilenameUtils
import java.awt.Component
import javax.swing.JOptionPane

/**
 * A UI-oriented process for importing [Libraries][Library].
 * Subclasses distinguish between [Library] and [Project] imports.
 */
abstract class AbstractLibraryImportProcess(
	private val managementService: AbstractLibraryManagementService,
	private val parentComponent: Component,
	private val dialogTitle: String,
	private val successHandler: (Library) -> Unit
) {

	companion object {
		private val LOG by logger(AbstractLibraryImportProcess::class)
	}

	protected abstract val logName: String
	protected abstract fun getImportSuccessMsg(name: String): String
	protected abstract fun getAlreadyExistsMsg(name: String): String
	protected abstract fun getInvalidMsg(name: String): String
	protected abstract fun getStaleReferenceMsg(name: String): String
	protected abstract fun getUuidAlreadyExistsMsg(): String

	fun import(path: String) {
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


	private fun handleSuccessfulImport(libraryName: String, library: Library) {
		JOptionPane.showConfirmDialog(
			parentComponent,
			getImportSuccessMsg(libraryName),
			dialogTitle,
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.INFORMATION_MESSAGE)

		successHandler(library)
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