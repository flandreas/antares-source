package ch.scorpion.antares.view.theme

import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule.BLUE_ON_DARK
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule.BROWN_ON_DARK
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule.GRAY_ON_DARK
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule.GREEN_ON_DARK
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule.PINK_ON_DARK
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule.RED_ON_DARK
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule.TURQUOISE_ON_DARK
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule.VIOLET_ON_DARK
import ch.scorpion.jabbah.draw.graphics.DrawGraphicsModule.YELLOW_ON_DARK
import ch.scorpion.jabbah.draw.style.BasicStyle

abstract class AbstractAntaresDarkThemeBuilder(name: String) : AbstractAntaresThemeBuilder(name) {

	companion object {

		val REFERENCE_COLORS = listOf(
			ReferenceColor(BLUE_ON_DARK),
			ReferenceColor(YELLOW_ON_DARK),
			ReferenceColor(RED_ON_DARK),
			ReferenceColor(GREEN_ON_DARK),
			// Blue-Green
			ReferenceColor(CompositeColor(Color(90, 196, 194), Color(13, 110, 110))),
			// Yellow-Orange
			ReferenceColor(CompositeColor(Color(247, 164, 49), Color(152, 103, 22))),
			ReferenceColor(PINK_ON_DARK),
			ReferenceColor(VIOLET_ON_DARK),
			// Black
			ReferenceColor(CompositeColor(Color(234, 234, 234), Color(32, 32, 32)))
		)

		val PREDEFINED_COLORS = listOf(
			PredefinedColor(PredefinedColorIdentity.White, DrawGraphicsModule.WHITE),
			PredefinedColor(PredefinedColorIdentity.Black, DrawGraphicsModule.BLACK),
			PredefinedColor(PredefinedColorIdentity.Gray, GRAY_ON_DARK),
			PredefinedColor(PredefinedColorIdentity.Yellow, YELLOW_ON_DARK),
			PredefinedColor(PredefinedColorIdentity.Brown, BROWN_ON_DARK),
			PredefinedColor(PredefinedColorIdentity.Red, RED_ON_DARK),
			PredefinedColor(PredefinedColorIdentity.Violet, VIOLET_ON_DARK),
			PredefinedColor(PredefinedColorIdentity.Blue, BLUE_ON_DARK),
			PredefinedColor(PredefinedColorIdentity.Turquoise, TURQUOISE_ON_DARK),
			PredefinedColor(PredefinedColorIdentity.Green, GREEN_ON_DARK),
		)

		val DARK_BUS_FILL_COLOR = Color(48, 48, 48)

		val DARK_SELECTION_COLOR = SELECTION_FOREGROUND_COLOR

		val BACKGROUND_BACKGROUND = Color(24, 24, 24)
		val BACKGROUND_FOREGROUND = Color(36, 36, 36)

		// dark yellow
		val HIGHLIGHT_COLOR = Color(86, 86, 0)

		val HIGHLIGHT_STYLE = BasicStyle(
			color = CompositeColor(HIGHLIGHT_COLOR, HIGHLIGHT_COLOR),
			stroke = HIGHLIGHT_STROKE,
			font = FONT)

		val OVERLAY_COLOR = Color(32, 32, 32, 192)

		val DARK_ZERO_COLOR = ZERO_COLOR.withBackground(ZERO_COLOR.foregroundColor.darker())

		val DARK_UNDEFINED_COLOR = UNDEFINED_COLOR.withBackground(UNDEFINED_COLOR.foregroundColor.darker())

		val DARK_ERROR_COLOR = CompositeColor(
			foregroundColor = Color.RED,
			backgroundColor = Color.RED.darker(),
			textColor = Color.WHITE)
	}
}