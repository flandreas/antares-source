package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.graph.library.AbstractLibraryItemSavable
import ch.scorpion.jabbah.io.Storable

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
