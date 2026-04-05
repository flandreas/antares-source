package io.antarescircuit.antares.model.expression

import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.jabbah.base.dsl.BaseTokenType
import io.antarescircuit.jabbah.base.richtext.RichTextParser
import io.antarescircuit.jabbah.base.richtext.RichTextTokenType

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

	override fun getOutput(truthTable: TruthTable, outputColumn: Int): String {
		val info = truthTable.getOutputColumnInfo(outputColumn)
		return if (info.isNegated) {
			val negation = RichTextParser.negated(info.plainName)
			if (negation.startsWith(RichTextTokenType.OVERLINE.id)) {
				"${BaseTokenType.SINGLE_QUOTE.id}$negation${BaseTokenType.SINGLE_QUOTE.id}"
			} else {
				negation
			}
		} else {
			info.plainName
		}
	}
}