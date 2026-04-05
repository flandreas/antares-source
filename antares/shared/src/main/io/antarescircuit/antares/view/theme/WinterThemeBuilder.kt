package io.antarescircuit.antares.view.theme

import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.CompositeColor

object WinterThemeBuilder : AbstractAntaresLightThemeBuilder("Winter") {

	private val SKY_BLUE = CompositeColor(
		foregroundColor = Color(69, 113, 180),
		backgroundColor = Color(220, 237, 250)
	)

	override fun build(): AntaresTheme = standardForColor(SKY_BLUE)
}