package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Icon
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * An implementation of a simple button as a [Drawable] that uses an [Icon] for rendering.
 */
class IconButton(
        private val icon: Icon,
        private val action: () -> Unit,
        location: Point2D = Point2D(),
        var tooltipKey: String? = null,
        private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangle(location.x, location.y, icon.dim.width, icon.dim.height) {

    companion object {
        private val LOG by logger(IconButton::class)
        private val STROKE = Stroke(1.5f)
    }

    var enabled: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                invalidate()
                validate()
            }
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
        } else if (!enabled) {
            context.g.color = styleProvider.getStyle(StyleType.FIGURE).color.foregroundColor.withAlpha(128)
        } else {
            context.g.color = styleProvider.getStyle(StyleType.FIGURE).color.foregroundColor
        }
        context.g.stroke = STROKE
        icon.draw(context, Point2D(x, y))
    }

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return handler
    }

    override fun update() {
        isHovering = false
        super.update()
    }

    override fun getToolTipText(x: Double, y: Double, width: Int?): String? {
        if (tooltipKey != null) {
            return Translations.getString(tooltipKey!!)
        }
        return null
    }

    /** ---- [IconButton] */

    private inner class Handler : InputEventHandlerAdapter<InputEventContext>() {
        override fun mouseMoved(context: InputEventContext): InputEventHandler<InputEventContext>? {
            if (contains(context.x, context.y)) {
                if (!isHovering && enabled) {
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

        override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
            if (enabled) {
                action.invoke()
            }
            return null
        }
    }
}