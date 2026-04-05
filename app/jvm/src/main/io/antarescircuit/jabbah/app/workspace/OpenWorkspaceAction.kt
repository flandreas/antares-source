package io.antarescircuit.jabbah.app.workspace

import io.antarescircuit.jabbah.app.DesktopApplication
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.event.ActionEvent
import java.awt.Frame

class OpenWorkspaceAction(
	private val application: DesktopApplication
) : AbstractAction("file.action.openWorkspace", opensDialog = true) {

	override fun execute(event: ActionEvent) {
		WorkspacePanel.showAsDialog(name, Frame.getFrames()[0], application)
	}
}