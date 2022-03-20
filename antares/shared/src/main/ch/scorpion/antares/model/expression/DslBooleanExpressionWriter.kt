package ch.scorpion.antares.model.expression

/**
 * Write a [BooleanExpression] as an Antares DSL expression string.
 */
class DslBooleanExpressionWriter : AbstractBooleanExpressionWriter(false) {

	override fun writeAnd(builder: StringBuilder) {
		builder.append(" and ")
	}

	override fun writeOr(builder: StringBuilder) {
		builder.append(" or ")
	}

	override fun writeConstant(constant: Boolean, builder: StringBuilder) {
		if (constant) {
			builder.append("1")
		} else {
			builder.append("0")
		}
	}

	override fun writeNot(builder: StringBuilder) {
		builder.append("not ") // Intentional trailing blank
	}
}