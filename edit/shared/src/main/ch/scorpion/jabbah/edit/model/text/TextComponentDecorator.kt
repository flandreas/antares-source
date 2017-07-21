package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/** Used to decorate a [TextComponent] for drawing individual look. */
interface TextComponentDecorator {

    var backgroundColor: Color?
    var foregroundColor: Color?
    var stroke: Stroke

    fun drawBackground(component: TextComponent, context: DrawContext)

    fun drawForeground(component: TextComponent, context: DrawContext)
}

/** A [TextComponentDecorator] that decorates a [TextComponent] with a [RectangularShape]. */
class RectangularShapeTextComponentDecorator(
        private val shape: RectangularShape,
        override var backgroundColor: Color? = null,
        override var foregroundColor: Color? = null,
        override var stroke: Stroke = Stroke(),
        private val transparent: TransparentImpl
) : TextComponentDecorator {

    /** ---- [TextComponentDecorator] interface */

    override fun drawBackground(component: TextComponent, context: DrawContext) {
        if (backgroundColor != null) {
            adjustShape(component)
            context.g.color = transparent.applyTo(backgroundColor!!)
            context.g.fill(shape)
        }
    }

    override fun drawForeground(component: TextComponent, context: DrawContext) {
        if (foregroundColor != null) {
            adjustShape(component)
            context.g.color = transparent.applyTo(foregroundColor!!)
            context.g.stroke = stroke
            context.g.draw(shape)
        }
    }

    /** ---- [RectangularShapeTextComponentDecorator] */

    private fun adjustShape(c: TextComponent) {
        shape.setFrame(c.x, c.y, c.width, c.height)
    }
}