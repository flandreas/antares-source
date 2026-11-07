package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.DesktopApplication

/**
 * An [Action] for quitting the [DesktopApplication].
 */
class QuitApplicationAction(
    application: DesktopApplication
) : AbstractApplicationAction("file.action.quit", application) {

    override fun execute(event: io.antarescircuit.jabbah.base.event.ActionEvent) {
	    (application as DesktopApplication).quit()
    }
}