package ch.scorpion.antares.view.synthesis

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.antares.model.truthtable.TruthTable

/** Defines the type of circuits than can be built from a [TruthTable]. */
enum class CircuitSynthesisType(
	private val baseKey: String,
	val build: CircuitFromTruthTableBuilder
) {
	AndOrGate(
		"antares.synthesis.type.andOr",
		{ truthTable, dnfs, graphStorable -> AndOrCircuitFromTruthTableBuilder(truthTable, dnfs, graphStorable).build() }
	),

	LookupTable(
		"antares.synthesis.type.lut",
		{ truthTable, dnfs, graphStorable -> LookupTableCircuitFromTruthTableBuilder(truthTable, dnfs, graphStorable).build() }
	);

	override fun toString(): String = Translations.getString("$baseKey.name")
}