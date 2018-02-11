package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.DesktopApplication

/**
 * An [Action] for quitting the [DesktopApplication].
 */
class QuitApplicationAction(
    application: DesktopApplication
) : AbstractApplicationAction("file.action.quit", application) {

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
	    (application as DesktopApplication).quit()
    }
}