package ch.scorpion.antares.view

import ch.scorpion.jabbah.base.Translations

enum class PortViewSpacing(val customName: String, val value: Int) {

	Narrow("narrow", 2 * Look.SCALE),
	Wide("wide", 4 * Look.SCALE);

	companion object {
		fun withName(customName: String): PortViewSpacing =
			values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown PortViewSpacing '$customName'")
	}

	override fun toString(): String =
		when (this) {
			Narrow -> Translations.getString("element.property.portViewSpacing.narrow.name")
			Wide -> Translations.getString("element.property.portViewSpacing.wide.name")
		}
}