package ch.scorpion.antares.view

import ch.scorpion.jabbah.draw.style.Theme
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.style.EdgeStyle
import ch.scorpion.jabbah.graph.view.style.GraphTheme

/**
 * Defines [GraphTheme]s for the Antares application.
 */
object AntaresThemes {

    private val SELECTION_COLOR = Color.ORANGE
    private val SKY_BLUE = CompositeColor(foregroundColor = Color(69, 113, 180), backgroundColor = Color(220, 237, 250))

    private val FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (2.5 * Look.SCALE).toInt())
    private val ANNOTATION_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (1.4 * Look.SCALE).toInt())
    private val EXPLANATION_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, 12)

    private val ANNOTATION_STROKE = Stroke(1.0f)
    private val HIGHLIGHT = CompositeColor(foregroundColor = Color.YELLOW, backgroundColor = Color.YELLOW)
    private val HIGHLIGHT_STROKE = Stroke(10.0f)
    private val BOX_STROKE = Stroke(1.5f, LineCap.ROUND, LineJoin.ROUND)
    private val LINE_STROKE = Stroke(1.2f)
    private val BUS_STROKE = Stroke(3.0f, LineCap.BUTT, LineJoin.ROUND)
    private val EXPLANATION = CompositeColor(
            foregroundColor = Color(252, 205, 90),
            backgroundColor = Color(255, 255, 223),
            textColor = Color.BLACK)
    private val SUBSYSTEM_STROKE = Stroke(1.0f, LineCap.BUTT, LineJoin.MITER, 5.0f, floatArrayOf(5.0f), 0.0f)

    private val ZERO = CompositeColor(foregroundColor = Color(0, 115, 15), backgroundColor = Color.BLACK, textColor = Color.WHITE)
    private val ONE = CompositeColor(foregroundColor = Color(0, 255, 0), backgroundColor = Color(0, 115, 15), textColor = Color.BLACK)
    private val UNDEFINED = CompositeColor(foregroundColor = Color(40, 125, 249), backgroundColor = Color.BLACK, textColor = Color.WHITE)
    private val ERROR = CompositeColor(foregroundColor = Color.RED, backgroundColor = Color(255, 214, 214), textColor = Color.BLACK)

    /** This should be taken from the current focus color of the System/Target.*/
    private val FOCUS_COLOR = Color(48, 131, 251)
    private val FOCUS_STROKE = Stroke(1.0f, LineCap.BUTT, LineJoin.MITER, 1.0f, floatArrayOf(2.0f, 1.0f), 0.0f)


    fun install() {
        Themes.register(winter(), crt())
    }

    private fun winter(): Theme {
        return AntaresTheme(
                name = "Winter",
                background = BasicStyle(
                        color = CompositeColor(
                                foregroundColor = Color.GRAY,
                                backgroundColor = Color.WHITE,
                                textColor = Color.BLACK),
                        font = FONT,
                        stroke = ANNOTATION_STROKE),
                figure = BasicStyle(
                        color = SKY_BLUE,
                        stroke = BOX_STROKE,
                        font = FONT),
                highlight = BasicStyle(
                        color = HIGHLIGHT,
                        font = FONT,
                        stroke = HIGHLIGHT_STROKE),
                message = BasicStyle(
                        color = ERROR,
                        font = EXPLANATION_FONT,
                        stroke = ANNOTATION_STROKE),
                vertice = BasicStyle(
                        color = SKY_BLUE,
                        stroke = BOX_STROKE,
                        font = FONT),
                edge = EdgeStyle(
                        color = CompositeColor(
                                foregroundColor = Color.BLACK,
                                backgroundColor = Color(232, 232, 232),
                                textColor = Color.BLACK),
                        stroke = LINE_STROKE,
                        busStroke = BUS_STROKE,
                        font = ANNOTATION_FONT),
                annotation = BasicStyle(
                        color = CompositeColor(
                                foregroundColor = SKY_BLUE.foregroundColor,
                                backgroundColor = SKY_BLUE.backgroundColor,
                                textColor = SKY_BLUE.foregroundColor),
                        stroke = ANNOTATION_STROKE,
                        font = ANNOTATION_FONT),
                explanation = BasicStyle(
                        color = EXPLANATION,
                        stroke = ANNOTATION_STROKE,
                        font = EXPLANATION_FONT),
                subsystem = BasicStyle(
                        color = CompositeColor(
                                foregroundColor = Color.GRAY,
                                backgroundColor = Color(224, 224, 128)),
                        font = FONT,
                        stroke = SUBSYSTEM_STROKE),
                selection = CompositeColor(
                        foregroundColor = SELECTION_COLOR,
                        textColor = SELECTION_COLOR,
                        backgroundColor = Color.WHITE),
                zero = ZERO,
                one = ONE,
                undefined = UNDEFINED,
                wordZero = CompositeColor(
                        foregroundColor = Color.BLACK,
                        backgroundColor = Color(232, 232, 232),
                        textColor = Color.WHITE),
                word = CompositeColor(
                        foregroundColor = Color.GRAY,
                        backgroundColor = Color(232, 232, 232),
                        textColor = Color.WHITE),
                error = ERROR,
                focus = BasicStyle(
                        color = CompositeColor(
                                foregroundColor = FOCUS_COLOR
                        ),
                        stroke = FOCUS_STROKE
                ))
    }

    private fun crt(): Theme {
        val color = CompositeColor(
                foregroundColor = ZERO.foregroundColor,
                backgroundColor = Color.BLACK
        )
        val highlightColor = Color(129, 123, 22)
        return AntaresTheme(
                name = "CRT",
                referenceColors = listOf(
                        CompositeColor(Color(236, 35, 46), Color(120, 3, 7)),
                        CompositeColor(Color(72, 186, 233), Color(3, 16, 139)),
                        CompositeColor(Color(115, 191, 91), Color(7, 87, 9)),
                        CompositeColor(Color(245, 235, 62), Color(67, 69, 10))
                ),
                highlight = BasicStyle(
                        color = CompositeColor(
                                backgroundColor = highlightColor,
                                foregroundColor = highlightColor),
                        stroke = HIGHLIGHT_STROKE),
                background = BasicStyle(
                        color = CompositeColor(
                                foregroundColor = Color.GRAY.darker(),
                                backgroundColor = Color.BLACK,
                                textColor = color.foregroundColor),
                        stroke = ANNOTATION_STROKE,
                        font = ANNOTATION_FONT),
                figure = BasicStyle(
                        color = color,
                        stroke = BOX_STROKE,
                        font = FONT),
                vertice = BasicStyle(
                        color = color,
                        stroke = BOX_STROKE,
                        font = FONT),
                edge = EdgeStyle(
                        color = color,
                        stroke = LINE_STROKE,
                        busStroke = BUS_STROKE,
                        font = ANNOTATION_FONT),
                annotation = BasicStyle(
                        color = color,
                        stroke = ANNOTATION_STROKE,
                        font = ANNOTATION_FONT),
                explanation = BasicStyle(
                        color = EXPLANATION,
                        stroke = ANNOTATION_STROKE,
                        font = FONT),
                message = BasicStyle(
                        color = EXPLANATION,
                        stroke = ANNOTATION_STROKE,
                        font = EXPLANATION_FONT),
                subsystem = BasicStyle(
                        color = CompositeColor(
                                foregroundColor = Color(13, 116, 15),
                                backgroundColor = Color(5, 40, 7)),
                        stroke = SUBSYSTEM_STROKE),
                selection = CompositeColor(
                        foregroundColor = SELECTION_COLOR,
                        backgroundColor = Color.BLACK),
                zero = ZERO,
                one = ONE,
                undefined = UNDEFINED,
                wordZero = ZERO,
                word = ONE,
                error = ERROR,
                focus = BasicStyle(
                        color = CompositeColor(
                                foregroundColor = FOCUS_COLOR
                        ),
                        stroke = FOCUS_STROKE
                )
        )
    }
}