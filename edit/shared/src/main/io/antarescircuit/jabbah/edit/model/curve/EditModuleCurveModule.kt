package io.antarescircuit.jabbah.edit.model.curve

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [io.antarescircuit.jabbah.edit.model.curve] package.
 */
object EditModuleCurveModule : AbstractModule() {

	override fun initialize() {
		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.ABOVE,
			QuadCurveComponent::class
		) { QuadCurveReplaceSelectionModel(it as QuadCurveComponent) }

		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.ABOVE,
			CubicCurveComponent::class
		) { CubicCurveReplaceSelectionModel(it as CubicCurveComponent) }
	}

	override fun resetDependencies() {}
}