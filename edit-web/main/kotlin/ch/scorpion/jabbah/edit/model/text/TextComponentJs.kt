package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent

class TextComponentFactoryJs : TextComponentFactory {

    override fun create(text: String, location: Point2D, styleType: StyleType, styleProvider: StyleProvider): TextComponent {
        return TextComponentJs(text, location, styleType, styleProvider)
    }
}

/**
 * Implements [TextComponent] for the JavaScript target.
 * Does not yet support inline editing.
 * TODO Implement displaying text at least as static Label
 */
class TextComponentJs(
        text: String,
        location: Point2D,
        styleType: StyleType,
        styleProvider: StyleProvider
) : RectangularComponent(styleType = styleType, styleProvider = styleProvider, bounds = Rectangle2D(location.x, location.y, 0.0, 0.0)),
    Transparent, TextComponent {

    /** ---- [TextComponent] interface */

    override var text: String = ""

    override fun contains(x: Double, y: Double): Boolean {
        return super<RectangularComponent>.contains(x, y)
    }

    /** ---- [Transparent] interface */

    private val transparent = TransparentImpl(this)

    override var transparency: Int
        get() = transparent.transparency
        set(value) {
            transparent.transparency = value
        }
}