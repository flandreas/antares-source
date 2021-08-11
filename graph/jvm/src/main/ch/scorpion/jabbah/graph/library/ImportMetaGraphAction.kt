package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.FileExtensionFilter
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import ch.scorpion.jabbah.graph.MetaGraphBundle
import ch.scorpion.jabbah.graph.library.MetaGraphBundleImportResult.*
import org.apache.commons.io.FilenameUtils
import java.awt.Frame
import javax.swing.JFileChooser
import javax.swing.JOptionPane

/**
 * Imports a [MetaGraphBundle] from a ZIP file.
 */
class ImportMetaGraphAction(
	controller: LibraryTreeViewController,
	private val operationTarget: () -> Any?,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction(
	"library.action.importMetaGraph",
	controller.applicationModeHolder,
	Operation.Change,
	controller,
	eventBus
) {

	companion object {
		private val LOG by logger(ImportMetaGraphAction::class)
	}

	override val operationAuthorized: Boolean
		get() = operationTarget.invoke() != null && Authorizer.isCurrentUserAuthorizedTo(operation, operationTarget.invoke()!!)

	override fun execute(event: ActionEvent) {
		val fileChooser = JFileChooser()
		fileChooser.dialogTitle = name
		fileChooser.fileFilter = createFilter()
		if (fileChooser.showOpenDialog(Frame.getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
			import(fileChooser.selectedFile.absolutePath)
		}
	}

	private fun import(path: String) {
		val destination = selectedFolder
		val service = destination.library!!.libraryService
		val bundleName = FilenameUtils.getBaseName(path)
		var replaceIfUuidExists = false
		do {
			var repeat = false
			LOG.debug("Import bundle '${bundleName}', replace if UUID exists = $replaceIfUuidExists")

			when (service.importMetaGraphBundle(path, bundleName, destination, replaceIfUuidExists)) {
				Success -> handleSuccessfulImport(bundleName)
				Invalid -> handleInvalidImportFile(bundleName)
				StaleLibraryReference -> handleStaleLibraryReference(bundleName)
				UuidAlreadyExists -> {
					replaceIfUuidExists = when (handleReplaceMetaGraphsByImport()) {
						true -> true
						false -> false
						null -> return
					}
					repeat = replaceIfUuidExists
				}
			}

		} while (repeat)
	}

	private fun createFilter(): javax.swing.filechooser.FileFilter =
		FileExtensionFilter(ExportMetaGraphAction.EXPORT_FILE_EXTENSION, Translations.getString("library.action.importMetaGraph.filer.name"))

	private fun handleSuccessfulImport(bundleName: String) {
		JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("library.action.importMetaGraph.success.msg"),
			name,
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.INFORMATION_MESSAGE)
	}

	private fun handleInvalidImportFile(bundleName: String) {
		JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("library.action.importMetaGraph.invalid.msg", bundleName),
			name,
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE)
	}

	private fun handleStaleLibraryReference(bundleName: String) {
		JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("library.action.importMetaGraph.staleLibraryReference.msg", bundleName),
			name,
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE)
	}

	private fun handleReplaceMetaGraphsByImport(): Boolean? {
		val result = JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("library.action.importMetaGraph.uuidAlreadyExists.msg"),
			name,
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE)

		return when (result) {
			JOptionPane.YES_OPTION -> true
			JOptionPane.NO_OPTION -> false
			else -> null
		}
	}
}