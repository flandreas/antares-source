package ch.scorpion.jabbah.draw.view.find

import ch.scorpion.jabbah.base.Translations

enum class SearchMatch {
	StartsWidth,
	Contains,
	EntireWord;

	override fun toString(): String =
		when (this) {
			StartsWidth -> Translations.getString("draw.search.match.startsWith")
			Contains ->  Translations.getString("draw.search.match.contains")
			EntireWord ->  Translations.getString("draw.search.match.entireWord")
		}
}