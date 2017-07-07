package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.edit.CommandEvent
import ch.scorpion.jabbah.edit.CommandManager
import java.awt.event.ActionEvent
import javax.swing.Action

/**
 * An [Action] for saving the current [Storable] with the current file path.
 */
class SaveFileAction(
        application: DesktopApplication,
        eventBus: EventBus,
        val commandManager: CommandManager
) : AbstractApplicationAction("file.action.save", application) {

    constructor(application: DesktopApplication): this(application, BaseModule.eventBus, application.mainFrame.editor.commandManager)

    init {
        isEnabled = false
        eventBus.register(CommandEvent::class, {update()})
        eventBus.register(CurrentSavableEvent::class, {update()})
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (application.savable != null && application.savable!!.defined) {
            application.save()
        } else {
            application.saveAs()
        }
    }

    private fun update() {
        isEnabled = application.savable == null || !application.savable!!.defined || commandManager.canUndo()
    }
}