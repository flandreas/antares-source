package io.antarescircuit.antares.view.theme

import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.CompositeColor

object BlackAndWhiteThemeBuilder : AbstractAntaresLightThemeBuilder("Black & White") {

	private val BLACK_AND_WHITE = CompositeColor(
		foregroundColor = Color.BLACK,
		backgroundColor = Color.WHITE
	)

	override fun build(): AntaresTheme = standardForColor(BLACK_AND_WHITE)
}