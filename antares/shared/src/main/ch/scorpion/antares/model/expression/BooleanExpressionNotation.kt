package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.module.BaseModule

enum class BooleanExpressionNotation(
	val customName: String,
	private val sample: String,
	val andOp: String,
	val orOp: String,
	val notOp: String,
	val trueConst: String,
	val falseConst: String,
	val isNotPostfix: Boolean
) {

	ARITHMETIC(
		"arithmetic",
		"(A’ * B) + (A * B’) + 0",
		"*",
		"+",
		"'",
		"1",
		"0",
		true
	),

	LOGIC(
		"logic",
		"(A ∧ ¬B) ∨ (¬A ∧ B) ∨ 0",
		"∧",
		"∨",
		"¬",
		"1",
		"0",
		false
	),

	PROGRAMMING(
		"programming",
		"(A && !B) || (!A && B) || false",
		"&&",
		"||",
		"!",
		"1",
		"0",
		false
	),

	VERBOSE(
		"verbose",
		"(A AND NOT B) OR (NOT A AND B) OR false",
		"AND",
		"OR",
		"NOT ", // Intentional trailing blank
		"true",
		"false",
		false
	);

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