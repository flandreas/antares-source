package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryElement
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

	override fun canImport(libraryElement: LibraryElement): Boolean {
		if (libraryElement is ContainerLibraryElement) {
			if (this === Analog) {
				return false
			}
			return values().firstOrNull { it === libraryElement.graphType }?.let {
				this.ordinal == it.ordinal - 1
			} ?: false
		}

		if (this === libraryElement.graphType) {
			return true
		}

		return false
	}
}