package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.model.curve] package.
 */
object EditModuleQuadCurveModule : AbstractModule() {

	override fun initialize() {
		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.ABOVE,
			QuadCurveComponent::class.simpleName!!,
			{ QuadCurveHandleSelectionModel(it as QuadCurveComponent) })
	}
}