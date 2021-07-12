package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * Performs a logical "OR" function with the current input signals of a [Vertice].
 */
class OrCalculator : AbstractDigitalGateCalculator() {

	companion object {

		fun calculate(source: MultiSignalSource<Bit>, filter: (Int) -> Boolean = { true }): Bit {
			var result = false
			for (portId in (1..source.signalCount).filter { filter(it) }) {
				@Suppress("NON_EXHAUSTIVE_WHEN")
				when (effectiveGateInputValue(portId, source)) {
					True -> result = true
					Error -> return Error
				}
			}
			return Bit.of(result)
		}
	}

	override fun calculate(source: MultiSignalSource<Bit>, filter: (Int) -> Boolean): Bit =
		Companion.calculate(source, filter)
}

class OrGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.OrGate"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		val CALCULATOR = OrCalculator()
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC
}
