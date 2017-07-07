package ch.scorpion.jabbah.graph.view.style

import ch.scorpion.jabbah.draw.style.Theme
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.draw.style.Style
import ch.scorpion.jabbah.edit.style.EditTheme

/**
 * Adds more [Theme] properties for the [ch.scorpion.jabbah.graph] module.
 */
open class GraphTheme(
    name: String = DEF_NAME,
    background: Style = DEF_BACKGROUND,
    selection: CompositeColor = DEF_SELECTION,
    val vertice: Style = DEF_VERTICE,
    val edge: EdgeStyle = DEF_EDGE,
    val annotation: Style = DEF_ANNOTATION,
    val explanation: Style = DEF_EXPLANATION,
    val highlight: Style = DEF_HIGHTLIGHT,
    val message: Style = DEF_MESSAGE,
    val subsystem: Style = DEF_SUBSYSTEM
) : EditTheme(name, background, selection) {

    companion object {
        val DEF_VERTICE = BasicStyle()
        val DEF_EDGE = EdgeStyle()
        val DEF_ANNOTATION = BasicStyle()
        val DEF_EXPLANATION = BasicStyle()
        val DEF_HIGHTLIGHT = BasicStyle()
        val DEF_MESSAGE = BasicStyle()
        val DEF_SUBSYSTEM = BasicStyle()
    }
}