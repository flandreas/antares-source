package ch.scorpion.jabbah.draw.svg

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.richtext.RichText
import ch.scorpion.jabbah.draw.view.AbstractViewAction
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
				SvgExporter.export(mainContent, fileChooser.selectedFile.absolutePath)
			}
		}
	}
}