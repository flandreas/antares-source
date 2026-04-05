package io.antarescircuit.antares.view.style

import io.antarescircuit.jabbah.draw.graphics.*
import io.antarescircuit.jabbah.draw.style.*
import io.antarescircuit.jabbah.graph.view.style.EdgeStyle
import io.antarescircuit.jabbah.graph.view.style.GraphTheme

/**
 * Adds more [Theme] properties for the [io.antarescircuit.antares.view] module.
 */
class AntaresTheme(
	name: String = DEF_NAME,
	dark: Boolean = DEF_DARK,
	referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider,
	referenceColors: List<ReferenceColor> = DEF_REF_COLORS,
	predefinedColors: List<PredefinedColor> = DEF_PREDEFINED_COLORS,
	background: Style = DEF_BACKGROUND,
	text: Style = DEF_TEXT,
	figure: Style = DEF_FIGURE,
	annotation: Style = DEF_ANNOTATION,
	tooltip: Style = DEF_TOOLTIP,
	shadow: CompositeColor = DEF_SHADOW,
	hover: CompositeColor = DEF_HOVER,
	selection: Style = DEF_SELECTION,
	highlight: Style = DEF_HIGHLIGHT,
	snap: Style = DEF_SNAP,
	messageInfo: Style = DEF_MESSAGE_INFO,
	messageError: Style = DEF_MESSAGE_ERROR,
	vertice: Style = DEF_VERTICE,
	edge: EdgeStyle = DEF_EDGE,
	explanation: Style = DEF_EXPLANATION,
	subsystem: Style = DEF_SUBSYSTEM,
	error: CompositeColor = DEF_ERROR,
	overlay: Color = DEF_OVERLAY,
	val zero: CompositeColor = DEF_ZERO,
	val one: CompositeColor = DEF_ONE,
	val undefined: CompositeColor = DEF_UNDEFINED,
	val wordZero: CompositeColor = DEF_WORD_ZERO,
	val word: CompositeColor = DEF_WORD,
	val focus: Style = DEF_FOCUS,
	val screen: CompositeColor = DEF_SCREEN
) : GraphTheme(
	name,
	dark,
	referenceColorSequenceProvider,
	referenceColors,
	predefinedColors,
	background,
	text,
	figure,
	annotation,
	tooltip,
	shadow,
	hover,
	selection,
	highlight,
	snap,
	messageInfo,
	messageError,
	vertice,
	edge,
	explanation,
	subsystem,
	error,
	overlay
) {
	companion object {
		val DEF_ZERO_FOREGROUND_COLOR = Color(0, 115, 15)
		val DEF_ZERO_BACKGROUND_COLOR = Color.BLACK
		val DEF_ZERO_TEXT_COLOR = Color.WHITE
		val DEF_ZERO = CompositeColor(foregroundColor = DEF_ZERO_FOREGROUND_COLOR, backgroundColor = DEF_ZERO_BACKGROUND_COLOR, textColor = DEF_ZERO_TEXT_COLOR)

		val DEF_ONE_FOREGROUND_COLOR = Color(0, 255, 0)
		val DEF_ONE_BACKGROUND_COLOR = Color(0, 115, 15)
		val DEF_ONE_TEXT_COLOR = Color.BLACK
		val DEF_ONE = CompositeColor(foregroundColor = DEF_ONE_FOREGROUND_COLOR, backgroundColor = DEF_ONE_BACKGROUND_COLOR, textColor = DEF_ONE_TEXT_COLOR)

		val DEF_UNDEFINED_FOREGROUND_COLOR = Color(40, 125, 249)
		val DEF_UNDEFINED_BACKGROUND_COLOR = Color.BLACK
		val DEF_UNDEFINED_TEXT_COLOR = Color.WHITE
		val DEF_UNDEFINED = CompositeColor(foregroundColor = DEF_UNDEFINED_FOREGROUND_COLOR, backgroundColor = DEF_UNDEFINED_BACKGROUND_COLOR, textColor = DEF_UNDEFINED_TEXT_COLOR)

		val DEF_WORD_ZERO = CompositeColor(foregroundColor = Color.BLACK, backgroundColor = Color(232, 232, 232), textColor = Color.WHITE)
		val DEF_WORD = CompositeColor(foregroundColor = Color.GRAY, backgroundColor = Color(232, 232, 232), textColor = Color.WHITE)
		val DEF_FOCUS = BasicStyle(
			color = CompositeColor(foregroundColor = Color(48, 131, 251)),
			stroke = Stroke(1.0f, LineCap.BUTT, LineJoin.MITER, 1.0f, floatArrayOf(2.0f, 1.0f), 0.0f)
		)
		val DEF_SCREEN = CompositeColor(Color.BLACK, Color.BLACK, Color.WHITE)
	}
}