package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.richtext.RichText
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane

/**
 * Exports a [MetaGraph] and all its dependents that are not contained in a standard [Library]
 * to an external ZIP file.
 */
class ExportMetaGraphAction(
	controller: LibraryTreeViewController
) : AbstractContainerLibraryElementAction(
	actionBaseName = "library.action.exportMetaGraph",
	operation = Operation.View,
	controller
) {

	companion object {
		private val LOG by logger(ExportMetaGraphAction::class)
		const val EXPORT_FILE_EXTENSION = "zip"
	}

	override fun execute(event: ActionEvent) {
		val element = selectedItem as ContainerLibraryElement
		val service = selectedItem!!.library!!.libraryService
		val metaGraph = service.getMetaGraph(element.library!!, element)

		val fileChooser = JFileChooser()
		val title = Translations.getString("library.action.exportMetaGraph.title", metaGraph.name)
		fileChooser.dialogTitle = title
		fileChooser.selectedFile = File("${RichText.stripToPlainText(metaGraph.name)}.$EXPORT_FILE_EXTENSION")
		if (fileChooser.showSaveDialog(Frame.getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
			LOG.userTrail("Export '${metaGraph.name} as bundle")
			val path = fileChooser.selectedFile.absolutePath
			service.exportMetaGraphBundle(element, LibraryModule.libraryHolder, path)
			JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("library.action.exportMetaGraph.success.msg", metaGraph.name, path),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE
			)
		}
	}
}