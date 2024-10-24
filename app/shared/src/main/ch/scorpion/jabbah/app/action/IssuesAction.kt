package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.ActionEvent

class IssuesAction(
	application: Application
) : AbstractApplicationAction("help.action.issues", application) {

	init {
	    enabled = application.issuesUrl != null
	}

	override fun execute(event: ActionEvent) {
		System.browse(application.issuesUrl!!, name)
	}
}