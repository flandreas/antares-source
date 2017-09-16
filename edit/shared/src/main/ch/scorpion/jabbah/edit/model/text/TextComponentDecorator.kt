package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.style.Stylable

/** Used to decorate a [TextComponent] for drawing individual look. */
interface TextComponentDecorator {

    fun drawBackground(component: TextComponent, context: DrawContext)

    fun drawForeground(component: TextComponent, context: DrawContext)
}

/** A [TextComponentDecorator] that decorates a [TextComponent] with a [RectangularShape]. */
class RectangularShapeTextComponentDecorator(
        private val shape: RectangularShape,
        private val stylable: Stylable,
        private val transparent: TransparentImpl
) : TextComponentDecorator {

    /** ---- [TextComponentDecorator] interface */

    override fun drawBackground(component: TextComponent, context: DrawContext) {
        if (stylable.filled) {
            adjustShape(component)
            context.g.color = transparent.applyTo(stylable.backgroundColor)
            context.g.fill(shape)
        }
    }

    override fun drawForeground(component: TextComponent, context: DrawContext) {
        adjustShape(component)
        context.g.color = transparent.applyTo(stylable.foregroundColor)
        context.g.stroke = stylable.stroke
        context.g.draw(shape)
    }

    /** ---- [RectangularShapeTextComponentDecorator] */

    private fun adjustShape(c: TextComponent) {
        shape.setFrame(c.x, c.y, c.width, c.height)
    }
}