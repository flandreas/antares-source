package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractSelectionModel
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.module.DrawModule

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