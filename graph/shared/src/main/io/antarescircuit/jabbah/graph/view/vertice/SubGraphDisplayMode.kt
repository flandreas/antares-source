package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.EnumProperty
import io.antarescircuit.jabbah.base.Translations

enum class SubGraphDisplayMode(override val customName: String) : EnumProperty<SubGraphDisplayMode> {

	SYMBOL("symbol"),
	GRAPH("graph");

	companion object {
		fun withName(customName: String): SubGraphDisplayMode {
			return entries.firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("Unknown display mode: $customName")
		}
	}

	override fun toString(): String = Translations.getString("graph.property.displayMode.$customName")
}
