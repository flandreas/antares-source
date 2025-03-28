package ch.scorpion.antares.view.synthesis

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.antares.model.truthtable.TruthTable

/**
 * Defines the type of circuits than can be built from a [TruthTable].
 * @property supportFSM `true` if synthesis supports creation of finite state machines (sequential circuits)
 */
enum class CircuitSynthesisType(
	private val baseKey: String,
	val supportFSM: Boolean,
	val build: CircuitFromTruthTableBuilder
) {
	AndOrGate(
		"antares.synthesis.type.andOr",
		true,
		{ truthTable, dnfs, graphStorable -> AndOrCircuitFromTruthTableBuilder(truthTable, dnfs, graphStorable).build() }
	),

	LookupTable(
		"antares.synthesis.type.lut",
		false,
		{ truthTable, dnfs, graphStorable -> LookupTableCircuitFromTruthTableBuilder(truthTable, dnfs, graphStorable).build() }
	);

	override fun toString(): String = Translations.getString("$baseKey.name")
}