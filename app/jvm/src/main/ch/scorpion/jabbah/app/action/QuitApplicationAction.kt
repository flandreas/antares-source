package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.DesktopApplication
import java.awt.event.ActionEvent

/**
 * An [Action] for quitting the [DesktopApplication].
 */
class QuitApplicationAction(
    application: DesktopApplication
) : AbstractApplicationAction("file.action.quit", application) {

    override fun actionPerformed(e: ActionEvent?) {
        application.quit()
    }
}