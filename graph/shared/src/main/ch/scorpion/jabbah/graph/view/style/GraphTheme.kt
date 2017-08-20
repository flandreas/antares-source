package ch.scorpion.jabbah.graph.view.style

import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.style.EditTheme

/**
 * Adds more [Theme] properties for the [ch.scorpion.jabbah.graph] module.
 */
open class GraphTheme(
        name: String = DEF_NAME,
        styleRepository: StyleRepository = DrawStyleModule.styleProvider,
        background: Style = DEF_BACKGROUND,
        figure: Style = DEF_FIGURE,
        selection: CompositeColor = DEF_SELECTION,
        highlight: Style = DEF_HIGHLIGHT,
        message: Style = DEF_MESSAGE,
        val vertice: Style = DEF_VERTICE,
        val edge: EdgeStyle = DEF_EDGE,
        val annotation: Style = DEF_ANNOTATION,
        val explanation: Style = DEF_EXPLANATION,
        val subsystem: Style = DEF_SUBSYSTEM
) : EditTheme(name, styleRepository, background, figure, selection) {

    companion object {
        val DEF_VERTICE = BasicStyle()
        val DEF_EDGE = EdgeStyle()
        val DEF_ANNOTATION = BasicStyle()
        val DEF_EXPLANATION = BasicStyle()
        val DEF_SUBSYSTEM = BasicStyle()
    }

    override fun activate() {
        super.activate()
        styleRepository.registerStyle(GraphStyleType.VERTICE, vertice)
        styleRepository.registerStyle(GraphStyleType.EDGE, edge)
        styleRepository.registerStyle(GraphStyleType.ANNOTATION, annotation)
        styleRepository.registerStyle(GraphStyleType.EXPLANATION, explanation)
        styleRepository.registerStyle(GraphStyleType.SUBSYSTEM, subsystem)
    }
}