package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.DesktopApplication
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.io.WriteFileWrapper
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

/** An [Action] for exporting the log file in the user's data directory.*/
class ExportLogfileAction(
	application: DesktopApplication
) : AbstractApplicationAction("file.action.exportLog", application) {

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		val fileChooser = JFileChooser()
		fileChooser.dialogTitle = name
		fileChooser.selectedFile = File("${application.systemName}-log.zip")
		fileChooser.currentDirectory = File(System.getProperty("user.home"))
		if (fileChooser.showSaveDialog(Frame.getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
			WriteFileWrapper.wrap(name) {
				(application as DesktopApplication).exportLogfile(fileChooser.selectedFile.absolutePath)
			}
		}
	}
}