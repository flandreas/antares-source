package ch.scorpion.antares.view.theme

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.graph.view.style.EdgeStyle

object CrtThemeBuilder : AbstractAntaresDarkThemeBuilder("CRT") {

	private val MAIN_COLOR = CompositeColor(
		foregroundColor = ZERO_COLOR.foregroundColor,
		backgroundColor = BACKGROUND_BACKGROUND
	)

	private val BACKGROUND_COLOR = CompositeColor(
		foregroundColor = BACKGROUND_FOREGROUND,
		backgroundColor = BACKGROUND_BACKGROUND,
		textColor = MAIN_COLOR.textColor
	)

	val SHADOW_COLOR = Color(22, 54, 49)

	private val VERY_DARK_GREEN = Color(2, 46, 8)

	private val EXPLANATION_COLOR = CompositeColor(
		foregroundColor = Color(50, 232, 42),
		backgroundColor = VERY_DARK_GREEN
	)

	private val INFO_COLOR = CompositeColor(
		backgroundColor = Color(198, 226, 184),
		foregroundColor = Color(115, 191, 91),
		textColor = Color.BLACK
	)

	private val SUBSYSTEM_COLOR = CompositeColor(
		foregroundColor = Color(13, 116, 15),
		backgroundColor = BACKGROUND_BACKGROUND.greener()
	)

	private val TOOLTIP_STYLE = BasicStyle(color = EXPLANATION_COLOR, stroke = TOOLTIP_STROKE, font = TOOLTIP_FONT)

	private val VERTICE_STYLE = BasicStyle(color = MAIN_COLOR, stroke = BOX_STROKE, font = FONT, shadow = true)

	private val EDGE_COLOR = CompositeColor(foregroundColor = ZERO_COLOR.foregroundColor, backgroundColor = VERY_DARK_GREEN)

	private val EDGE_STYLE = EdgeStyle(color = EDGE_COLOR, stroke = EDGE_STROKE, executionStroke = EDGE_EXECUTION_STROKE, font = ANNOTATION_FONT)

	private val ANNOTATION_STYLE = BasicStyle(color = MAIN_COLOR, stroke = ANNOTATION_STROKE, font = ANNOTATION_FONT)

	private val EXPLANATION_STYLE = BasicStyle(color = EXPLANATION_COLOR, stroke = ANNOTATION_STROKE, font = EXPLANATION_FONT)

	private val MESSAGE_ERROR_STYLE = BasicStyle(color = EXPLANATION_COLOR, stroke = ANNOTATION_STROKE, font = EXPLANATION_FONT)

	private val MESSAGE_INFO_STYLE = BasicStyle(color = INFO_COLOR, stroke = ANNOTATION_STROKE, font = EXPLANATION_FONT)

	private val SUBSYSTEM_STYLE = BasicStyle(color = SUBSYSTEM_COLOR, stroke = SUBSYSTEM_STROKE, font = SUBSYSTEM_FONT)

	private val SELECTION_STYLE = BasicStyle(color = CompositeColor(foregroundColor = DARK_SELECTION_COLOR, backgroundColor = BACKGROUND_BACKGROUND))

	private val UNDEFINED_FOREGROUND_COLOR = Color(40, 125, 249)

	private val UNDEFINED_COLOR = CompositeColor(
		foregroundColor = UNDEFINED_FOREGROUND_COLOR,
		backgroundColor = UNDEFINED_FOREGROUND_COLOR,
		textColor = Color.WHITE)

	private val WORD_ZERO_COLOR = CompositeColor(
		foregroundColor = ZERO_COLOR.foregroundColor,
		backgroundColor = VERY_DARK_GREEN,
		textColor = ZERO_COLOR.textColor
	)

	private val WORD_COLOR = CompositeColor(
		foregroundColor = ONE_COLOR.foregroundColor,
		backgroundColor = VERY_DARK_GREEN,
		textColor = ONE_COLOR.textColor
	)

	private val FOCUS_COLOR = CompositeColor(foregroundColor = ONE_COLOR.foregroundColor.between(ZERO_COLOR.foregroundColor))

	private val FOCUS_STYLE = BasicStyle(color = FOCUS_COLOR, stroke = FOCUS_STROKE)

	private val OVERLAY_COLOR = Color(32, 32, 32, 192)

	private val SCREEN_COLOR = CompositeColor(
		foregroundColor = MAIN_COLOR.foregroundColor,
		backgroundColor = Color(0, 24, 0),
		textColor = ONE_COLOR.foregroundColor
	)

	override fun build(): AntaresTheme {
		return AntaresTheme(
			name = name,
			dark = true,
			referenceColors = REFERENCE_COLORS,
			predefinedColors = PREDEFINED_COLORS,
			background = BasicStyle(color = BACKGROUND_COLOR, stroke = ANNOTATION_STROKE, font = FONT),
			figure = BasicStyle(color = MAIN_COLOR, stroke = BOX_STROKE, font = FONT, shadow = true),
			tooltip = TOOLTIP_STYLE,
			highlight = HIGHLIGHT_STYLE,
			vertice = VERTICE_STYLE,
			edge = EDGE_STYLE,
			annotation = ANNOTATION_STYLE,
			explanation = EXPLANATION_STYLE,
			messageError = MESSAGE_ERROR_STYLE,
			messageInfo = MESSAGE_INFO_STYLE,
			subsystem = SUBSYSTEM_STYLE,
			selection = SELECTION_STYLE,
			zero = ZERO_COLOR.withBackground(ZERO_COLOR.foregroundColor),
			one = ONE_COLOR,
			undefined = UNDEFINED_COLOR,
			wordZero = WORD_ZERO_COLOR,
			word = WORD_COLOR,
			error = ERROR_COLOR,
			focus = FOCUS_STYLE,
			overlay = OVERLAY_COLOR,
			screen = SCREEN_COLOR,
			shadow = CompositeColor(SHADOW_COLOR, SHADOW_COLOR)
		)
	}
}