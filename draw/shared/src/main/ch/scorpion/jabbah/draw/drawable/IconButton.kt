package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Icon
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

/** An implementation of a simple button as a [Drawable] that uses an [Icon] for rendering.*/
class IconButton(
        private val icon: Icon,
        location: Point2D = Point2D(),
        private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangle(location.x, location.y, icon.dim.width, icon.dim.height) {

    companion object {
        private val LOG by logger(IconButton::class)
        private val STROKE = Stroke(1.5f)
    }

    private val handler = Handler()

    private var isHovering = false

    /** ---- [RectangularDrawable] */

    override val lineWidth: Double get() = STROKE.width.toDouble()

    /** ---- [Drawable] interface */

    override fun contains(p: Point2D): Boolean {
        LOG.debug("IconButton.contains $p")
        return super.contains(p)
    }

    override fun draw(context: DrawContext) {
        if (isHovering) {
            // TODO Configurable
            context.g.color = Color.ORANGE
        } else {
            context.g.color = styleProvider.getStyle(StyleType.FIGURE).color.foregroundColor
        }
        context.g.stroke = STROKE
        icon.draw(context, Point2D(x, y))
    }

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return handler
    }

    /** ---- [IconButton] */

    private inner class Handler : InputEventHandlerAdapter<InputEventContext>() {
        override fun mouseMoved(context: InputEventContext): InputEventHandler<InputEventContext>? {
            if (contains(context.x, context.y)) {
                if (!isHovering) {
                    LOG.debug("IconButton: start hover mode")
                    isHovering = true
                    invalidate()
                    validate()
                }
                return this
            }

            if (isHovering) {
                LOG.debug("IconButton: stop hover mode")
                isHovering = false
                invalidate()
                validate()
            }
            return null
        }
    }
}