package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.AbstractHandleSelectionModel
import ch.scorpion.jabbah.edit.select.AbstractSelectedColorWrappingSelectionModel

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