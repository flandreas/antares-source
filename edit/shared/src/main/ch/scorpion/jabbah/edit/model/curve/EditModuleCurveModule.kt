package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.model.curve] package.
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
}