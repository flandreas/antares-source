package io.antarescircuit.antares.view.theme

import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.graph.view.style.GraphTheme

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