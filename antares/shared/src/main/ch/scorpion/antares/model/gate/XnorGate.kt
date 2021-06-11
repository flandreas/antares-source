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
 * Performs a logical "XNOR" function with the current input signals of a [Vertice].
 */
class XnorCalculator : AbstractDigitalGateCalculator() {

	override fun calculate(source: MultiSignalSource<Bit>, filter: (Int) -> Boolean): Bit =
		XorCalculator.calculate(source, filter).not()
}

class XnorGate(inputCount: InputCount = InputCount.TWO) : AbstractDigitalGate(CALCULATOR, inputCount) {

	companion object {

		private const val XNOR_BASE_RESOURCE_KEY = "library.element.XnorGate"
		private val XNOR_TYPE get() = Translations.getString("$XNOR_BASE_RESOURCE_KEY.name")
		private val XNOR_TYPE_DESC get() = Translations.getOptionalString("$XNOR_BASE_RESOURCE_KEY.desc")

		private const val EVEN_BASE_RESOURCE_KEY = "library.element.EvenFunction"
		private val EVEN_TYPE get() = Translations.getString("$EVEN_BASE_RESOURCE_KEY.name")
		private val EVEN_TYPE_DESC get() = Translations.getOptionalString("$EVEN_BASE_RESOURCE_KEY.desc")


		val CALCULATOR = XnorCalculator()
	}

	override val type: String get() = if (inputCount == InputCount.TWO.count) XNOR_TYPE else EVEN_TYPE
	override val typeDesc: String? get() = if (inputCount == InputCount.TWO.count) XNOR_TYPE_DESC else EVEN_TYPE_DESC

	override fun createOutputPort(): OutputPort<DigitalSignal> = DigitalPortImpl.createOutput(Logic.NEGATIVE)
}
