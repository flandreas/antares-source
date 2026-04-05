package io.antarescircuit.jabbah.graph.view.port

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.graph.view.VerticeView

/**
 * Determines the position of a [PortView] label in relation to the owning [VerticeView].
 */
enum class PortLabelPosition(val customName: String) {

	HIDE("hide"),
	INTERNAL("internal"),
	EXTERNAL("external");

	companion object {

		fun withName(customName: String): PortLabelPosition {
			for (pos in values()) {
				if (pos.customName == customName) {
					return pos
				}
			}
			throw IllegalArgumentException("unknown PortLabelPosition $customName")
		}
	}

	override fun toString(): String {
		return when (this) {
			HIDE -> Translations.getString("graph.property.PortLabelPosition.hide.name")
			EXTERNAL -> Translations.getString("graph.property.PortLabelPosition.external.name")
			INTERNAL -> Translations.getString("graph.property.PortLabelPosition.internal.name")
		}
	}
}