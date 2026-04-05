package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.base.EnumProperty
import io.antarescircuit.jabbah.base.Translations

enum class InternalLabelOrientation(
	override val customName: String
) : EnumProperty<InternalLabelOrientation> {

	Horizontal("horizontal"),
	Aligned("aligned");

	companion object {
		fun withName(name: String): InternalLabelOrientation =
			values().firstOrNull { it.customName == name }
				?: throw IllegalArgumentException("unknown InternalLabelOrientation")
	}

	override fun toString(): String {
		return when (this) {
			Horizontal -> Translations.getString("graph.property.internalLabelOrientation.horizontal")
			Aligned -> Translations.getString("graph.property.internalLabelOrientation.aligned")
		}
	}
}