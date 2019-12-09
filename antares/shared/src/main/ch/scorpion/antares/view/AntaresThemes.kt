package ch.scorpion.antares.view

import ch.scorpion.jabbah.draw.style.Theme
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.style.EdgeStyle
import ch.scorpion.jabbah.graph.view.style.GraphTheme

/**
 * Defines [GraphTheme]s for the Antares application.
 */
object AntaresThemes {

	private val SELECTION_COLOR = Color.ORANGE

	private val FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, 15)
	private val EXPLANATION_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, 12)

	private val ANNOTATION_STROKE = Stroke(1.0f)
	private val HIGHLIGHT_STROKE = Stroke(10.0f, LineCap.ROUND, LineJoin.ROUND)
	private val BOX_STROKE = Stroke(1.5f, LineCap.ROUND, LineJoin.ROUND)
	private val EDGE_STROKE = Stroke(1.0f)
	private val EDGE_EXECUTION_STROKE = Stroke(1.3f)
	private val BUS_STROKE = Stroke(3.0f, LineCap.BUTT, LineJoin.ROUND)
	private val TOOLTIP = CompositeColor(
		foregroundColor = Color(252, 205, 90),
		backgroundColor = Color(255, 255, 223),
		textColor = Color.BLACK)
	private val EXPLANATION = CompositeColor(
		foregroundColor = Color.GRAY,
		backgroundColor = Color(240, 240, 240),
		textColor = Color.BLACK)
	private val SUBSYSTEM_COLOR = Color(244, 244, 244)
	private val SUBSYSTEM_FONT = FontImpl(FontFamily.SANS_SERIF, FontStyle.PLAIN.value, (1.8 * Look.SCALE).toInt())
	private val SUBSYSTEM_STROKE = Stroke(1.0f, LineCap.BUTT, LineJoin.MITER, 5.0f, floatArrayOf(5.0f), 0.0f)

	private val ZERO = CompositeColor(foregroundColor = Color(0, 115, 15), backgroundColor = Color.BLACK, textColor = Color.WHITE)
	private val ONE = CompositeColor(foregroundColor = Color(0, 255, 0), backgroundColor = Color(0, 115, 15), textColor = Color.BLACK)
	private val UNDEFINED = CompositeColor(foregroundColor = Color(40, 125, 249), backgroundColor = Color.BLACK, textColor = Color.WHITE)
	private val ERROR = CompositeColor(foregroundColor = Color.RED, backgroundColor = Color(255, 214, 214), textColor = Color.BLACK)
	private val INFO = CompositeColor(backgroundColor = Color(198, 226, 184), foregroundColor = Color(115, 191, 91), textColor = Color.BLACK)

	/** This should be taken from the current focus color of the System/Target.*/
	private val FOCUS_COLOR = Color(48, 131, 251)
	private val FOCUS_STROKE = Stroke(0.8f, LineCap.BUTT, LineJoin.MITER, 1.0f, floatArrayOf(2.0f, 1.0f), 0.0f)

	private val TOOLTIP_STROKE = Stroke(1.0f)

	fun install() {
		Themes.register(winter(), blackAndWhite(), crt())
	}

	private fun winter(): Theme {
		val highlightColor = CompositeColor(foregroundColor = Color.YELLOW, backgroundColor = Color.YELLOW)
		val skyBlue = CompositeColor(foregroundColor = Color(69, 113, 180), backgroundColor = Color(220, 237, 250))
		return AntaresTheme(
			name = "Winter",
			supportsWhiteBackground = true,
			background = BasicStyle(
				color = CompositeColor(
					foregroundColor = Color(232, 232, 232),
					backgroundColor = Color.WHITE,
					textColor = Color.BLACK),
				font = FONT,
				stroke = ANNOTATION_STROKE),
			figure = BasicStyle(
				color = skyBlue,
				stroke = BOX_STROKE,
				font = FONT,
				shadow = true),
			tooltip = BasicStyle(
				color = TOOLTIP,
				stroke = TOOLTIP_STROKE),
			highlight = BasicStyle(
				color = highlightColor,
				font = FONT,
				stroke = HIGHLIGHT_STROKE),
			messageError = BasicStyle(
				color = ERROR,
				font = EXPLANATION_FONT,
				stroke = ANNOTATION_STROKE),
			messageInfo = BasicStyle(
				color = INFO,
				font = EXPLANATION_FONT,
				stroke = ANNOTATION_STROKE),
			vertice = BasicStyle(
				color = skyBlue,
				stroke = BOX_STROKE,
				font = FONT,
				shadow = true),
			edge = EdgeStyle(
				color = CompositeColor(
					foregroundColor = Color.BLACK,
					backgroundColor = Color(232, 232, 232),
					textColor = Color.BLACK),
				stroke = EDGE_STROKE,
				executionStroke = EDGE_EXECUTION_STROKE,
				busStroke = BUS_STROKE,
				font = Look.ANNOTATION_FONT),
			annotation = BasicStyle(
				color = CompositeColor(
					foregroundColor = skyBlue.foregroundColor,
					backgroundColor = skyBlue.backgroundColor,
					textColor = skyBlue.foregroundColor),
				stroke = ANNOTATION_STROKE,
				font = Look.ANNOTATION_FONT),
			explanation = BasicStyle(
				color = EXPLANATION,
				stroke = ANNOTATION_STROKE,
				font = EXPLANATION_FONT),
			subsystem = BasicStyle(
				color = CompositeColor(
					foregroundColor = Color.GRAY,
					backgroundColor = SUBSYSTEM_COLOR),
				font = SUBSYSTEM_FONT,
				stroke = SUBSYSTEM_STROKE),
			selection = BasicStyle(
				color = CompositeColor(
					foregroundColor = SELECTION_COLOR,
					textColor = SELECTION_COLOR,
					backgroundColor = Color.WHITE)),
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
				stroke = FOCUS_STROKE),
			screen = CompositeColor(
				foregroundColor = Color.BLACK,
				backgroundColor = Color.DARK_GRAY,
				textColor = Color.WHITE
			)
		)
	}

	private fun crt(): Theme {
		val mainColor = CompositeColor(
			foregroundColor = ZERO.foregroundColor,
			backgroundColor = Color.BLACK
		)

		val veryDarkGreen = Color(2, 46, 8)
		// dark orange
		val highlightColor = Color(102, 61, 0)

		val explanationColor = CompositeColor(
			foregroundColor = Color(50, 232, 42),
			backgroundColor = veryDarkGreen
		)
		val shadow = Color(22, 54, 49)

		val red = CompositeColor(Color(236, 35, 46), Color(120, 3, 7))
		val blue = CompositeColor(Color(72, 186, 233), Color(3, 16, 139))
		val green = CompositeColor(Color(115, 191, 91), Color(7, 87, 9))
		val yellow = CompositeColor(Color(245, 235, 62), Color(67, 69, 10))

		return AntaresTheme(
			name = "CRT",
			supportsWhiteBackground = false,
			referenceColors = listOf(
				// Red
				red.withAlpha(DrawTheme.REF_COLOR_ALPHA),
				// Blue
				blue.withAlpha(DrawTheme.REF_COLOR_ALPHA),
				// Green
				green.withAlpha(DrawTheme.REF_COLOR_ALPHA),
				// Yellow
				yellow.withAlpha(DrawTheme.REF_COLOR_ALPHA),
				// Violet
				CompositeColor(Color(125, 108, 171), Color(55, 14, 91, DrawTheme.REF_COLOR_ALPHA)),
				// Pink
				CompositeColor(Color(188, 126, 179), Color(104, 8, 89, DrawTheme.REF_COLOR_ALPHA)),
				// Blue-Green
				CompositeColor(Color(90, 196, 194), Color(13, 110, 110, DrawTheme.REF_COLOR_ALPHA)),
				// Yellow-Orange
				CompositeColor(Color(247, 164, 49), Color(152, 103, 22, DrawTheme.REF_COLOR_ALPHA)),
				// Black
				CompositeColor(Color(234, 234, 234), Color(32, 32, 32, DrawTheme.REF_COLOR_ALPHA))),
			predefinedColors = listOf(
				PredefinedColor(PredefinedColorIdentity.White, DrawGraphicsModule.WHITE),
				PredefinedColor(PredefinedColorIdentity.Black, DrawGraphicsModule.BLACK),
				PredefinedColor(PredefinedColorIdentity.Gray, CompositeColor(foregroundColor = Color(64, 64, 64), backgroundColor = Color(32, 32, 32))),
				PredefinedColor(PredefinedColorIdentity.Red, red),
				PredefinedColor(PredefinedColorIdentity.Blue, blue),
				PredefinedColor(PredefinedColorIdentity.Green, green),
				PredefinedColor(PredefinedColorIdentity.Yellow, yellow)
			),
			highlight = BasicStyle(
				color = CompositeColor(
					backgroundColor = highlightColor,
					foregroundColor = highlightColor),
				stroke = HIGHLIGHT_STROKE),
			background = BasicStyle(
				color = CompositeColor(
					foregroundColor = Color(32, 32, 32),
					backgroundColor = Color.BLACK,
					textColor = mainColor.foregroundColor),
				stroke = ANNOTATION_STROKE,
				font = Look.ANNOTATION_FONT),
			figure = BasicStyle(
				color = mainColor,
				stroke = BOX_STROKE,
				font = FONT,
				shadow = true),
			tooltip = BasicStyle(
				color = explanationColor,
				stroke = TOOLTIP_STROKE
			),
			shadow = CompositeColor(shadow, shadow),
			vertice = BasicStyle(
				color = mainColor,
				stroke = BOX_STROKE,
				font = FONT,
				shadow = true),
			edge = EdgeStyle(
				color = CompositeColor(
					foregroundColor = ZERO.foregroundColor,
					backgroundColor = veryDarkGreen
				),
				stroke = EDGE_STROKE,
				executionStroke = EDGE_EXECUTION_STROKE,
				busStroke = BUS_STROKE,
				font = Look.ANNOTATION_FONT),
			annotation = BasicStyle(
				color = mainColor,
				stroke = ANNOTATION_STROKE,
				font = Look.ANNOTATION_FONT),
			explanation = BasicStyle(
				color = explanationColor,
				stroke = ANNOTATION_STROKE,
				font = EXPLANATION_FONT),
			messageError = BasicStyle(
				color = EXPLANATION,
				stroke = ANNOTATION_STROKE,
				font = EXPLANATION_FONT),
			messageInfo = BasicStyle(
				color = INFO,
				font = EXPLANATION_FONT,
				stroke = ANNOTATION_STROKE),
			subsystem = BasicStyle(
				font = SUBSYSTEM_FONT,
				color = CompositeColor(
					foregroundColor = Color(13, 116, 15),
					backgroundColor = Color(5, 40, 7)),
				stroke = SUBSYSTEM_STROKE),
			selection = BasicStyle(
				CompositeColor(
					foregroundColor = SELECTION_COLOR,
					backgroundColor = Color.BLACK)),
			zero = ZERO.withBackground(ZERO.foregroundColor),
			one = ONE,
			undefined = UNDEFINED,
			wordZero = CompositeColor(
				foregroundColor = ZERO.foregroundColor,
				backgroundColor = veryDarkGreen,
				textColor = ZERO.textColor
			),
			word = CompositeColor(
				foregroundColor = ONE.foregroundColor,
				backgroundColor = veryDarkGreen,
				textColor = ONE.textColor
			),
			error = ERROR,
			focus = BasicStyle(
				color = CompositeColor(
					foregroundColor = ONE.foregroundColor.between(ZERO.foregroundColor)
				),
				stroke = FOCUS_STROKE
			),
			overlay = Color(32, 32, 32, 192),
			screen = CompositeColor(
				foregroundColor = mainColor.foregroundColor,
				backgroundColor = Color(0, 24, 0),
				textColor = ONE.foregroundColor
			)
		)
	}

	private fun blackAndWhite(): Theme {
		val highlightColor = CompositeColor(foregroundColor = Color.YELLOW, backgroundColor = Color.YELLOW)
		val figureColor = CompositeColor(foregroundColor = Color.BLACK, backgroundColor = Color.WHITE)
		return AntaresTheme(
			name = "Black & White",
			supportsWhiteBackground = true,
			background = BasicStyle(
				color = CompositeColor(
					foregroundColor = Color(232, 232, 232),
					backgroundColor = Color.WHITE,
					textColor = Color.BLACK),
				font = FONT,
				stroke = ANNOTATION_STROKE),
			figure = BasicStyle(
				color = figureColor,
				stroke = BOX_STROKE,
				font = FONT,
				shadow = true),
			highlight = BasicStyle(
				color = highlightColor,
				font = FONT,
				stroke = HIGHLIGHT_STROKE),
			messageError = BasicStyle(
				color = ERROR,
				font = EXPLANATION_FONT,
				stroke = ANNOTATION_STROKE),
			messageInfo = BasicStyle(
				color = INFO,
				font = EXPLANATION_FONT,
				stroke = ANNOTATION_STROKE),
			vertice = BasicStyle(
				color = figureColor,
				stroke = BOX_STROKE,
				font = FONT,
				shadow = true),
			edge = EdgeStyle(
				color = CompositeColor(
					foregroundColor = Color.BLACK,
					backgroundColor = Color(232, 232, 232),
					textColor = Color.BLACK),
				stroke = EDGE_STROKE,
				executionStroke = EDGE_EXECUTION_STROKE,
				busStroke = BUS_STROKE,
				font = Look.ANNOTATION_FONT),
			annotation = BasicStyle(
				color = figureColor,
				stroke = ANNOTATION_STROKE,
				font = Look.ANNOTATION_FONT),
			explanation = BasicStyle(
				color = EXPLANATION,
				stroke = ANNOTATION_STROKE,
				font = EXPLANATION_FONT),
			subsystem = BasicStyle(
				color = CompositeColor(
					foregroundColor = Color.GRAY,
					backgroundColor = SUBSYSTEM_COLOR),
				font = SUBSYSTEM_FONT,
				stroke = SUBSYSTEM_STROKE),
			selection = BasicStyle(
				CompositeColor(
					foregroundColor = SELECTION_COLOR,
					textColor = SELECTION_COLOR,
					backgroundColor = Color.WHITE)),
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
				stroke = FOCUS_STROKE),
			screen = CompositeColor(
				foregroundColor = Color.BLACK,
				backgroundColor = Color.DARK_GRAY,
				textColor = Color.WHITE
			)
		)
	}
}