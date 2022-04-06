package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.io.Storable

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