package ch.scorpion.jabbah.edit.app

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.help.HelpIdProvider
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component

/** Displays help for the one and only selected [Component]. */
class HelpComponentAction(
	baseName: String = "base.action.help"
) : AbstractSelectionAwareAction(baseName) {

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && selectionCount == 1 && singleSelection is HelpIdProvider

	override fun execute(event: ActionEvent) {
		BaseModule.helpProvider.provideHelpFor((singleSelection as HelpIdProvider).helpId)
	}
}