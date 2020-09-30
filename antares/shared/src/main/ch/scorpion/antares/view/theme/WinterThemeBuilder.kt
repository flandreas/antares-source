package ch.scorpion.antares.view.theme

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor

object WinterThemeBuilder : AbstractAntaresLightThemeBuilder("Winter") {

	private val SKY_BLUE = CompositeColor(
		foregroundColor = Color(69, 113, 180),
		backgroundColor = Color(220, 237, 250)
	)

	override fun build(): AntaresTheme = standardForColor(SKY_BLUE)
}