package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.event.ActionEvent

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