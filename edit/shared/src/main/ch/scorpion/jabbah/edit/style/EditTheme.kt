package ch.scorpion.jabbah.edit.style

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.select.RubberBand

/**
 * Adds more [Theme] properties for the [ch.scorpion.jabbah.edit] module.
 */
open class EditTheme(
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
	val selection: Style = DEF_SELECTION,
	val highlight: Style = DEF_HIGHLIGHT,
	val snap: Style = DEF_SNAP,
	val messageInfo: Style = DEF_MESSAGE_INFO,
	val messageError: Style = DEF_MESSAGE_ERROR
) : DrawTheme(
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
	hover
) {

	companion object {
		// Red
		val DEF_ERROR_MESSAGE_COLOR = CompositeColor(foregroundColor = Color(237, 76, 48), backgroundColor = Color(251, 225, 216), textColor = Color.BLACK)
		// Blue
		val DEF_INFO_MESSAGE_COLOR = CompositeColor(backgroundColor = Color(198, 226, 184), foregroundColor = Color(115, 191, 91), textColor = Color.BLACK)

		val DEF_SELECTION = BasicStyle(CompositeColor(Color.ORANGE, Color.WHITE, Color.ORANGE))
		val DEF_HIGHLIGHT = BasicStyle(CompositeColor(Color.YELLOW, Color.YELLOW, Color.BLACK))
		val DEF_SNAP = BasicStyle(CompositeColor(Color.GREEN, Color.GREEN, Color.GREEN), Stroke(0.5f))
		val DEF_MESSAGE_ERROR = BasicStyle(DEF_ERROR_MESSAGE_COLOR)
		val DEF_MESSAGE_INFO = BasicStyle(DEF_INFO_MESSAGE_COLOR)
	}

	override fun activateIn(styleRepository: StyleRepository, styleOnly: Boolean) {
		super.activateIn(styleRepository, styleOnly)
		styleRepository.registerStyle(EditStyleType.SELECTION, selection)
		styleRepository.registerStyle(EditStyleType.HIGHLIGHT, highlight)
		styleRepository.registerStyle(EditStyleType.MESSAGE_INFO, messageInfo)
		styleRepository.registerStyle(EditStyleType.MESSAGE_ERROR, messageError)

		if (!styleOnly) {
			BaseModule.properties.set(RubberBand.PROP_FILL_PAINT, selection.color.foregroundColor.withAlpha(32))
			BaseModule.properties.set(RubberBand.PROP_STROKE_PAINT, selection.color.foregroundColor)
		}
	}
}