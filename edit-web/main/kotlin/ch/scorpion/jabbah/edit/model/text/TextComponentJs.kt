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
        return SimpleTextComponent(text, location, styleType, styleProvider)
    }
}
