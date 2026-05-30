package io.antarescircuit.antares.model.expression

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ApplicationDataViewController
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.graph.library.AbstractLibraryItemSavable
import io.antarescircuit.jabbah.io.Storable

class BooleanExpressionSavable(
	item: BooleanExpressionLibraryItem,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItemSavable(item) {

	override val typeName: String get() = Translations.getString("library.element.booleanExpression.name")

	override val supportsMostRecent: Boolean get() = true

	private val expressionLibraryItem: BooleanExpressionLibraryItem get() = item as BooleanExpressionLibraryItem

	override val description: String get() = "$typeName \"${expressionLibraryItem.name}\""

	override fun equals(other: Any?): Boolean {
		if (other !is BooleanExpressionSavable) {
			return false
		}
		return expressionLibraryItem.uuid == other.expressionLibraryItem.uuid
	}

	override fun hashCode(): Int = expressionLibraryItem.uuid.hashCode()

	override fun open(application: Application): Boolean {
		eventBus.post(OpenBooleanExpressionItemRequest(expressionLibraryItem))
		return true
	}

	override fun save(appDataViewController: ApplicationDataViewController): Boolean {
		expressionLibraryItem.updateStorable(appDataViewController.data!!.content as BooleanExpressionStorable)
		with (item.library!!) {
			libraryService.updateLibraryItem(this, item)
		}
		appDataViewController.data = appDataViewController.data!!.withSavable(this)
		return true
	}

	override fun getPropertyBean(storable: Storable): Bean = storable as BooleanExpressionStorable
}
