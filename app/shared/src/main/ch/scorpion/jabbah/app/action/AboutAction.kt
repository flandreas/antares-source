package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent

class AboutAction(
	application: Application
) : AbstractApplicationAction(
	name = "${Translations.getString("application.action.about.name")} ${application.displayName}",
	application = application
) {

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		application.showAboutInfo()
	}
}