package io.antarescircuit.jabbah.app.action

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.draw.view.ContentViewManager
import io.antarescircuit.jabbah.io.Storable

/** An [Action] for closing the current [Storable]. */
class CloseFileAction(
	application: Application,
	val viewManager: ContentViewManager
) : AbstractApplicationAction("file.action.close", application) {

	constructor(application: Application): this(application, DrawViewModule.viewManager)

	override fun execute(event: ActionEvent) {
		application.controller.closeData()
	}
}