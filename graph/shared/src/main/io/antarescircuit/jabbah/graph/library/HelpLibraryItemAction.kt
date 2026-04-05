package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.help.HelpIdProvider
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

/** Provides help for a [LibraryItem].*/
class HelpLibraryItemAction(
	controller: LibraryTreeViewController
) : AbstractLibraryAction("base.action.help", Operation.View, controller) {

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && selectedItem is HelpIdProvider

	override fun execute(event: ActionEvent) {
		BaseModule.helpProvider.provideHelpFor((selectedItem as HelpIdProvider).helpId)
	}
}