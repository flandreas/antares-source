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
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileFilter

abstract class AbstractLibraryPersistencePanel(
	private val managementService: AbstractLibraryManagementService,
	private val userHolder: UserHolder<User> = EditAuthModule.userHolder,
	isOpen: (entry: LibraryDictionaryEntry) -> Boolean,
	private val logName: String,
) : AbstractLibrarySelectionPanel(userHolder, isOpen) {

	companion object {
		private val LOG by logger(AbstractLibraryPersistencePanel::class)
		const val EXPORT_FILE_EXTENSION = "zip"
	}

	protected val exportAction: Action = ExportAction()

	protected val importAction: Action = ImportAction()

	/** Manages various import situations by displaying UI to the user. */
	protected abstract val importProcess: AbstractLibraryImportProcess

	protected abstract val exportActionNameKey: String
	protected abstract val importActionNameKey: String
	protected abstract val fileExtensionFilterName: String
	protected abstract fun getExportSuccessMsg(entry: LibraryDictionaryEntry): String

	/** Executed after successful import. */
	protected fun successHandler(library: Library, process: AbstractLibraryImportProcess) {
		load()
		selectLibrary(library.uuid)
	}

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

		private fun createFilter(): FileFilter = FileExtensionFilter(EXPORT_FILE_EXTENSION, fileExtensionFilterName)

		override fun execute(event: ActionEvent) {
			val fileChooser = JFileChooser()
			fileChooser.dialogTitle = name
			fileChooser.fileFilter = createFilter()
			if (fileChooser.showOpenDialog(this@AbstractLibraryPersistencePanel) == JFileChooser.APPROVE_OPTION) {
				importProcess.import(fileChooser.selectedFile.absolutePath)
			}
		}
	}
}