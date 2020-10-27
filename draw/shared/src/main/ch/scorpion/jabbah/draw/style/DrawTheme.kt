package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.draw.graphics.*

open class DrawTheme(
	override val name: String = DEF_NAME,
	override val dark: Boolean = DEF_DARK,
	private val referenceColorSequenceProvider: ReferenceColorSequenceProvider = ReferenceColorSequenceProvider,
	private val referenceColors: List<ReferenceColor> = DEF_REF_COLORS,
	private val predefinedColors: List<PredefinedColor> = DEF_PREDEFINED_COLORS,
	val background: Style = DEF_BACKGROUND,
	val figure: Style = DEF_FIGURE,
	val tooltip: Style = DEF_TOOLTIP,
	val shadow: CompositeColor = DEF_SHADOW
) : Theme {

	companion object {
		const val DEF_NAME = "default"
		const val DEF_DARK = false
		val DEF_BACKGROUND = BasicStyle(CompositeColor(Color(240, 240, 240), Color.WHITE, Color.BLACK))
		val DEF_FIGURE = BasicStyle(CompositeColor(Color.BLACK, Color.WHITE, Color.BLACK))
		val DEF_TOOLTIP = BasicStyle(CompositeColor(foregroundColor = Color(249, 214, 54),
			backgroundColor = Color(255, 253, 219), textColor = Color.BLACK))
		val DEF_SHADOW = CompositeColor(Color.GRAY, Color.GRAY, Color.GRAY)

		val DEF_REF_COLORS = listOf(
			// TODO Do the darker colors need individual design?
			ReferenceColor(DrawGraphicsModule.RED, DrawGraphicsModule.RED.darker()),
			ReferenceColor(DrawGraphicsModule.BLUE, DrawGraphicsModule.BLUE.darker()),
			ReferenceColor(DrawGraphicsModule.GREEN, DrawGraphicsModule.GREEN.darker()),
			ReferenceColor(DrawGraphicsModule.YELLOW, DrawGraphicsModule.YELLOW.darker()),
			ReferenceColor(DrawGraphicsModule.VIOLET, DrawGraphicsModule.VIOLET.darker()),
			ReferenceColor(DrawGraphicsModule.PINK, DrawGraphicsModule.PINK.darker()),
			ReferenceColor(DrawGraphicsModule.GRAY, DrawGraphicsModule.GRAY.darker()),
			ReferenceColor(DrawGraphicsModule.WHITE, DrawGraphicsModule.WHITE.darker()),
			ReferenceColor(DrawGraphicsModule.BLACK, DrawGraphicsModule.BLACK.darker())
		)

		val DEF_PREDEFINED_COLORS = listOf(
			PredefinedColor(PredefinedColorIdentity.White, DrawGraphicsModule.WHITE),
			PredefinedColor(PredefinedColorIdentity.Black, DrawGraphicsModule.BLACK),
			PredefinedColor(PredefinedColorIdentity.Gray, DrawGraphicsModule.GRAY),
			PredefinedColor(PredefinedColorIdentity.Red, DrawGraphicsModule.RED),
			PredefinedColor(PredefinedColorIdentity.Blue, DrawGraphicsModule.BLUE),
			PredefinedColor(PredefinedColorIdentity.Green, DrawGraphicsModule.GREEN),
			PredefinedColor(PredefinedColorIdentity.Yellow, DrawGraphicsModule.YELLOW)
		)
	}

	override fun toString(): String = name

	override fun activateIn(styleRepository: StyleRepository, styleOnly: Boolean) {
		if (!styleOnly) {
			referenceColorSequenceProvider.replaceColors(referenceColors)
			predefinedColors.forEach { PredefinedColorRepository.register(it) }
		}
		styleRepository.registerStyle(StyleType.BACKGROUND, background)
		styleRepository.registerStyle(StyleType.FIGURE, figure)
		styleRepository.registerStyle(StyleType.TOOLTIP, tooltip)
	}
}