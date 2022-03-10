package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractLibraryItemSavable
import ch.scorpion.jabbah.graph.project.Project

class BooleanExpressionSavable(
	item: BooleanExpressionLibraryItem,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItemSavable(item) {

	private val expressionLibraryItem: BooleanExpressionLibraryItem get() = item as BooleanExpressionLibraryItem

	override val description: String = if (item.library is Project) {
		"${Translations.getString("project.savable.prefix")} \"${item.name.getTranslation()}\""
	} else {
		"${Translations.getString("library.savable.prefix")} \"${item.name.getTranslation()}\""
	}

	override val editable: Boolean
		get() = item.library?.let { Authorizer.isCurrentUserAuthorizedTo(Operation.Change, it) } ?: false

	override fun open(application: Application): Boolean {
		eventBus.post(OpenBooleanExpressionItemRequest(expressionLibraryItem))
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
