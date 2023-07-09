package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * Performs a logical "NOT" function with the current input signal of a [Vertice].
 */
class NotCalculator : AbstractLogicGateCalculator() {

	override fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit =
		calculateOutputBit(input.first().bitAt(bitIndex))

	override fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit =
		calculateOutputBit(input.getSignal(1).bitAt(0))

	private fun calculateOutputBit(inputBit: Bit): Bit =
		when (inputBit) {
			Bit.Undefined -> CurrentUndefinedGateInputBehavior.value.definedBit
			else -> inputBit.not()
		}
}

class NotGate(
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractLogicGate(CALCULATOR, PortCount.ONE, bitWidth, PortCount.ONE, PortCount.ONE) {

    companion object {
	    private const val BASE_RESOURCE_KEY = "library.element.NotGate"
	    private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
	    private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

	    val CALCULATOR = NotCalculator()
    }

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	override fun createOutputPort(): OutputPort<DigitalSignal> = DigitalPortImpl.createOutput(Logic.NEGATIVE)
}


