package ch.scorpion.jabbah.edit.style

import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.Style

/**
 * Adds more [Theme] properties for the [ch.scorpion.jabbah.edit] module.
 */
open class EditTheme(
    name: String = DEF_NAME,
    background: Style = DEF_BACKGROUND,
    val selection: CompositeColor = DEF_SELECTION
) : DrawTheme(name, background) {

    companion object {
        val DEF_SELECTION = CompositeColor(Color.ORANGE, Color.WHITE, Color.ORANGE)
    }
}