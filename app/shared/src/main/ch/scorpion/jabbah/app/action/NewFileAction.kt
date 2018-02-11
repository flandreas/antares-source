package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent

/**
 * An [Action] for creating a new file.
 */
class NewFileAction(application: Application) : AbstractApplicationAction("file.action.new", application) {

    override fun execute(event: ActionEvent) {
	    application.newFile()
    }
}