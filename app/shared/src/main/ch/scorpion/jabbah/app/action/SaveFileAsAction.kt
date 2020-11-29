package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent

/**
 * An [Action] for saving the [ApplicationData] in a new [Savable].
 */
class SaveFileAsAction(
    application: Application
) : AbstractApplicationAction("file.action.saveAs", application) {

    override fun execute(event: ActionEvent) {
	    application.controller.saveAs()
    }
}