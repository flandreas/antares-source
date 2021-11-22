package ch.scorpion.antares.view.theme

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.draw.style.Theme
import ch.scorpion.jabbah.graph.view.style.EdgeStyle

/**
 * Defines [Theme] property values suggested to be used by light [AntaresTheme]s.
 */
abstract class AbstractAntaresLightThemeBuilder(name: String) : AbstractAntaresThemeBuilder(name) {

	companion object {

		val BACKGROUND_COLOR = CompositeColor(
			foregroundColor = Color(232, 232, 232),
			backgroundColor = Color.WHITE,
			textColor = Color.BLACK)

		val BACKGROUND_STYLE = BasicStyle(color = BACKGROUND_COLOR, stroke = ANNOTATION_STROKE, font = FONT,)

		private val TEXT_COLOR = CompositeColor(
			foregroundColor = Color.BLACK,
			backgroundColor = BACKGROUND_COLOR.backgroundColor)

		private val TEXT_STYLE = BasicStyle(color = TEXT_COLOR, stroke = ANNOTATION_STROKE, font = TEXT_FONT)

		val TOOLTIP_COLOR = CompositeColor(
			foregroundColor = Color(252, 205, 90),
			backgroundColor = Color(255, 255, 223),
			textColor = Color.BLACK)

		val TOOLTIP_STYLE = BasicStyle(color = TOOLTIP_COLOR, stroke = TOOLTIP_STROKE, font = TOOLTIP_FONT)

		val HIGHLIGHT_COLOR = CompositeColor(foregroundColor = Color.YELLOW, backgroundColor = Color.YELLOW)

		val HIGHLIGHT_STYLE = BasicStyle(color = HIGHLIGHT_COLOR, stroke = HIGHLIGHT_STROKE, font = FONT)

		val EDGE_COLOR = CompositeColor(
			foregroundColor = Color.BLACK,
			backgroundColor = Color(232, 232, 232),
			textColor = Color.BLACK)

		val EDGE_STYLE = EdgeStyle(
			color = EDGE_COLOR,
			stroke = EDGE_STROKE,
			executionStroke = EDGE_EXECUTION_STROKE,
			busStroke = BUS_STROKE,
			font = ANNOTATION_FONT)

		val EXPLANATION_COLOR = CompositeColor(
			foregroundColor = Color.GRAY,
			backgroundColor = Color(240, 240, 240),
			textColor = Color.BLACK)

		val EXPLANATION_STYLE = BasicStyle(color = EXPLANATION_COLOR, stroke = ANNOTATION_STROKE, font = EXPLANATION_FONT)

		val SUBSYSTEM_COLOR = CompositeColor(
			foregroundColor = Color.GRAY,
			backgroundColor = Color(244, 244, 244))

		val SUBSYSTEM_STYLE = BasicStyle(color = SUBSYSTEM_COLOR, stroke = SUBSYSTEM_STROKE, font = SUBSYSTEM_FONT)

		val SELECTION_COLOR = CompositeColor(
			foregroundColor = SELECTION_FOREGROUND_COLOR,
			backgroundColor = BACKGROUND_COLOR.backgroundColor
		)

		val SELECTION_STYLE = BasicStyle(color = SELECTION_COLOR)

		/** TODO: This should be taken from the current focus color of the System/Target.*/
		private val FOCUS_COLOR = CompositeColor(foregroundColor = Color(48, 131, 251))

		val FOCUS_STYLE = BasicStyle(color = FOCUS_COLOR, stroke = FOCUS_STROKE)

		val SCREEN_COLOR = CompositeColor(
			foregroundColor = Color(67, 67, 67),
			backgroundColor = Color(33, 33, 33),
			textColor = Color.LIGHT_GRAY)

	}

	protected fun standardForColor(color: CompositeColor): AntaresTheme {
		return AntaresTheme(
			name = name,
			dark = false,
			background = BACKGROUND_STYLE,
			text = TEXT_STYLE,
			figure = BasicStyle(color = color, stroke = BOX_STROKE, font = FONT, shadow = true),
			tooltip = TOOLTIP_STYLE,
			highlight = HIGHLIGHT_STYLE,
			vertice = BasicStyle(color = color, stroke = BOX_STROKE, font = FONT, shadow = true),
			edge = EDGE_STYLE,
			annotation = BasicStyle(color = color, stroke = ANNOTATION_STROKE, font = ANNOTATION_FONT),
			explanation = EXPLANATION_STYLE,
			subsystem = SUBSYSTEM_STYLE,
			selection = SELECTION_STYLE,
			zero = ZERO_COLOR,
			one = ONE_COLOR,
			undefined = UNDEFINED_COLOR,
			wordZero = WORD_ZERO_COLOR,
			word = WORD_COLOR,
			error = ERROR_COLOR,
			focus = FOCUS_STYLE,
			screen = SCREEN_COLOR
		)
	}
}