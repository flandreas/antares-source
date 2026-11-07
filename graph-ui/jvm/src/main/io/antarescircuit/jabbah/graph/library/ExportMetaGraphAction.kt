package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.io.WriteFileWrapper
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.model.vertice.BrokenReferenceException
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
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

	override val opensDialog: Boolean get() = true

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

			WriteFileWrapper.wrap(name) {
				try {
					service.exportMetaGraphBundle(element, LibraryModule.libraryHolder, path)
					JOptionPane.showConfirmDialog(
						Frame.getFrames()[0],
						Translations.getString("library.action.exportMetaGraph.success.msg", metaGraph.name, path),
						name,
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE
					)
				} catch (_: BrokenReferenceException) {
					JOptionPane.showMessageDialog(
						Frame.getFrames()[0],
						Translations.getString("library.action.exportMetaGraph.brokenRef.msg"),
						name,
						JOptionPane.ERROR_MESSAGE)
				}
			}
		}
	}
}