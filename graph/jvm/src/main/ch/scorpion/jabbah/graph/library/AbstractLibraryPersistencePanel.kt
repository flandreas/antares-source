package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.FileExtensionFilter
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.auth.User
import ch.scorpion.jabbah.edit.auth.UserHolder
import ch.scorpion.jabbah.graph.library.LibraryImportResultType.*
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import org.apache.commons.io.FilenameUtils
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.filechooser.FileFilter

abstract class AbstractLibraryPersistencePanel(
	private val managementService: AbstractLibraryManagementService,
	private val logName: String,
	private val userHolder: UserHolder<User> = EditAuthModule.userHolder
) : JPanel() {

	companion object {
		private val LOG by logger(AbstractLibraryPersistencePanel::class)
		const val EXPORT_FILE_EXTENSION = "zip"
	}

	protected val exportAction: Action = ExportAction()

	protected val importAction: Action = ImportAction()

	protected abstract val selectedLibrary: LibraryDictionaryEntry?
	protected abstract val exportActionNameKey: String
	protected abstract val importActionNameKey: String
	protected abstract val fileExtensionFilterName: String
	protected abstract fun getExportSuccessMsg(entry: LibraryDictionaryEntry): String
	protected abstract fun getImportSuccessMsg(name: String): String
	protected abstract fun getAlreadyExistsMsg(name: String): String
	protected abstract fun getInvalidMsg(name: String): String
	protected abstract fun getStaleReferenceMsg(name: String): String
	protected abstract fun getUuidAlreadyExistsMsg(): String
	protected abstract fun refreshLibraries()

	protected fun getLibraryIdentity(uuid: UUID): LibraryIdentification =
		LibraryIdentification(uuid, userHolder.user.identity)

	private inner class ExportAction : AbstractAction(exportActionNameKey) {
		override fun execute(event: ActionEvent) {
			selectedLibrary?.let {
				val fileChooser = JFileChooser()
				fileChooser.dialogTitle = name
				fileChooser.selectedFile = File("${it.name.value}.${EXPORT_FILE_EXTENSION}")
				if (fileChooser.showSaveDialog(this@AbstractLibraryPersistencePanel) == JFileChooser.APPROVE_OPTION) {
					LOG.userTrail("Export $logName ${it.uuid}")

					managementService.export(getLibraryIdentity(it.uuid), fileChooser.selectedFile.absolutePath)
					JOptionPane.showConfirmDialog(
						this@AbstractLibraryPersistencePanel,
						getExportSuccessMsg(it),
						name,
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE)
				}
			}
		}
	}

	private inner class ImportAction : AbstractAction(importActionNameKey) {
		override fun execute(event: ActionEvent) {
			val fileChooser = JFileChooser()
			fileChooser.dialogTitle = name
			fileChooser.fileFilter = createFilter()
			if (fileChooser.showOpenDialog(this@AbstractLibraryPersistencePanel) == JFileChooser.APPROVE_OPTION) {
				import(fileChooser.selectedFile.absolutePath)
			}
		}

		private fun import(path: String) {
			val name = FilenameUtils.getBaseName(path)
			var replaceIfUuidExists = false
			do {
				var repeat = false
				LOG.userTrail("Import $logName '${name}', replace if UUID exists = $replaceIfUuidExists")
				val result = managementService.import(path, replaceIfUuidExists)
				when (result.type) {
					Success -> handleSuccessfulImport(name)
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

		private fun createFilter(): FileFilter = FileExtensionFilter(EXPORT_FILE_EXTENSION, fileExtensionFilterName)

		fun handleSuccessfulImport(libraryName: String) {
			JOptionPane.showConfirmDialog(
				this@AbstractLibraryPersistencePanel,
				getImportSuccessMsg(libraryName),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE)
			refreshLibraries()
		}

		fun handleImportNameAlreadyExists(libraryName: String) {
			JOptionPane.showConfirmDialog(
				this@AbstractLibraryPersistencePanel,
				getAlreadyExistsMsg(libraryName),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE)
		}

		fun handleInvalidImportFile(libraryName: String) {
			JOptionPane.showConfirmDialog(
				this@AbstractLibraryPersistencePanel,
				getInvalidMsg(libraryName),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE)
		}

		fun handleStaleLibraryReference(libraryName: String) {
			JOptionPane.showConfirmDialog(
				this@AbstractLibraryPersistencePanel,
				getStaleReferenceMsg(libraryName),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE)
		}

		fun handleReplaceLibraryByImport(): Boolean {
			return JOptionPane.showConfirmDialog(
				this@AbstractLibraryPersistencePanel,
				getUuidAlreadyExistsMsg(),
				name,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION
		}

		fun handleQuotaExceeded(msg: String) {
			JOptionPane.showConfirmDialog(
				this@AbstractLibraryPersistencePanel,
				msg,
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE)
		}
	}
}