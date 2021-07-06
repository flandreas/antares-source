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
			var undefined = false
			var result = false
			for (portId in (1..source.signalCount).filter { filter(it) }) {
				when (source.getSignal(portId)) {
					True -> result = true
					False -> {}
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

			if (result) {
				return True
			}

			if (undefined) {
				return False
			}

			return Bit.of(false)
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
