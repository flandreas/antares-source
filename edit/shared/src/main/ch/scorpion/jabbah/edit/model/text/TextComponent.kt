package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.geom.RoundRectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.TextRenderInfo
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.edit.style.EditStyleType
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * A [Component] with multipline text that can be interactively edited by the user.
 */
interface TextComponent : Component, RectangularShape {

    var text: String

    // Requested by the compiler due to multiple inheritance problem
    override fun contains(x: Double, y: Double): Boolean {
        throw UnsupportedOperationException("not implemented")
    }
}

interface TextComponentFactory {

    /**
     * Creates a new platform-specific [TextComponent] implementation.
     * @param text the text to be displayed
     * @param location the location of the text baseline point
     */
    fun create(
            text: String,
            location: Point2D,
            styleType: StyleType,
            styleProvider: StyleProvider
    ): TextComponent
}
