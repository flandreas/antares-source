package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.GraphType

enum class AntaresGraphTypes(
	override val customName: String
) : GraphType {
	Digital("digital"),
	Analog("analog");

	override fun toString(): String =
		when (this) {
			Digital -> Translations.getString("antares.graphType.digital")
			Analog -> Translations.getString("antares.graphType.analog")
		}
}