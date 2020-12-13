package ch.scorpion.antares.view.theme

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule
import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.graph.view.style.EdgeStyle

object DarculaThemeBuilder : AbstractAntaresDarkThemeBuilder("Darcula") {

	private val MAIN_COLOR = CompositeColor(
		foregroundColor = Color(162, 162, 162),
		backgroundColor = Color(48, 48, 48))

	private val BACKGROUND_COLOR = CompositeColor(
		foregroundColor = BACKGROUND_FOREGROUND,
		backgroundColor = BACKGROUND_BACKGROUND,
		textColor = MAIN_COLOR.textColor
	)

	private val TOOLTIP_COLOR = DrawGraphicsModule.YELLOW_ON_DARK

	val SHADOW_COLOR = MAIN_COLOR.foregroundColor.darker()

	private val EXPLANATION_COLOR = CompositeColor(
		foregroundColor = MAIN_COLOR.foregroundColor,
		backgroundColor = MAIN_COLOR.backgroundColor.brighter()
	)

	private val SUBSYSTEM_COLOR = CompositeColor(
		foregroundColor = MAIN_COLOR.foregroundColor,
		backgroundColor = Color(64, 64, 64)
	)

	private val SCREEN_COLOR = CompositeColor(
		foregroundColor = MAIN_COLOR.foregroundColor.darker().darker(),
		backgroundColor = MAIN_COLOR.backgroundColor.darker(),
		textColor = ONE_COLOR.foregroundColor
	)

	private val FOCUS_COLOR = CompositeColor(MAIN_COLOR.foregroundColor)

	private val MESSAGE_ERROR_COLOR = DrawGraphicsModule.RED_ON_DARK

	private val MESSAGE_INFO_COLOR = DrawGraphicsModule.GREEN_ON_DARK

	private val BUS_FILL_COLOR = Color(48, 48, 48)

	private val WORD_ZERO_COLOR = CompositeColor(
		foregroundColor = MAIN_COLOR.foregroundColor.brighter(),
		backgroundColor = BUS_FILL_COLOR,
		textColor = Color.BLACK)

	private val WORD_COLOR = CompositeColor(
		foregroundColor = MAIN_COLOR.foregroundColor.darker(),
		backgroundColor = BUS_FILL_COLOR,
		textColor = Color.WHITE)

	private val SELECTION_COLOR = CompositeColor(foregroundColor = DARK_SELECTION_COLOR, backgroundColor = MAIN_COLOR.backgroundColor)

	private val BACKGROUND_STYLE = BasicStyle(color = BACKGROUND_COLOR, stroke = ANNOTATION_STROKE, font = FONT)

	private val FIGURE_STYLE = BasicStyle(color = MAIN_COLOR, stroke = BOX_STROKE, font = FONT, shadow = true)

	private val VERTICE_STYLE = BasicStyle(color = MAIN_COLOR, stroke = BOX_STROKE, font = FONT, shadow = true)

	private val EDGE_STYLE = EdgeStyle(color = MAIN_COLOR, stroke = EDGE_STROKE, executionStroke = EDGE_EXECUTION_STROKE, font = ANNOTATION_FONT)

	private val ANNOTATION_STYLE = BasicStyle(color = MAIN_COLOR, stroke = ANNOTATION_STROKE, font = ANNOTATION_FONT)

	private val SELECTION_STYLE = BasicStyle(color = SELECTION_COLOR)

	private val TOOLTIP_STYLE = BasicStyle(color = TOOLTIP_COLOR, stroke = TOOLTIP_STROKE, font = TOOLTIP_FONT)

	private val SUBSYSTEM_STYLE = BasicStyle(color = SUBSYSTEM_COLOR, stroke = SUBSYSTEM_STROKE, font = SUBSYSTEM_FONT)

	private val EXPLANATION_STYLE = BasicStyle(color = EXPLANATION_COLOR, stroke = ANNOTATION_STROKE, font = EXPLANATION_FONT)

	private val MESSAGE_ERROR_STYLE = BasicStyle(color = MESSAGE_ERROR_COLOR, stroke = ANNOTATION_STROKE, font = EXPLANATION_FONT)

	private val MESSAGE_INFO_STYLE = BasicStyle(color = MESSAGE_INFO_COLOR, stroke = ANNOTATION_STROKE, font = EXPLANATION_FONT)

	private val FOCUS_STYLE = BasicStyle(color = FOCUS_COLOR, stroke = FOCUS_STROKE)

	override fun build(): AntaresTheme {
		return AntaresTheme(
			name = name,
			dark = true,
			referenceColors = REFERENCE_COLORS,
			predefinedColors = PREDEFINED_COLORS,
			background = BACKGROUND_STYLE,
			figure = FIGURE_STYLE,
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
			zero = ZERO_COLOR,
			one = ONE_COLOR,
			undefined = UNDEFINED_COLOR,
			wordZero = WORD_ZERO_COLOR,
			word = WORD_COLOR,
			error = ERROR_COLOR,
			focus = FOCUS_STYLE,
			overlay = OVERLAY_COLOR,
			screen = SCREEN_COLOR,
			shadow = CompositeColor(SHADOW_COLOR, SHADOW_COLOR),
			hover = SELECTION_COLOR
		)
	}
}