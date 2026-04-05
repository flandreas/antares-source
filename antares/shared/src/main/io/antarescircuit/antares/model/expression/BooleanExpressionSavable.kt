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

	private val expressionLibraryItem: BooleanExpressionLibraryItem get() = item as BooleanExpressionLibraryItem

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
