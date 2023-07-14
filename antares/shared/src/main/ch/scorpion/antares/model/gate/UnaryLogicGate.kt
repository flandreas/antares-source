package ch.scorpion.antares.model.gate

import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.gate.UnaryLogicGateType.Buffer
import ch.scorpion.antares.model.gate.UnaryLogicGateType.Not
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.help.HelpId

/**
 * The supported types of [UnaryLogicGate]s with only 1 [InputPort].
 */
enum class UnaryLogicGateType(
	override val outputLogic: Logic,
	override val calculator: AbstractLogicGateCalculator,
	override val helpId: HelpId,
	private val baseResourceKey: String
): LogicGateType {

	Not(Logic.NEGATIVE, NotCalculator, HelpId("NotGate"), "library.element.NotGate"),
	Buffer(Logic.POSITIVE, BufferCalculator, HelpId("BufferGate"), "library.element.Buffer");

	fun getType(): String = Translations.getString("$baseResourceKey.name")

	fun getTypeDesc(): String? = Translations.getOptionalString("$baseResourceKey.desc")
}

class UnaryLogicGate(
	val gateType: UnaryLogicGateType,
	bitWidth: BitWidth = BitWidth.BW_1
): AbstractLogicGate(gateType, PortCount.ONE, bitWidth, PortCount.ONE, PortCount.ONE) {

	companion object {
		fun notGate(): UnaryLogicGate = UnaryLogicGate(Not)
		fun bufferGate(): UnaryLogicGate = UnaryLogicGate(Buffer)
	}

	override val type: String get() = gateType.getType()

	override val typeDesc: String? get() = gateType.getTypeDesc()
}