package io.antarescircuit.antares.model.truthtable

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ApplicationDataViewController
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.graph.library.AbstractLibraryItemSavable
import io.antarescircuit.jabbah.io.Storable

class TruthTableSavable(
	item: TruthTableLibraryItem,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItemSavable(item) {

	override val typeName: String get() = Translations.getString("library.element.truthTable.name")

	private val truthTableLibraryItem: TruthTableLibraryItem get() = item as TruthTableLibraryItem

	override val supportsMostRecent: Boolean get() = true

	override val description: String get() = "$typeName \"${truthTableLibraryItem.name}\""

	override fun equals(other: Any?): Boolean {
		if (other !is TruthTableSavable) {
			return false
		}
		return truthTableLibraryItem.uuid == other.truthTableLibraryItem.uuid
	}

	override fun hashCode(): Int = truthTableLibraryItem.uuid.hashCode()

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