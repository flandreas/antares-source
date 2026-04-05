package io.antarescircuit.jabbah.edit.model.polyline

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [io.antarescircuit.jabbah.edit.model.polyline] package.
 */
object EditModelPolylineModule : AbstractModule() {

	override fun initialize() {
		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.ABOVE,
			PolylineComponent::class
		) { PolylineReplaceSelectionModel(it as PolylineComponent) }
	}

	override fun resetDependencies() {}
}