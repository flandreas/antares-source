package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

/** An [Action] for exporting the log file in the user's data directory.*/
class ExportLogfileAction(
	application: DesktopApplication
) : AbstractApplicationAction("file.action.exportLog", application) {

	override fun execute(event: ActionEvent) {
		val fileChooser = JFileChooser()
		fileChooser.dialogTitle = name
		fileChooser.selectedFile = File("${application.systemName}-log.zip")
		fileChooser.currentDirectory = File(System.getProperty("user.home"))
		if (fileChooser.showSaveDialog(Frame.getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
			(application as DesktopApplication).exportLogfile(fileChooser.selectedFile.absolutePath)
		}
	}
}