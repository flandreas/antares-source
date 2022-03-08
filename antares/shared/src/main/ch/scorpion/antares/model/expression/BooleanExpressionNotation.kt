package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.module.BaseModule

enum class BooleanExpressionNotation(
	val customName: String,
	val dnfWriter: BooleanExpressionWriter,
	private val sample: String
) {

	ARITHMETIC("arithmetic", StandardBooleanExpressionWriter.ARITHMETIC, "(A’ * B) + (A * B’) + 0"),
	LOGIC("logic", StandardBooleanExpressionWriter.LOGIC, "(A ∧ ¬B) ∨ (¬A ∧ B) ∨ 0"),
	PROGRAMMING("programming", StandardBooleanExpressionWriter.PROGRAMMING, "(A && !B) || (!A && B) || false"),
	VERBOSE("verbose", StandardBooleanExpressionWriter.VERBOSE, "(A AND NOT B) OR (NOT A AND B) OR false");

	companion object {

		const val PROP_NOTATION = "antares.expression.notation"
		const val PROP_OMIT_AND = "antares.expression.omitAnd"
		const val PROP_AND_PARENTHESIS = "antares.expression.andParenthesis"

		fun withName(customName: String): BooleanExpressionNotation =
			values().firstOrNull { it.customName == customName } ?:
				throw IllegalArgumentException("Unknown BooleanExpressionNotation '$customName'")

		fun fromProperties(): BooleanExpressionNotation = withName(BaseModule.properties.getString(PROP_NOTATION))
	}

	override fun toString(): String = sample
}