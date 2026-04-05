package io.antarescircuit.antares.model.gate

import io.antarescircuit.jabbah.graph.model.InputPort
import io.antarescircuit.antares.model.Logic
import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.gate.UnaryLogicGateType.Buffer
import io.antarescircuit.antares.model.gate.UnaryLogicGateType.Not
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.graph.model.Vertice

/**
 * The supported types of [UnaryLogicGate]s with only 1 [InputPort].
 */
enum class UnaryLogicGateType(
	override val outputLogic: Logic,
	override val calculator: AbstractLogicGateCalculator,
	override val helpId: HelpId,
	override val baseResourceKey: String
): LogicGateType {

	Not(Logic.NEGATIVE, NotCalculator, HelpId("NotGate"), "library.element.NotGate"),
	Buffer(Logic.POSITIVE, BufferCalculator, HelpId("BufferGate"), "library.element.Buffer");

	override fun getName(gate: Vertice): String = Translations.getString("$baseResourceKey.name")

	override fun getDescription(gate: Vertice): String? = Translations.getOptionalString("$baseResourceKey.desc")
}

class UnaryLogicGate(
	gateType: UnaryLogicGateType,
	bitWidth: BitWidth = BitWidth.BW_1
): AbstractLogicGate(gateType, PortCount.ONE, bitWidth, PortCount.ONE, PortCount.ONE) {

	companion object {
		fun notGate(bitWidth: BitWidth = BitWidth.BW_1): UnaryLogicGate = UnaryLogicGate(Not, bitWidth)
		fun bufferGate(): UnaryLogicGate = UnaryLogicGate(Buffer)
	}

	override val type: String get() = gateType.getName(this)

	override val typeDesc: String? get() = gateType.getDescription(this)
}