package ch.scorpion.antares.model

import ch.scorpion.antares.model.analog.AnalogOscilloscopeProbeVertice
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.signal.Digital2AnalogAdapter
import ch.scorpion.antares.model.signal.Digital2DigitalAdapter
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryElement
import ch.scorpion.jabbah.graph.model.GraphType
import ch.scorpion.jabbah.graph.model.GraphTypeSignalAdapter
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice

enum class AntaresGraphTypes(
	override val customName: String,
	override val needsGraphViewForExecution: Boolean,
	override val isCombiningNets: Boolean
) : GraphType {

	Digital("digital", false, true) {

		override fun <I: Any, O: Any> adaptTo(other: GraphType): GraphTypeSignalAdapter<I, O> =
			when (other) {
				Analog -> Digital2AnalogAdapter as GraphTypeSignalAdapter<I, O>
				Digital -> Digital2DigitalAdapter as GraphTypeSignalAdapter<I, O>
				else -> throw IllegalArgumentException("Cannot adapt digital graph to $other")
			}

		override fun literalToSignal(literal: Any): Any =
			when (literal) {
				is Long -> DigitalSignalFactory.ofMinimalBitWidth(literal.toULong())
				is ULong -> DigitalSignalFactory.ofMinimalBitWidth(literal)
				else -> literal
			}
    },

	Analog("analog", true, false) {

		override fun <I: Any, O: Any> adaptTo(other: GraphType): GraphTypeSignalAdapter<I, O> {
			throw IllegalArgumentException("Cannot adapt analog graph to $other")
		}

		override fun literalToSignal(literal: Any): Any =
			when (literal) {
				is Float -> AnalogSignal(literal.toDouble())
				is Double -> AnalogSignal(literal)
				is Long -> AnalogSignal(literal.toDouble())
				else -> literal
			}

		override fun <T : Any> createOscilloscopeProbeVertice(name: String?): OscilloscopeProbeVertice<T> =
			AnalogOscilloscopeProbeVertice() as OscilloscopeProbeVertice<T>
	};

	override fun toString(): String =
		when (this) {
			Digital -> Translations.getString("antares.graphType.digital")
			Analog -> Translations.getString("antares.graphType.analog")
		}

	override fun checkImport(libraryElement: LibraryElement): String? {
		if (libraryElement is ContainerLibraryElement) {
			if (this === Analog) {
				return Translations.getString("graph.subGraphTypeError.msg", this)
			}
			if (this === Digital && libraryElement.graphType === Digital) {
				return null
			}
			val sourceType = values().firstOrNull { it === libraryElement.graphType }
			return if (sourceType == null || this.ordinal != sourceType.ordinal - 1) {
				Translations.getString("graph.graphTypeError.msg", libraryElement.graphType, this)
			} else {
				null
			}
		}

		if (this === libraryElement.graphType) {
			return null
		}

		return Translations.getString("graph.graphTypeError.msg", libraryElement.graphType, this)
	}
}