package ch.scorpion.jabbah.base.ui

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Opens a browser to display help content.
 *
 * @property helpIdProvider provides a URL segment to be appended to [BaseModule.baseDocumentationUrl]
 * that specified the URL of the help page. Should usually start with "/".
 */
class HelpAction(
	private val helpIdProvider: () -> HelpId?,
	imagePath: String = LARGE_IMAGE
) : AbstractAction("base.action.help", imagePath = imagePath) {

	companion object {
		private const val SMALL_IMAGE = "/img/help.png"
		private const val LARGE_IMAGE = "/img/help24.png"

		fun withSmallImage(helpId: HelpId?): HelpAction = HelpAction({ helpId }, imagePath = SMALL_IMAGE)
		fun withSmallImage(helpIdProvider: () -> HelpId?): HelpAction = HelpAction(helpIdProvider, imagePath = SMALL_IMAGE)
	}

	constructor(helpId: HelpId) : this({ helpId })

	init {
		enabled = BaseModule.baseDocumentationUrl != null
	}

	override fun execute(event: ActionEvent) {
		BaseModule.helpProvider.provideHelpFor(helpIdProvider())
	}
}