package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataRepository
import ch.scorpion.jabbah.app.ApplicationDataView
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ContentViewManager

/**
 * An [Action] for opening [ApplicationData] from a [ApplicationDataRepository]
 * and display it in the current [ApplicationDataView].
 */
class OpenFileAction(
	application: Application,
	val viewManager: ContentViewManager
) : AbstractApplicationAction("file.action.open", application) {

    constructor(application: Application): this(application, DrawViewModule.viewManager)

    override fun execute(event: ActionEvent) {
	    application.controller.open()
    }
}