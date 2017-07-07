package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.DesktopApplication
import java.awt.event.ActionEvent

/**
 * An [Action] for creating a new file.
 */
class NewFileAction(application: DesktopApplication) : AbstractApplicationAction("file.action.new", application) {

    override fun actionPerformed(e: ActionEvent?) {
        application.newFile()
    }
}