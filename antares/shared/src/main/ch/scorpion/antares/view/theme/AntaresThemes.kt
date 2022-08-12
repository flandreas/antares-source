package ch.scorpion.antares.view.theme

import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.style.GraphTheme

/**
 * Defines [GraphTheme]s for the Antares application.
 */
object AntaresThemes {

	fun install(themeToUse: String? = null) {
		themeToUse?.let { Themes.store(it) }

		Themes.register(
			WinterThemeBuilder.build(),
			DesertThemeBuilder.build(),
			BlackAndWhiteThemeBuilder.build(),
			DarculaThemeBuilder.build(),
			CrtThemeBuilder.build())
	}
}