package io.antarescircuit.jabbah.edit.model.polyline

import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractHandleSelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractSelectedColorWrappingSelectionModel

/**
 * A [SelectionModel] that renders a [PolylineComponent] in the selection color and that forwards
 * input event handling to a [PolylineHandleSelectionModel].
 */
class PolylineReplaceSelectionModel(component: PolylineComponent) : AbstractSelectedColorWrappingSelectionModel<PolylineComponent>(component) {

	/** ---- [AbstractSelectedColorWrappingSelectionModel] */

	override fun createInnerSelectionModel(component: PolylineComponent): AbstractHandleSelectionModel<PolylineComponent> {
		return PolylineHandleSelectionModel(component)
	}
}