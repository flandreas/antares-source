package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * Performs a logical "XOR" function with the current input signals of a [Vertice].
 */
class XorCalculator : AbstractDigitalGateCalculator() {

	companion object {

		fun calculate(source: MultiSignalSource<Bit>, filter: (Int) -> Boolean = { true }): Bit {
			var trueCount = 0
			var undefined = false

			for (portId in (1..source.signalCount).filter { filter(it) }) {
				@Suppress("NON_EXHAUSTIVE_WHEN")
				when (source.getSignal(portId)) {
					True -> trueCount++
					Error -> return Error
					Undefined -> undefined = true
				}
			}

			if (undefined) {
				when (CurrentOpenGateInputBehaviour.value) {
					OpenGateInputBehavior.Accept -> {}
					OpenGateInputBehavior.Random -> return Bit.random()
					OpenGateInputBehavior.Error -> return Error
				}
			}

			return Bit.of(trueCount.rem(2) == 1)
		}
	}

	override fun calculate(source: MultiSignalSource<Bit>, filter: (Int) -> Boolean): Bit =
		Companion.calculate(source, filter)
}

class XorGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

	companion object {

		private const val XOR_BASE_RESOURCE_KEY = "library.element.XorGate"
		private val XOR_TYPE get() = Translations.getString("$XOR_BASE_RESOURCE_KEY.name")
		private val XOR_TYPE_DESC get() = Translations.getOptionalString("$XOR_BASE_RESOURCE_KEY.desc")

		private const val ODD_BASE_RESOURCE_KEY = "library.element.OddFunction"
		private val ODD_TYPE get() = Translations.getString("$ODD_BASE_RESOURCE_KEY.name")
		private val ODD_TYPE_DESC get() = Translations.getOptionalString("$ODD_BASE_RESOURCE_KEY.desc")


		val CALCULATOR = XorCalculator()
	}

	override val type: String get() = if (inputCount == InputCount.TWO.count) XOR_TYPE else ODD_TYPE
	override val typeDesc: String? get() = if (inputCount == InputCount.TWO.count) XOR_TYPE_DESC else ODD_TYPE_DESC
}
