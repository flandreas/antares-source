package ch.scorpion.antares.model.gate

import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.PortCount.TWO
import ch.scorpion.antares.model.gate.NonUnaryLogicGateType.*
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_1
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations

/**
 * The supported types of [NonUnaryLogicGate] with 2 or more [InputPort]s.
 */
enum class NonUnaryLogicGateType(
	override val outputLogic: Logic,
	override val calculator: AbstractLogicGateCalculator,
	private val baseResourceKey: String,
	private val moreThanTwoPortBaseResourceKey: String? = null
): LogicGateType {
	And(Logic.POSITIVE, AndCalculator, "library.element.AndGate"),
	Nand(Logic.NEGATIVE, NandCalculator, "library.element.NandGate"),
	Or(Logic.POSITIVE, OrCalculator, "library.element.OrGate"),
	Nor(Logic.NEGATIVE, NorCalculator, "library.element.NorGate"),
	Xor(Logic.POSITIVE, XorCalculator, "library.element.XorGate", "library.element.OddFunction"),
	Xnor(Logic.NEGATIVE, XnorCalculator, "library.element.XnorGate", "library.element.EvenFunction");

	fun getType(gate: NonUnaryLogicGate): String =
		if (gate.inputCount > PortCount.TWO.count && moreThanTwoPortBaseResourceKey != null) {
			Translations.getString("$moreThanTwoPortBaseResourceKey.name")
		} else {
			Translations.getString("$baseResourceKey.name")
		}

	fun getTypeDesc(gate: NonUnaryLogicGate): String? =
		if (gate.inputCount > PortCount.TWO.count && moreThanTwoPortBaseResourceKey != null) {
			Translations.getOptionalString("$moreThanTwoPortBaseResourceKey.desc")
		} else {
			Translations.getOptionalString("$baseResourceKey.desc")
		}
}

class NonUnaryLogicGate(
	val gateType: NonUnaryLogicGateType,
	inputCount: PortCount = TWO,
	bitWidth: BitWidth = BW_1
): AbstractLogicGate(gateType, inputCount, bitWidth) {

	companion object {
		fun andGate(): NonUnaryLogicGate = NonUnaryLogicGate(And)
		fun nandGate(): NonUnaryLogicGate = NonUnaryLogicGate(Nand)
		fun orGate(): NonUnaryLogicGate = NonUnaryLogicGate(Or)
		fun norGate(): NonUnaryLogicGate = NonUnaryLogicGate(Nor)
		fun xorGate(): NonUnaryLogicGate = NonUnaryLogicGate(Xor)
		fun xnorGate(): NonUnaryLogicGate = NonUnaryLogicGate(Xnor)
	}

	override val type: String get() = gateType.getType(this)

	override val typeDesc: String? get() = gateType.getTypeDesc(this)

	fun calculate(portFilter: (Int) -> Boolean): DigitalSignal = if (bitWidth.width == BW_1.width) {
		AndCalculator.calculateSingleBit(this, portFilter)
	} else {
		AndCalculator.calculateMultiBit(this, portFilter)
	}
}