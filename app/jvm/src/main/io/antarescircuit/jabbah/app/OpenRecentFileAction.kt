package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.app.action.AbstractApplicationAction
import io.antarescircuit.jabbah.base.Translations
import java.awt.Frame
import javax.swing.JOptionPane

/** An [Action] for opening a recently opened [Savable] again.*/
class OpenRecentFileAction(
	private val savable: Savable,
	application: Application
) : AbstractApplicationAction(name = savable.description, description = null, accelerator = null, application = application) {

	override fun execute(event: io.antarescircuit.jabbah.base.event.ActionEvent) {
		if (!savable.open(application)) {
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