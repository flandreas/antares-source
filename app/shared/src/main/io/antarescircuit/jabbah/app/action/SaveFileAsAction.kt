package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent

/**
 * An [Action] for saving the [ApplicationData] in a new [Savable].
 */
class SaveFileAsAction(
    application: Application
) : AbstractApplicationAction("file.action.saveAs", application) {

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
	    application.controller.saveAs()
    }
}