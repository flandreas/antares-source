package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations

enum class LibraryVisibility(val customName: String) {
	Private("private"),
	Public("public");

	companion object {
		fun withName(customName: String): LibraryVisibility =
			values().firstOrNull { it.customName == customName}
				?: throw IllegalArgumentException("unknown LibraryVisibility '$customName'")
	}

	override fun toString(): String = when (this) {
		Private -> Translations.getString("library.property.visibility.private.name")
		Public -> Translations.getString("library.property.visibility.public.name")
	}
}