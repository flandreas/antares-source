package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Beaks text into multiple lines according to a box with a fix width.
 *
 * Designed to be immutable. Can be aggregated into a [Drawable].
 * Used primarily for rendering multi-line text of the JavaScript platform, but could also
 * be useful on the JVM platform, and therefore contained in the 'shared' module.
 */
class MultilineText(
        text: String,
        private val font: Font,
        maxWidth: Int,
        private val lineHeight: Int = font.size,
        textRenderInfoFactory: TextRenderInfoFactory = DrawModule.textRenderInfoFactory
) {

    val height: Int get() = lines.size * lineHeight

    private val lines = mutableListOf<String>()

    private val ascent: Int

    init {
        var lAscent = 0

        for (car in text.split('\n')) {
            var line = ""
            for (word in car.split(' ')) {
                val testLine = line + word + " "
                val textRenderInfo = textRenderInfoFactory.invoke(testLine, font)
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
    }

    /**
     * Draws the individual lines of this [MultilineText] at the specified location which is
     * the upper-left corner of the surrounding box.
     * Uses the [Font] provided during construction. Make sure that other [Graphics2D] properties
     * such as [Color]s have been set prior to calling this function, if needed.
     */
    fun draw(context: DrawContext, x: Int, y: Int) {
        var yy = y + ascent
        for (line in lines) {
            context.g.drawString(line, x, y)
            yy += lineHeight
        }
    }
}