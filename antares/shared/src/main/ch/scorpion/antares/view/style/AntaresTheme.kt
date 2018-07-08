package ch.scorpion.antares.view.style

import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.graph.view.style.EdgeStyle
import ch.scorpion.jabbah.graph.view.style.GraphTheme

/**
 * Adds more [Theme] properties for the [ch.scorpion.antares.view] module.
 */
class AntaresTheme(
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
        vertice: Style = DEF_VERTICE,
        edge: EdgeStyle = DEF_EDGE,
        annotation: Style = DEF_ANNOTATION,
        explanation: Style = DEF_EXPLANATION,
        subsystem: Style = DEF_SUBSYSTEM,
        val zero: CompositeColor = DEF_ZERO,
        val one: CompositeColor = DEF_ONE,
        val undefined: CompositeColor = DEF_UNDEFINED,
        val error: CompositeColor = DEF_ERROR,
        val wordZero: CompositeColor = DEF_WORD_ZERO,
        val word: CompositeColor = DEF_WORD,
        val focus: Style = DEF_FOCUS

) : GraphTheme(
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
		messageError,
        vertice,
        edge,
        annotation,
        explanation,
        subsystem
){
    companion object {
        val DEF_ZERO = CompositeColor(foregroundColor = Color(0, 115, 15), backgroundColor = Color.BLACK, textColor = Color.WHITE)
        val DEF_ONE = CompositeColor(foregroundColor = Color(0, 255, 0), backgroundColor = Color(0, 115, 15), textColor = Color.BLACK)
        val DEF_UNDEFINED = CompositeColor(foregroundColor = Color(40, 125, 249), backgroundColor = Color.BLACK, textColor = Color.WHITE)
        val DEF_ERROR = CompositeColor(foregroundColor = Color.RED, backgroundColor = Color(255, 214, 214), textColor = Color.BLACK)
        val DEF_WORD_ZERO = CompositeColor(foregroundColor = Color.BLACK, backgroundColor = Color(232, 232, 232), textColor = Color.WHITE)
        val DEF_WORD = CompositeColor(foregroundColor = Color.GRAY, backgroundColor = Color(232, 232, 232), textColor = Color.WHITE)
        val DEF_FOCUS = BasicStyle(
                color = CompositeColor(foregroundColor = Color(48, 131, 251)),
                stroke = Stroke(1.0f, LineCap.BUTT, LineJoin.MITER, 1.0f, floatArrayOf(2.0f, 1.0f), 0.0f)
        )
    }
}