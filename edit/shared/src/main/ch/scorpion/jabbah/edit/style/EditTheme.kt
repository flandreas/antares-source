package ch.scorpion.jabbah.edit.style

import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.*

/**
 * Adds more [Theme] properties for the [ch.scorpion.jabbah.edit] module.
 */
open class EditTheme(
        name: String = DEF_NAME,
        styleRepository: StyleRepository = DrawStyleModule.styleProvider,
        background: Style = DEF_BACKGROUND,
        figure: Style = DEF_FIGURE,
        val selection: CompositeColor = DEF_SELECTION,
        val highlight: Style = DEF_HIGHLIGHT,
        val message: Style = DEF_MESSAGE
) : DrawTheme(name, styleRepository, background, figure) {

    companion object {
        val DEF_SELECTION = CompositeColor(Color.ORANGE, Color.WHITE, Color.ORANGE)
        val DEF_HIGHLIGHT = BasicStyle(CompositeColor(Color.YELLOW, Color.YELLOW, Color.BLACK))
        val DEF_MESSAGE = BasicStyle(CompositeColor(foregroundColor = Color(252, 205, 90), backgroundColor = Color(255, 255, 223)))
    }

    override fun activate() {
        super.activate()
        styleRepository.registerStyle(EditStyleType.HIGHLIGHT, highlight)
        styleRepository.registerStyle(EditStyleType.MESSAGE, message)
    }
}