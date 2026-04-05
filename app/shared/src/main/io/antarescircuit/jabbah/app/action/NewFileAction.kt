package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent

/**
 * An [Action] for creating a new file.
 */
class NewFileAction(application: Application) : AbstractApplicationAction("file.action.new", application) {

    override fun execute(event: ActionEvent) {
	    application.controller.newData()
    }
}