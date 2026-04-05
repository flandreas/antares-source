package io.antarescircuit.antares.model.net

import io.antarescircuit.jabbah.base.Translations

enum class TransistorType(val customName: String) {
	N("n"),
	P("p");

	companion object {
		fun withName(customName: String): TransistorType =
			values().firstOrNull { it.customName == customName } ?:
				throw IllegalArgumentException("Unknown TransistorType '$customName'")
	}

	override fun toString(): String {
		return when (this) {
			N -> Translations.getString("element.property.transistorType.n.name")
			P -> Translations.getString("element.property.transistorType.p.name")
		}
	}
}