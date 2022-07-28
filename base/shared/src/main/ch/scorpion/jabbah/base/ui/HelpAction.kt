package ch.scorpion.jabbah.base.ui

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.ActionEvent

/**
 * Opens a browser to display help content.
 *
 * @property helpId an URL segment to be appended to [BaseModule.baseDocumentationUrl]
 * that specified the URL of the help page. Should usually start with "/".
 */
class HelpAction(
	private val helpId: String
) : AbstractAction("base.action.help", imagePath = "/img/help24.png") {

	init {
		enabled = BaseModule.baseDocumentationUrl != null
	}

	override fun execute(event: ActionEvent) {
		System.browse("${BaseModule.baseDocumentationUrl!!.invoke()}$helpId", name)
	}
}