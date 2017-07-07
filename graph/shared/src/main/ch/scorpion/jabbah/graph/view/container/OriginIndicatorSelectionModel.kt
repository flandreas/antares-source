package ch.scorpion.jabbah.graph.view.container

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * A [SelectionModel] for [OriginIndicator].
 */
class OriginIndicatorSelectionModel(model: OriginIndicator) : AbstractSelectionModel<OriginIndicator>(model) {

    override val boundingBox: RectangularShape get() = component.boundingBox

    override fun draw(context: DrawContext) {
        context.g.color = DrawModule.properties.getColor(OriginIndicator.PROP_SELECTION_COLOR)
        component.drawSelected(context)
    }

    override fun componentUpdated() {
        validate()
    }

    override fun contains(x: Double, y: Double): Boolean {
        return component.contains(x, y)
    }
}