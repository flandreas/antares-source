package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.DesktopApplication
import java.awt.event.ActionEvent

/**
 * An [Action] for saving the [Storable] in a new file.
 */
class SaveFileAsAction(
    application: DesktopApplication
) : AbstractApplicationAction("file.action.saveAs", application) {

    override fun actionPerformed(e: ActionEvent?) {
        application.saveAs()
    }
}