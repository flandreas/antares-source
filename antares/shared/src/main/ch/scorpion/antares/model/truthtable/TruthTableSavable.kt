package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation

class TruthTableSavable(
	val item: TruthTableLibraryItem,
	private val eventBus: EventBus = BaseModule.eventBus
) : Savable {

	override val description: String = "TODO: SavableDesc"

	override val defined: Boolean get() = true

	override val supportsMostRecent: Boolean get() = true

	override val editable: Boolean
		get() = item.library?.let { Authorizer.isCurrentUserAuthorizedTo(Operation.Change, it) } ?: false

	override fun open(application: Application): Boolean {
		eventBus.post(OpenTruthTableItemRequest(item))
		return true
	}

	override fun save(appDataViewController: ApplicationDataViewController): Boolean {
		with (item.library!!) {
			libraryService.updateLibraryItem(this, item)
		}
		appDataViewController.data = appDataViewController.data!!.withSavable(this)
		return true
	}
}