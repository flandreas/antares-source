package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.app.action.AbstractApplicationAction
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.logger
import java.awt.Frame
import javax.swing.JOptionPane

/** An [Action] for opening a recently opened [Savable] again.*/
class OpenRecentFileAction(
	private val savable: Savable,
	application: Application
) : AbstractApplicationAction(name = savable.description, description = null, accelerator = null, application = application) {

	companion object {
		private val LOG by logger(OpenRecentFileAction::class)
	}

	override fun execute(event: ActionEvent) {
		LOG.userTrail("Open recent file '${savable.description}'")

		if (!savable.open()) {
			JOptionPane.showConfirmDialog(
				Frame.getFrames()[0],
				Translations.getString("file.action.openRecent.cannotOpen.text"),
				Translations.getString("file.action.openRecent.name"),
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE
			)
		}
	}
}