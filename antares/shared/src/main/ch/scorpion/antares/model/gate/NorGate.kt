package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.InputCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * Performs a logical "NOR" function with the current input signals of a [Vertice].
 */
class NorCalculator : AbstractDigitalGateCalculator() {
	override fun calculate(source: MultiSignalSource<Bit>, filter: (Int) -> Boolean): Bit =
		OrCalculator.calculate(source, filter).not()
}

class NorGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.NorGate"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		val CALCULATOR = NorCalculator()
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	override fun createOutputPort(): OutputPort<DigitalSignal> =
		DigitalPortImpl.createOutput(Logic.NEGATIVE)
}

