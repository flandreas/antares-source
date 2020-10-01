package ch.scorpion.antares.view.theme

import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.style.GraphTheme

/**
 * Defines [GraphTheme]s for the Antares application.
 */
object AntaresThemes {

	fun install() {
		Themes.register(
			WinterThemeBuilder.build(),
			BlackAndWhiteThemeBuilder.build(),
			DarculaThemeBuilder.build(),
			CrtThemeBuilder.build())
	}
}