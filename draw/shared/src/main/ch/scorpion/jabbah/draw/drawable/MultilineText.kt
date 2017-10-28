package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Breaks text into multiple lines according to a box with a fix width.
 *
 * Designed to be immutable. Can be aggregated into a [Drawable].
 * Used primarily for rendering multi-line text of the JavaScript platform, but could also
 * be useful on the JVM platform, and therefore contained in the 'shared' module.
 *
 * @property location the upper-left corner of the surrounding box relative to the owner's coordinate system
 */
class MultilineText(
        private val text: String,
        private val font: Font,
        private val maxWidth: Double,
        override var location: Point2D = Point2D(),
        textRenderInfoFactory: TextRenderInfoFactory = DrawModule.textRenderInfoFactory,
        private val asHtml: Boolean = false
) : AbstractRectangle(), Locatable {

    companion object {
        private val DEBUG_GFX = false
        val LINE_DIST = 5.0
    }

    private val lines = mutableListOf<String>()

    private val ascent: Int

    private val lineHeight: Double = font.size + LINE_DIST

    init {
        var lAscent = 0

        for (car in text.split('\n')) {
            var line = ""
            for (word in car.split(' ')) {
                val testLine = line + word + " "
                val textRenderInfo = textRenderInfoFactory.measureSingleLineText(testLine, font)
                val testWidth = textRenderInfo.textBounds.width
                lAscent = textRenderInfo.ascent.toInt()

                if (testWidth > maxWidth) {
                    lines.add(line)
                    line = word + " "
                } else {
                    line = testLine
                }
            }

            lines.add(line)
        }

        ascent = lAscent

        setBounds(location.x, location.y, maxWidth, lines.size * lineHeight)
    }

    /** ---- [RectangularDrawable] */

    override val lineWidth: Double get() = 0.0

    /** ----- [Drawable] interface */

    override fun draw(context: DrawContext) {
        if (asHtml) {
            context.g.drawText(text, xInt, yInt, widthInt)
        } else {
            var yy = location.y + ascent
            for (line in lines) {
                context.g.drawString(line, location.x.toInt(), yy.toInt())
                yy += lineHeight
            }
        }

        if (DEBUG_GFX) {
            val oldColor = context.g.color
            context.g.color = Color.GRAY
            context.g.drawRect(location.x, location.y, maxWidth, height)
            context.g.fillOval(location.x.toInt() - 3, location.y.toInt() - 3 + ascent, 6, 6)
            context.g.color = oldColor
        }
    }

    override fun contains(x: Double, y: Double): Boolean {
        return boundingBox.contains(x, y)
    }
}