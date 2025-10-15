package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.help.HelpIdProvider
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

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