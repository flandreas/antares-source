package io.antarescircuit.antares.view.synthesis

import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Nand
import io.antarescircuit.antares.model.gate.NonUnaryLogicGateType.Nor
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.antares.model.truthtable.TruthTable

/**
 * Defines the type of circuits than can be built from a [TruthTable].
 * @property supportFSM `true` if synthesis supports creation of finite state machines (sequential circuits)
 * @property supportTwoInputGatesOnly `true` if synthesis can be restricted to gates with two inputs
 */
enum class CircuitSynthesisType(
	private val baseKey: String,
	val supportFSM: Boolean,
	val supportTwoInputGatesOnly: Boolean,
	val build: CircuitFromTruthTableBuilder
) {
	/** Sum of products */
	AndOrGate("antares.synthesis.type.andOr", gates(productOfSums = false, universalGate = null)),

	/** Product of sums */
	OrAndGate("antares.synthesis.type.orAnd", gates(productOfSums = true, universalGate = null)),

	/** NAND-NAND from the sum of products, inverters are NAND gates with tied inputs */
	NandGate("antares.synthesis.type.nand", gates(productOfSums = false, universalGate = Nand)),

	/** NOR-NOR from the product of sums, inverters are NOR gates with tied inputs */
	NorGate("antares.synthesis.type.nor", gates(productOfSums = true, universalGate = Nor)),

	LookupTable(
		"antares.synthesis.type.lut",
		false,
		false,
		{ truthTable, _, graphStorable -> LookupTableCircuitFromTruthTableBuilder(truthTable, graphStorable).build() }
	);

	constructor(baseKey: String, build: CircuitFromTruthTableBuilder) : this(baseKey, true, true, build)

	override fun toString(): String = Translations.getString("$baseKey.name")
}

private fun gates(productOfSums: Boolean, universalGate: NonUnaryLogicGateType?): CircuitFromTruthTableBuilder =
	{ truthTable, twoInputGatesOnly, graphStorable ->
		GateCircuitFromTruthTableBuilder(
			truthTable,
			GateNetworkFactory(productOfSums, universalGate, twoInputGatesOnly),
			graphStorable,
		).build()
	}
