package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractLibraryItemSavable
import ch.scorpion.jabbah.graph.project.Project

class TruthTableSavable(
	item: TruthTableLibraryItem,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItemSavable(item) {

	override val typeName: String get() = Translations.getString("library.element.truthTable.name")

	private val truthTableLibraryItem: TruthTableLibraryItem get() = item as TruthTableLibraryItem

	override val description: String = if (item.library is Project) {
		"${Translations.getString("project.savable.prefix")} \"${item.truthTable.name.getTranslation()}\""
	} else {
		"${Translations.getString("library.savable.prefix")} \"${item.truthTable.name.getTranslation()}\""
	}

	override val editable: Boolean
		get() = item.library?.let { Authorizer.isCurrentUserAuthorizedTo(Operation.Change, it) } ?: false

	override fun open(application: Application): Boolean {
		eventBus.post(OpenTruthTableItemRequest(truthTableLibraryItem))
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