package ch.scorpion.antares.model.gate

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.graph.model.MultiSignalSource
import ch.scorpion.jabbah.graph.model.OutputPort

/**
 * Performs a NAND calculation by inverting the result of a [AndCalculator].
 */
class NandCalculator : AbstractDigitalGateCalculator() {

	private val andCalculator = AndCalculator()

	override fun calculateBit(input: Collection<DigitalSignal>, bitIndex: Int): Bit =
		andCalculator.calculateBit(input, bitIndex).not()

	override fun calculateBit(input: MultiSignalSource<DigitalSignal>): Bit =
		andCalculator.calculateBit(input).not()
}

class NandGate(
	inputCount: PortCount = PortCount.TWO,
	bitWidth: BitWidth = BitWidth.BW_1
) : AbstractDigitalGate(CALCULATOR, inputCount, bitWidth) {

	companion object {
		private const val BASE_RESOURCE_KEY = "library.element.NandGate"
		private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
		private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")

		val CALCULATOR = NandCalculator()
	}

	override val type: String get() = TYPE
	override val typeDesc: String? get() = TYPE_DESC

	override fun createOutputPort(): OutputPort<DigitalSignal> = DigitalPortImpl.createOutput(Logic.NEGATIVE)
}
