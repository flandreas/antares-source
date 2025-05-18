package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.graph.library.AbstractLibraryItemSavable
import ch.scorpion.jabbah.io.Storable

class TruthTableSavable(
	item: TruthTableLibraryItem,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItemSavable(item) {

	override val typeName: String get() = Translations.getString("library.element.truthTable.name")

	private val truthTableLibraryItem: TruthTableLibraryItem get() = item as TruthTableLibraryItem

	override fun open(application: Application): Boolean {
		eventBus.post(OpenTruthTableItemRequest(truthTableLibraryItem))
		return true
	}

	override fun save(appDataViewController: ApplicationDataViewController): Boolean {
		truthTableLibraryItem.updateStorable((appDataViewController.data!!.content as TruthTable))
		with (item.library!!) {
			libraryService.updateLibraryItem(this, item)
		}
		appDataViewController.data = appDataViewController.data!!.withSavable(this)
		return true
	}

	override fun getPropertyBean(storable: Storable): Bean = storable as TruthTable
}