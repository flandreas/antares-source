package ch.scorpion.jabbah.app.workspace

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import java.awt.Frame

class OpenWorkspaceAction : AbstractAction("file.action.openWorkspace") {

	override fun execute(event: ActionEvent) {
		WorkspacePanel.showAsDialog(Frame.getFrames()[0])
	}
}