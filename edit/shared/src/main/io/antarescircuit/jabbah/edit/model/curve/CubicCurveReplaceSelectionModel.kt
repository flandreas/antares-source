package io.antarescircuit.jabbah.edit.model.curve

import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractHandleSelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractSelectedColorWrappingSelectionModel

/**
 * A [SelectionModel] that renders a [CubicCurveComponent] in the selection color and that forwards
 * input event handling to a [CubicCurveHandleSelectionModel].
 */
class CubicCurveReplaceSelectionModel(
    component: CubicCurveComponent
) : AbstractSelectedColorWrappingSelectionModel<CubicCurveComponent>(component) {

    override fun createInnerSelectionModel(component: CubicCurveComponent): AbstractHandleSelectionModel<CubicCurveComponent> =
        CubicCurveHandleSelectionModel(component)
}