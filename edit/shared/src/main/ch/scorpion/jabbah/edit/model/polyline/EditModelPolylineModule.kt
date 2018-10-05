package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.model.polyline] package.
 */
object EditModelPolylineModule : AbstractModule() {

	override fun initialize() {
		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.ABOVE,
			PolylineComponent::class.simpleName!!
		) { PolylineReplaceSelectionModel(it as PolylineComponent) }
	}
}