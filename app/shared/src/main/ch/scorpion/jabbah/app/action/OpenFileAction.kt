package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager

/**
 * An [Action] for opening a [Storable] from a file in the current [View].
 */
class OpenFileAction(
	application: Application,
	val viewManager: ViewManager
) : AbstractApplicationAction("file.action.open", application) {

    constructor(application: Application): this(application, DrawViewModule.viewManager)

    override fun execute(event: ActionEvent) {
	    application.open()
    }
}