package io.antarescircuit.jabbah.edit.model.curve

import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractHandleSelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractSelectedColorWrappingSelectionModel

/**
 * A [SelectionModel] that renders a [QuadCurveComponent] in the selection color and that forwards
 * input event handling to a [QuadCurveHandleSelectionModel].
 */
class QuadCurveReplaceSelectionModel(
	component: QuadCurveComponent
) : AbstractSelectedColorWrappingSelectionModel<QuadCurveComponent>(component) {

	override fun createInnerSelectionModel(component: QuadCurveComponent): AbstractHandleSelectionModel<QuadCurveComponent> =
		QuadCurveHandleSelectionModel(component)
}