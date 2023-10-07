package ch.scorpion.jabbah.base.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Opens a browser to display help content.
 *
 * @property helpId an URL segment to be appended to [BaseModule.baseDocumentationUrl]
 * that specified the URL of the help page. Should usually start with "/".
 */
class HelpAction(
	private val helpId: HelpId
) : AbstractAction("base.action.help", imagePath = "/img/help24.png") {

	init {
		enabled = BaseModule.baseDocumentationUrl != null
	}

	override fun execute(event: ActionEvent) {
		BaseModule.helpProvider.provideHelpFor(helpId)
	}
}