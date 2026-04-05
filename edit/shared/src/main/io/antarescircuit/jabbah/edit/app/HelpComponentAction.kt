package io.antarescircuit.jabbah.edit.app

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.help.HelpIdProvider
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Component

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