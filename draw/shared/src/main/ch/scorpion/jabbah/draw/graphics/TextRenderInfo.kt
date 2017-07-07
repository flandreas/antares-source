package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.Rectangle2D

/**
 * Represents platform-specific geometrical information about how a text is rendered.
 */
data class TextRenderInfo(val textBounds: Rectangle2D, val ascent: Double)