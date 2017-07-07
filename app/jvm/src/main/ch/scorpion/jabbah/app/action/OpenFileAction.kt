package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.DesktopApplication
import javax.swing.JFileChooser
import ch.scorpion.jabbah.app.ApplicationFileFilter
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import java.awt.Component
import java.awt.event.ActionEvent

/**
 * An [Action] for opening a [Storable] from a file in the current [View].
 */
class OpenFileAction(
    application: DesktopApplication,
    val viewManager: ViewManager
) : AbstractApplicationAction("file.action.open", application) {

    constructor(application: DesktopApplication): this(application, DrawViewModule.viewManager)

    override fun actionPerformed(e: ActionEvent?) {
        val fileChooser = JFileChooser()
        fileChooser.isAcceptAllFileFilterUsed = false
        fileChooser.fileFilter = ApplicationFileFilter(application)

        if (fileChooser.showOpenDialog(viewManager.activeView!!.canvas as Component) == JFileChooser.APPROVE_OPTION) {
            application.openFile(fileChooser.selectedFile.absolutePath)
        }
    }
}