package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.style.EditStyleType
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.drawable.Colorable
import ch.scorpion.jabbah.draw.graphics.CompositeColor


/**
 * A [SelectionModel] for [SelectionDrawingStrategy.BELOW] that draws a rounded rectangle that is slightly
 * larger than the [Component]'s bounding box.
 */
class BoundingBoxBelowSelectionModel(
    component: Component,
    private val styleProvider: StyleProvider
) : AbstractSelectionModel<Component>(component), Colorable {

    @Suppress("unused")
    constructor(component: Component): this(component, DrawStyleModule.styleProvider)

    companion object {
        /** The number of pixels to add to the [Component]'s bounding box at each side. */
        private val OUTSET = 10

        /** The arc size of the rounded rectangle.*/
        private val ARC_SIZE = 15
    }

    private var bounds = Rectangle2D()

    /** ---- [Colorable] interface */

    override var color: CompositeColor = styleProvider.getStyle(EditStyleType.HIGHLIGHT).color
        set(value) {
            field = value
            invalidate()
        }

    /** ---- [Drawable] interface */

    override val boundingBox: RectangularShape = bounds

    override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

    override fun draw(context: DrawContext) {
        val oldColor = context.g.color
        context.g.color = color.backgroundColor

        context.g.fillRoundRect(
                bounds.x.toInt(),
                bounds.y.toInt(),
                bounds.width.toInt(),
                bounds.height.toInt(),
                ARC_SIZE, ARC_SIZE)

        context.g.color = oldColor
    }

    /** ---- [AbstractSelectionModel] */

    override fun componentUpdated() {
        invalidate()
        val bbox = component.boundingBox
        bounds = Rectangle2D(
                bbox.x - OUTSET,
                bbox.y - OUTSET,
                bbox.width + 2 * OUTSET,
                bbox.height + 2 * OUTSET)
        invalidate()
        validate()
    }
}