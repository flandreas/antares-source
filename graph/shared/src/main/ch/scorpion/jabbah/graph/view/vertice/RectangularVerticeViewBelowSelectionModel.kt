package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.AbstractBelowSelectionModel
import ch.scorpion.jabbah.edit.style.EditStyleType

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