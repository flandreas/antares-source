package ch.scorpion.jabbah.app.workspace

import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import java.awt.Frame

class OpenWorkspaceAction(
	private val application: DesktopApplication
) : AbstractAction("file.action.openWorkspace", opensDialog = true) {

	override fun execute(event: ActionEvent) {
		WorkspacePanel.showAsDialog(name, Frame.getFrames()[0], application)
	}
}