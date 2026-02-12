package ch.scorpion.jabbah.edit.model.group

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.select.EditSelectModule

/** Module definitions for the [ch.scorpion.jabbah.edit.model.group] package.*/
object EditModelGroupModule : AbstractModule() {

	override fun initialize() {
		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.REPLACE,
			GroupComponent::class
		) { GroupComponentSelectionModel(it as GroupComponent) }
	}

	override fun resetDependencies() {}
}