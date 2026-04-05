package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataRepository
import io.antarescircuit.jabbah.app.ApplicationDataView
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager

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