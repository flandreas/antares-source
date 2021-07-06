package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.Vertice

/** Performs a logical "AND" function with the current input signals of a [Vertice].*/
class AndCalculator : AbstractDigitalGateCalculator() {

	companion object {

		fun calculate(source: MultiSignalSource<Bit>, filter: (Int) -> Boolean = { true }): Bit {
			var undefined = false
			var trueCount = 0
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

			return Bit.of(trueCount == source.signalCount)
		}
	}

	override fun calculate(source: MultiSignalSource<Bit>, filter: (Int) -> Boolean): Bit {
		return Companion.calculate(source, filter)
	}
}

/** A digital gate that performs a logical AND operation. */
class AndGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.AndGate"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		val CALCULATOR = AndCalculator()
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	fun calculate(portFilter: (Int) -> Boolean): Bit = AndCalculator.calculate(this, portFilter)
}
