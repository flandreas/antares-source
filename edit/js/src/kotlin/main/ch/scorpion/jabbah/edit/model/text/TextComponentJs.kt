package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

class TextComponentFactoryJs : TextComponentFactory {

    override fun create(text: TranslatableText, location: Point2D, styleType: StyleType, styleProvider: StyleProvider): TextComponent {
        return SimpleTextComponent(text, location, styleType, styleProvider)
    }
}
