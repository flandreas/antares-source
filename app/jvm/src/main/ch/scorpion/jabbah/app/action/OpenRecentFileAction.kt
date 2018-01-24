package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.DesktopApplication
import javax.swing.Action
import ch.scorpion.jabbah.app.Savable
import java.awt.event.ActionEvent

/** An [Action] for opening a recently opened [Savable] again.*/
class OpenRecentFileAction(
        private val savable: Savable,
        application: DesktopApplication
) : AbstractApplicationAction(name = savable.description, description = null, accelerator = null, application = application) {

    override fun actionPerformed(e: ActionEvent?) {
        savable.open(application)
    }
}