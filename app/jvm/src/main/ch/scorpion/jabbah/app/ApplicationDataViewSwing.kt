package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.swing.FileExtensionFilter
import ch.scorpion.jabbah.io.Storable
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileFilter

/** A [javax.swing] implementation of [ApplicationDataView]*/
class ApplicationDataViewSwing(
	controller: ApplicationDataViewController,
	private val fileExtension: String,
	private val displayName: String
) : ApplicationDataView {

	init {
		controller.view = this
	}

	override fun dispose() {
		throw UnsupportedOperationException("not implemented")
	}

	override fun decideSaveChangedData(action: String): SaveUnchangedDataDecision {
		val answer = JOptionPane.showConfirmDialog(
			SwingUtilities.getWindowAncestor(BusyHandler.topLevel()?.rootPane) ?: Frame.getFrames()[0],
			Translations.getString("application.unsavedData.question"),
			Translations.getString(action),
			JOptionPane.YES_NO_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE)

		return when (answer) {
			JOptionPane.NO_OPTION -> {
				SaveUnchangedDataDecision.No
			}
			JOptionPane.CANCEL_OPTION -> SaveUnchangedDataDecision.Cancel
			JOptionPane.YES_OPTION -> SaveUnchangedDataDecision.Yes
			else -> throw IllegalStateException("unsupported answer")
		}
	}

	override fun defineSavableForStoring(storable: Storable, currentSavable: Savable?): Savable? {
		val fileChooser = JFileChooser()
		fileChooser.isAcceptAllFileFilterUsed = true
		fileChooser.isFileHidingEnabled = true
		fileChooser.fileFilter = createFileFilter()
		if (currentSavable is FileSavable) {
			if (!currentSavable.filePath.isNullOrEmpty()) {
				fileChooser.selectedFile = File(currentSavable.filePath)
			}
		}

		if (fileChooser.showSaveDialog(Frame.getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
			return FileSavable.withPath(fileChooser.selectedFile.absolutePath)
		}

		return null
	}

	override fun defineSavableForLoading(): Savable? {
		val fileChooser = JFileChooser()
		fileChooser.isAcceptAllFileFilterUsed = false
		fileChooser.fileFilter = createFileFilter()
		if (fileChooser.showOpenDialog(Frame.getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
			return FileSavable.withPath(fileChooser.selectedFile.absolutePath)
		}
		return null
	}

	private fun createFileFilter(): FileFilter {
		return FileExtensionFilter(fileExtension, displayName)
	}
}