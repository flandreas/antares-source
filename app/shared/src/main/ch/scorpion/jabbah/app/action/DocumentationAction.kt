package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.ActionEvent

class DocumentationAction(
	application: Application
) : AbstractApplicationAction("help.action.documentation", application) {

	init {
		enabled = application.documentationUrl != null
	}

	override fun execute(event: ActionEvent) {
		System.browse(application.documentationUrl!!, name)
	}
}