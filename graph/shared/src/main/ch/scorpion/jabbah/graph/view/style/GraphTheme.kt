package ch.scorpion.jabbah.graph.view.style

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.ReferenceColorSequenceProvider
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.select.Handle
import ch.scorpion.jabbah.edit.style.EditTheme

/**
 * Adds more [Theme] properties for the [ch.scorpion.jabbah.graph] module.
 */
open class GraphTheme(
        name: String = DEF_NAME,
        supportsWhiteBackground: Boolean = DEF_SUPPORTS_WHITE_BACKGROUND,
        referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider,
        referenceColors: List<CompositeColor> = DEF_REF_COLORS,
        background: Style = DEF_BACKGROUND,
        figure: Style = DEF_FIGURE,
        tooltip: Style = DEF_TOOLTIP,
        selection: CompositeColor = DEF_SELECTION,
        highlight: Style = DEF_HIGHLIGHT,
        messageInfo: Style = DEF_MESSAGE_INFO,
        messageError: Style = DEF_MESSAGE_ERROR,
        val vertice: Style = DEF_VERTICE,
        val edge: EdgeStyle = DEF_EDGE,
        val annotation: Style = DEF_ANNOTATION,
        val explanation: Style = DEF_EXPLANATION,
        val subsystem: Style = DEF_SUBSYSTEM,
        val error: CompositeColor = DEF_ERROR
) : EditTheme(
        name,
        supportsWhiteBackground,
        referenceColorSequenceProvider,
        referenceColors,
        background,
        figure,
        tooltip,
        selection,
        highlight,
        messageInfo,
		messageError
) {

    companion object {
        val DEF_VERTICE = BasicStyle()
        val DEF_EDGE = EdgeStyle()
        val DEF_ANNOTATION = BasicStyle()
        val DEF_EXPLANATION = BasicStyle()
        val DEF_SUBSYSTEM = BasicStyle()
	    val DEF_ERROR = CompositeColor(foregroundColor = Color.RED, backgroundColor = Color(255, 214, 214), textColor = Color.BLACK)
    }

    override fun activateIn(styleRepository: StyleRepository, styleOnly: Boolean) {
        super.activateIn(styleRepository, styleOnly)
        styleRepository.registerStyle(GraphStyleType.VERTICE, vertice)
        styleRepository.registerStyle(GraphStyleType.EDGE, edge)
        styleRepository.registerStyle(GraphStyleType.ANNOTATION, annotation)
        styleRepository.registerStyle(GraphStyleType.EXPLANATION, explanation)
        styleRepository.registerStyle(GraphStyleType.SUBSYSTEM, subsystem)

        if (!styleOnly) {
            BaseModule.properties.set(Handle.PROP_BORDER_COLOR, selection.foregroundColor)
            BaseModule.properties.set(Handle.PROP_FILL_COLOR, selection.backgroundColor)
        }
    }
}