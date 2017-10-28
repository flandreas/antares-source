package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.Rectangle2D

interface TextRenderInfoFactory {

    /** Measures rendered single-line, non-HTML text.*/
    fun measureSingleLineText(text: String, font: Font): TextRenderInfo

    /**
     * Measures rendered multi-line, HTML text when being rendered in the specified with.
     * This is especially useful for determining the height of a text block of a fixed width
     * when it is rendered using [Graphics2D.drawText].
     */
    fun measureHtmlText(text: String, font: Font, width: Int): TextRenderInfo
}

/**
 * Represents platform-specific geometrical information about how a text is rendered.
 */
data class TextRenderInfo(val textBounds: Rectangle2D, val ascent: Double)