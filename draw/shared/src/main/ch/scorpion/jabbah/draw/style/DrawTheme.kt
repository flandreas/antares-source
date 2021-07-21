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
	val annotation: Style = DEF_ANNOTATION,
	val tooltip: Style = DEF_TOOLTIP,
	val shadow: CompositeColor = DEF_SHADOW,
	val hover: CompositeColor = DEF_HOVER
) : Theme {

	companion object {
		const val DEF_NAME = "default"
		const val DEF_DARK = false
		val DEF_BACKGROUND = BasicStyle(CompositeColor(Color(240, 240, 240), Color.WHITE, Color.BLACK))
		val DEF_FIGURE = BasicStyle()
		val DEF_ANNOTATION = BasicStyle(stroke = DEF_FIGURE.stroke.thinner)
		val DEF_TOOLTIP = BasicStyle(CompositeColor(foregroundColor = Color(249, 214, 54),
			backgroundColor = Color(255, 253, 219), textColor = Color.BLACK))
		val DEF_SHADOW = CompositeColor(Color.GRAY, Color.GRAY, Color.GRAY)

		val DEF_HOVER = CompositeColor(Color.ORANGE, Color.WHITE)

		val DEF_REF_COLORS = listOf(
			ReferenceColor(DrawGraphicsModule.YELLOW, DrawGraphicsModule.YELLOW_ON_DARK),
			ReferenceColor(DrawGraphicsModule.RED, DrawGraphicsModule.RED_ON_DARK),
			ReferenceColor(DrawGraphicsModule.BLUE, DrawGraphicsModule.BLUE_ON_DARK),
			ReferenceColor(DrawGraphicsModule.GREEN, DrawGraphicsModule.GREEN_ON_DARK),
			ReferenceColor(DrawGraphicsModule.PINK, DrawGraphicsModule.PINK_ON_DARK),
			ReferenceColor(DrawGraphicsModule.VIOLET, DrawGraphicsModule.VIOLET_ON_DARK),
			ReferenceColor(DrawGraphicsModule.GRAY, DrawGraphicsModule.GRAY.darker()),
			ReferenceColor(DrawGraphicsModule.WHITE, DrawGraphicsModule.WHITE.darker()),
			ReferenceColor(DrawGraphicsModule.BLACK, DrawGraphicsModule.BLACK.darker())
		)

		val DEF_PREDEFINED_COLORS = listOf(
			PredefinedColor(PredefinedColorIdentity.White, DrawGraphicsModule.WHITE),
			PredefinedColor(PredefinedColorIdentity.Black, DrawGraphicsModule.BLACK),
			PredefinedColor(PredefinedColorIdentity.Gray, DrawGraphicsModule.GRAY),
			PredefinedColor(PredefinedColorIdentity.Yellow, DrawGraphicsModule.YELLOW),
			PredefinedColor(PredefinedColorIdentity.Brown, DrawGraphicsModule.BROWN),
			PredefinedColor(PredefinedColorIdentity.Red, DrawGraphicsModule.RED),
			PredefinedColor(PredefinedColorIdentity.Violet, DrawGraphicsModule.VIOLET),
			PredefinedColor(PredefinedColorIdentity.Blue, DrawGraphicsModule.BLUE),
			PredefinedColor(PredefinedColorIdentity.Turquoise, DrawGraphicsModule.TURQUOISE),
			PredefinedColor(PredefinedColorIdentity.Green, DrawGraphicsModule.GREEN),
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
		styleRepository.registerStyle(StyleType.ANNOTATION, annotation)
		styleRepository.registerStyle(StyleType.TOOLTIP, tooltip)
	}
}