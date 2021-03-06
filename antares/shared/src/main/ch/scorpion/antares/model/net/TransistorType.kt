package ch.scorpion.antares.model.net

import ch.scorpion.jabbah.base.Translations

enum class TransistorType(val customName: String) {
	P("p"),
	N("n");

	companion object {
		fun withName(customName: String): TransistorType =
			values().firstOrNull { it.customName == customName } ?:
				throw IllegalArgumentException("Unknown TransistorType '$customName'")
	}

	override fun toString(): String {
		return when (this) {
			P -> Translations.getString("element.property.transistorType.p.name")
			N -> Translations.getString("element.property.transistorType.n.name")
		}
	}
}