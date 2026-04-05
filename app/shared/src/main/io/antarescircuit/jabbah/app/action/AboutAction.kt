package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent

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