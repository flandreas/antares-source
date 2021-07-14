package ch.scorpion.jabbah.graph.view.style

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.select.Handle
import ch.scorpion.jabbah.edit.style.EditTheme

/**
 * Adds more [Theme] properties for the [ch.scorpion.jabbah.graph] module.
 */
open class GraphTheme(
	name: String = DEF_NAME,
	dark: Boolean = DEF_DARK,
	referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider,
	referenceColors: List<ReferenceColor> = DEF_REF_COLORS,
	predefinedColors: List<PredefinedColor> = DEF_PREDEFINED_COLORS,
	background: Style = DEF_BACKGROUND,
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
	val vertice: Style = DEF_VERTICE,
	val edge: EdgeStyle = DEF_EDGE,
	val explanation: Style = DEF_EXPLANATION,
	val subsystem: Style = DEF_SUBSYSTEM,
	val error: CompositeColor = DEF_ERROR,
	val overlay: Color = DEF_OVERLAY
) : EditTheme(
	name,
	dark,
	referenceColorSequenceProvider,
	referenceColors,
	predefinedColors,
	background,
	figure,
	annotation,
	tooltip,
	shadow,
	hover,
	selection,
	highlight,
	snap,
	messageInfo,
	messageError
) {

	companion object {
		val DEF_VERTICE = BasicStyle()
		val DEF_EDGE = EdgeStyle()
		val DEF_EXPLANATION = BasicStyle()
		val DEF_SUBSYSTEM = BasicStyle()
		val DEF_ERROR = CompositeColor(foregroundColor = Color.RED, backgroundColor = Color(255, 214, 214), textColor = Color.BLACK)
		val DEF_OVERLAY = Color(255, 255, 255, 192)
	}

	override fun activateIn(styleRepository: StyleRepository, styleOnly: Boolean) {
		super.activateIn(styleRepository, styleOnly)
		styleRepository.registerStyle(GraphStyleType.VERTICE, vertice)
		styleRepository.registerStyle(GraphStyleType.EDGE, edge)
		styleRepository.registerStyle(GraphStyleType.EXPLANATION, explanation)
		styleRepository.registerStyle(GraphStyleType.SUBSYSTEM, subsystem)

		if (!styleOnly) {
			BaseModule.properties.set(Handle.PROP_BORDER_COLOR, selection.color.foregroundColor)
			BaseModule.properties.set(Handle.PROP_FILL_COLOR, selection.color.backgroundColor)
		}
	}
}