package ch.scorpion.antares.view.theme

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor

object BlackAndWhiteThemeBuilder : AbstractAntaresLightThemeBuilder("Black & White") {

	private val BLACK_AND_WHITE = CompositeColor(
		foregroundColor = Color.BLACK,
		backgroundColor = Color.WHITE
	)

	override fun build(): AntaresTheme = standardForColor(BLACK_AND_WHITE)
}