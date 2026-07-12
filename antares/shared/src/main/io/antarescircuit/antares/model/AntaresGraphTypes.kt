package io.antarescircuit.antares.model

import io.antarescircuit.antares.model.analog.AnalogOscilloscopeProbeVertice
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.model.signal.Digital2AnalogAdapter
import io.antarescircuit.antares.model.signal.Digital2DigitalAdapter
import io.antarescircuit.antares.model.signal.DigitalSignalFactory
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.model.GraphTypeSignalAdapter
import io.antarescircuit.jabbah.graph.model.image.ImageLibraryElement
import io.antarescircuit.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice

enum class AntaresGraphTypes(
	override val customName: String,
	override val needsGraphViewForExecution: Boolean,
	override val isCombiningNets: Boolean
) : GraphType {

	Digital("digital", false, true) {

		override val supportOscilloscopeSignalCurveStyleSelection: Boolean get() = false

		@Suppress("UNCHECKED_CAST")
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

		@Suppress("UNCHECKED_CAST")
		override fun <T : Any> createOscilloscopeProbeVertice(name: String?): OscilloscopeProbeVertice<T> =
			AnalogOscilloscopeProbeVertice() as OscilloscopeProbeVertice<T>
	};

	override fun toString(): String =
		when (this) {
			Digital -> Translations.getString("antares.graphType.digital")
			Analog -> Translations.getString("antares.graphType.analog")
		}

	override fun checkImport(other: GraphType): String? {
		if (this === Analog) {
			return Translations.getString("graph.subGraphTypeError.msg", this)
		}
		if (this === Digital && other === Digital) {
			return null
		}
		val sourceType = entries.firstOrNull { it === other }
		return if (sourceType == null || this.ordinal != sourceType.ordinal - 1) {
			Translations.getString("graph.graphTypeError.msg", other, this)
		} else {
			null
		}
	}

	override fun checkImport(libraryElement: LibraryElement): String? {
		if (libraryElement is ContainerLibraryElement) {
			return checkImport(libraryElement.graphType)
		}

		if (this === libraryElement.graphType) {
			return null
		}

		if (libraryElement is ImageLibraryElement) {
			return null
		}

		return Translations.getString("graph.graphTypeError.msg", libraryElement.graphType, this)
	}
}