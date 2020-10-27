package ch.scorpion.antares.view.theme

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.draw.style.DrawTheme

abstract class AbstractAntaresDarkThemeBuilder(name: String) : AbstractAntaresThemeBuilder(name) {

	companion object {

		val RED = CompositeColor(Color(236, 35, 46), Color(120, 3, 7))
		val BLUE = CompositeColor(Color(72, 186, 233), Color(3, 16, 139))
		val GREEN = CompositeColor(Color(115, 191, 91), Color(7, 87, 9))
		val YELLOW = CompositeColor(Color(245, 235, 62), Color(67, 69, 10))

		val REFERENCE_COLORS = listOf(
			// TODO: Design colors explicitly
			ReferenceColor(RED),
			ReferenceColor(BLUE),
			ReferenceColor(GREEN),
			ReferenceColor(YELLOW),
			// Violet
			ReferenceColor(CompositeColor(Color(125, 108, 171), Color(55, 14, 91))),
			// Pink
			ReferenceColor(CompositeColor(Color(188, 126, 179), Color(104, 8, 89))),
			// Blue-Green
			ReferenceColor(CompositeColor(Color(90, 196, 194), Color(13, 110, 110))),
			// Yellow-Orange
			ReferenceColor(CompositeColor(Color(247, 164, 49), Color(152, 103, 22))),
			// Black
			ReferenceColor(CompositeColor(Color(234, 234, 234), Color(32, 32, 32))))

		val PREDEFINED_COLORS = listOf(
			PredefinedColor(PredefinedColorIdentity.White, DrawGraphicsModule.WHITE),
			PredefinedColor(PredefinedColorIdentity.Black, DrawGraphicsModule.BLACK),
			PredefinedColor(PredefinedColorIdentity.Gray, CompositeColor(foregroundColor = Color(64, 64, 64), backgroundColor = Color(32, 32, 32))),
			PredefinedColor(PredefinedColorIdentity.Red, RED),
			PredefinedColor(PredefinedColorIdentity.Blue, BLUE),
			PredefinedColor(PredefinedColorIdentity.Green, GREEN),
			PredefinedColor(PredefinedColorIdentity.Yellow, YELLOW)
		)

		val DARK_SELECTION_COLOR = SELECTION_FOREGROUND_COLOR

		val BACKGROUND_BACKGROUND = Color(24, 24, 24)
		val BACKGROUND_FOREGROUND = Color(36, 36, 36)

		val HIGHLIGHT_COLOR = Color(102, 61, 0) // dark orange

		val HIGHLIGHT_STYLE = BasicStyle(
			color = CompositeColor(foregroundColor = HIGHLIGHT_COLOR, backgroundColor = HIGHLIGHT_COLOR),
			stroke = HIGHLIGHT_STROKE,
			font = FONT)
	}
}