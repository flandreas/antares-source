package io.antarescircuit.jabbah.draw.svg

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.io.WriteFileWrapper
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.draw.view.AbstractViewAction
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

class ExportSvgAction : AbstractViewAction("draw.action.exportSvg") {

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		view?.let {
			val mainContent = it.mainContent
			val fileChooser = JFileChooser()
			fileChooser.dialogTitle = name
			fileChooser.selectedFile = File("${RichText.stripToPlainText(mainContent.name)}.svg")

			if (fileChooser.showSaveDialog(Frame.getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
				WriteFileWrapper.wrap(name) {
					SvgExporter.export(mainContent, fileChooser.selectedFile.absolutePath)
				}
			}
		}
	}
}