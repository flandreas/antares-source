package io.antarescircuit.jabbah.edit.model.group

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.select.EditSelectModule

/** Module definitions for the [io.antarescircuit.jabbah.edit.model.group] package.*/
object EditModelGroupModule : AbstractModule() {

	override fun initialize() {
		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.REPLACE,
			GroupComponent::class
		) { GroupComponentSelectionModel(it as GroupComponent) }
	}

	override fun resetDependencies() {}
}