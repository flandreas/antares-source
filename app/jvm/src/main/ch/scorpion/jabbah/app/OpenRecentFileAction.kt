package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.app.action.AbstractApplicationAction
import ch.scorpion.jabbah.base.Translations
import java.awt.Frame
import javax.swing.JOptionPane

/** An [Action] for opening a recently opened [Savable] again.*/
class OpenRecentFileAction(
	private val savable: Savable,
	application: Application
) : AbstractApplicationAction(name = savable.description, description = null, accelerator = null, application = application) {

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
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