package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractBelowSelectionModel
import io.antarescircuit.jabbah.edit.style.EditStyleType

/**
 * A [SelectionModel] for [SelectionDrawingStrategy.BELOW] that draws a rounded rectangle
 * below the rectangular bounds of the [Component]
 */
class RectangularVerticeViewBelowSelectionModel(
    component: AbstractRectangularVerticeView<*>,
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    styleType: StyleType = EditStyleType.SELECTION,
    outset: Int = DEF_OUTSET
) : AbstractBelowSelectionModel<AbstractRectangularVerticeView<*>>(component, styleProvider, styleType, outset) {

    private var bounds = Rectangle2D()

    override val boundingBox: RectangularShape get() = bounds

    override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

    override fun draw(context: DrawContext) {
        val oldColor = context.g.color
        context.g.color = color.foregroundColor

        context.g.fillRoundRect(
            bounds.x.toInt(),
            bounds.y.toInt(),
            bounds.width.toInt(),
            bounds.height.toInt(),
            ARC_SIZE, ARC_SIZE)

        context.g.color = oldColor
    }

    override fun componentUpdated() {
        invalidate()
        val bbox = component.rotation.rotateRectangleAround(
            Point2D.ZERO,
            component.bounds
        ).moveBy(component.location)

        //val bbox = component.bounds
        bounds = Rectangle2D(
            bbox.x - outset,
            bbox.y - outset,
            bbox.width + 2 * outset,
            bbox.height + 2 * outset)
        invalidate()
        validate()
    }
}