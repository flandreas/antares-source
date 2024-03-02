package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations

enum class LibraryVisibility(val customName: String) {
	Private("private"),
	Hidden("hidden"),
	Public("public");

	companion object {
		fun withName(customName: String): LibraryVisibility =
			entries.firstOrNull { it.customName == customName}
				?: throw IllegalArgumentException("unknown LibraryVisibility '$customName'")
	}

	override fun toString(): String = when (this) {
		Private -> Translations.getString("library.property.visibility.private.name")
		Hidden -> Translations.getString("library.property.visibility.hidden.name")
		Public -> Translations.getString("library.property.visibility.public.name")
	}

	fun description(): String = when (this) {
		Private -> Translations.getString("library.property.visibility.private.desc")
		Hidden -> Translations.getString("library.property.visibility.hidden.desc")
		Public -> Translations.getString("library.property.visibility.public.desc")
	}
}