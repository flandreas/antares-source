package ch.scorpion.antares.model.expression

import ch.scorpion.antares.model.truthtable.DnfWriter
import ch.scorpion.antares.model.truthtable.StandardDnfWriter
import ch.scorpion.jabbah.base.module.BaseModule

enum class BooleanExpressionNotation(
	val customName: String,
	val dnfWriter: DnfWriter,
	private val sample: String
) {

	ARITHMETIC("arithmetic", StandardDnfWriter.ARITHMETIC, "A’B + AB’ + 0"),
	LOGIC("logic", StandardDnfWriter.LOGIC, "A ∧ ¬B ∨ ¬A ∧ B ∨ 0");

	companion object {

		const val PROP_NOTATION = "antares.expression.notation"

		fun withName(customName: String): BooleanExpressionNotation =
			values().firstOrNull { it.customName == customName } ?:
				throw IllegalArgumentException("Unknown BooleanExpressionNotation '$customName'")

		fun fromProperties(): BooleanExpressionNotation = withName(BaseModule.properties.getString(PROP_NOTATION))
	}

	override fun toString(): String = sample
}