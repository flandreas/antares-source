package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * A [ArrowBubble] is a [Drawable] that draws a [RectangularDrawable] inside a [Path] consisting of
 * a rounded rectangle and an arrow tip pointing upwards.
 *
 * @property content the content to be drawn inside the [Path]
 * @property location the [Point2D] at the tip of the arrow path
 */
class ArrowBubble(
        private val content: RectangularDrawable,
        private val location: Point2D,
        styleType: StyleType,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractStyledDrawable(styleType, styleProvider) {

    companion object {

        /** The inset between the content and the border path.*/
        private val INSET = 8.0

        /** The size of the rounded corner arcs.*/
        private val ARC_SIZE = 5.0

        /** The base width of the arrow tip.*/
        private val TIP_WIDTH = 15.0

        /** The height of the arrow tip.*/
        private val TIP_HEIGHT = 15.0

        /** The width of the area from the path's left edge to the arrow's tip.*/
        private val LEFT_WIDTH = TIP_WIDTH / 2 + 20

    }

    /** The border path with the arrow tip pointing upwards. Expressed in relative coordinates with origin (0,0) at the arrow tip. */
    private val path: Path = createPath()

    /** The overall width of the path.*/
    private val width: Double get() = content.width + 2 * INSET

    private val height: Double get() = content.height + 2 * INSET + TIP_HEIGHT

    /** The width of the area from the path's right edge to the arrow's tip.*/
    private val rightWidth: Double get() = width - LEFT_WIDTH

    /** The upper-left corner of [content] in the local, relative coordinate system. */
    private val contentLocation: Point2D get() = Point2D(-LEFT_WIDTH + INSET, TIP_HEIGHT + INSET)

    init {
        content.setBounds(contentLocation.x, contentLocation.y, content.width, content.height)
    }

    /** ----  [AbstractDrawable] */
    
    override val boundingBox: RectangularShape get() = path.boundingBox.expandBy(style.stroke.width.toDouble())

    override fun draw(context: DrawContext) {
        context.g.translate(location.x, location.y)

        context.g.color = style.color.backgroundColor
        context.g.fill(path)
        context.g.color = style.color.foregroundColor
        context.g.stroke = style.stroke
        context.g.draw(path)

        context.g.color = style.color.textColor
        context.g.font = style.font
        content.draw(context)

        context.g.translate(-location.x, -location.y)
    }

    override fun contains(x: Double, y: Double): Boolean = path.contains(x - location.x, y - location.y)

    /** ---- [ArrowBubble] */

    private fun createPath(): Path {
        return System.get().createPath()
                .moveTo(0, 0)
                .lineTo(TIP_WIDTH / 2, TIP_HEIGHT)
                .lineTo(rightWidth - ARC_SIZE, TIP_HEIGHT)
                .quadTo(rightWidth, TIP_HEIGHT, rightWidth, TIP_HEIGHT + ARC_SIZE)
                .lineTo(rightWidth, height - ARC_SIZE)
                .quadTo(rightWidth, height, rightWidth - ARC_SIZE, height)
                .lineTo(-LEFT_WIDTH + ARC_SIZE, height)
                .quadTo(-LEFT_WIDTH, height, -LEFT_WIDTH, height - ARC_SIZE)
                .lineTo(-LEFT_WIDTH, TIP_HEIGHT + ARC_SIZE)
                .quadTo(-LEFT_WIDTH, TIP_HEIGHT, -LEFT_WIDTH + ARC_SIZE, TIP_HEIGHT)
                .lineTo(-TIP_WIDTH / 2, TIP_HEIGHT)
                .close()
    }
}