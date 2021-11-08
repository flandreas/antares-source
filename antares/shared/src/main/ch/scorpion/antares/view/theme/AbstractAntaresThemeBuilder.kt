package ch.scorpion.antares.view.theme

import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.draw.style.Theme
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.*

/**
 * Defines [Theme] property values suggested to be used by all [AntaresTheme]s.
 */
abstract class AbstractAntaresThemeBuilder(protected val name: String) {

	companion object {

		val FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, 15)

		val SELECTION_FOREGROUND_COLOR = Color.ORANGE

		val ANNOTATION_STROKE = Stroke(1.0f)

		val ANNOTATION_FONT = Look.ANNOTATION_FONT

		val BOX_STROKE = Stroke(1.5f, LineCap.ROUND, LineJoin.ROUND)

		val TOOLTIP_STROKE = Stroke(1.0f)

		val TOOLTIP_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, 12)

		val HIGHLIGHT_STROKE = Stroke(8.0f, LineCap.ROUND, LineJoin.ROUND)

		val EDGE_STROKE = Stroke(1.0f)

		val EDGE_EXECUTION_STROKE = Stroke(1.3f)

		val BUS_STROKE = Stroke(3.0f, LineCap.BUTT, LineJoin.MITER)

		val SUBSYSTEM_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (1.8 * Look.SCALE).toInt())

		val SUBSYSTEM_STROKE = Stroke(1.0f, LineCap.BUTT, LineJoin.MITER, 5.0f, floatArrayOf(5.0f), 0.0f)

		val ERROR_COLOR = CompositeColor(
			foregroundColor = Color.RED,
			backgroundColor = Color(255, 214, 214),
			textColor = Color.WHITE)
		val FOCUS_STROKE = Stroke(0.8f, LineCap.BUTT, LineJoin.MITER, 1.0f, floatArrayOf(2.0f, 1.0f), 0.0f)

		val EXPLANATION_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, 12)

		// Signal colors

		val ZERO_COLOR = AntaresTheme.DEF_ZERO

		val ONE_COLOR = AntaresTheme.DEF_ONE

		val UNDEFINED_COLOR = AntaresTheme.DEF_UNDEFINED

		val BUS_FILL_COLOR = Color(232, 232, 232)

		val WORD_ZERO_COLOR = CompositeColor(
			foregroundColor = Color.BLACK,
			backgroundColor = BUS_FILL_COLOR,
			textColor = Color.WHITE)

		val WORD_COLOR = CompositeColor(
			foregroundColor = Color.GRAY,
			backgroundColor = BUS_FILL_COLOR,
			textColor = Color.WHITE)
	}

	abstract fun build(): AntaresTheme
}