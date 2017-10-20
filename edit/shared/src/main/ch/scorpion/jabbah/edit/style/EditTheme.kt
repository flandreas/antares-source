package ch.scorpion.jabbah.edit.style

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.select.RubberBand

/**
 * Adds more [Theme] properties for the [ch.scorpion.jabbah.edit] module.
 */
open class EditTheme(
        name: String = DEF_NAME,
        supportsWhiteBackground: Boolean = DEF_SUPPORTS_WHITE_BACKGROUND,
        referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider,
        referenceColors: List<CompositeColor> = DEF_REF_COLORS,
        background: Style = DEF_BACKGROUND,
        figure: Style = DEF_FIGURE,
        val selection: CompositeColor = DEF_SELECTION,
        val highlight: Style = DEF_HIGHLIGHT,
        val message: Style = DEF_MESSAGE
) : DrawTheme(
        name,
        supportsWhiteBackground,
        referenceColorSequenceProvider,
        referenceColors,
        background,
        figure
) {

    companion object {
        val DEF_SELECTION = CompositeColor(Color.ORANGE, Color.WHITE, Color.ORANGE)
        val DEF_HIGHLIGHT = BasicStyle(CompositeColor(Color.YELLOW, Color.YELLOW, Color.BLACK))
        val DEF_MESSAGE = BasicStyle(CompositeColor(foregroundColor = Color(252, 205, 90), backgroundColor = Color(255, 255, 223)))
    }

    override fun activateIn(styleRepository: StyleRepository, styleOnly: Boolean) {
        super.activateIn(styleRepository, styleOnly)
        styleRepository.registerStyle(EditStyleType.HIGHLIGHT, highlight)
        styleRepository.registerStyle(EditStyleType.MESSAGE, message)

        if (!styleOnly) {
            BaseModule.properties.set(RubberBand.PROP_FILL_PAINT, selection.foregroundColor.withAlpha(32))
            BaseModule.properties.set(RubberBand.PROP_STROKE_PAINT, selection.foregroundColor)
        }
    }
}