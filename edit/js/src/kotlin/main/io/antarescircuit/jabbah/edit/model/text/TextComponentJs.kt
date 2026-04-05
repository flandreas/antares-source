package io.antarescircuit.jabbah.edit.model.text

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType

class TextComponentFactoryJs : TextComponentFactory {

    override fun create(text: TranslatableText, location: Point2D, styleType: StyleType, styleProvider: StyleProvider): TextComponent {
        return SimpleTextComponent(text, location, styleType, styleProvider)
    }
}
