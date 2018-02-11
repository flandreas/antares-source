package ch.scorpion.jabbah.app.action

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.Savable

/** An [Action] for opening a recently opened [Savable] again.*/
class OpenRecentFileAction(
        private val savable: Savable,
        application: Application
) : AbstractApplicationAction(name = savable.description, description = null, accelerator = null, application = application) {

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
	    savable.open(application)
    }
}