package ch.scorpion.antares.model.expression

import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.jabbah.base.dsl.Node
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Creates a textual representation of a [BooleanExpression] (represented by its root AST node)
 * that was generated from the specified [TruthTable], containing the variable names.
 */
interface BooleanExpressionWriter {

	/**
	 * Creates the textual representation.
	 *
	 * @param truthTable the [TruthTable] containing variable names
	 * @param ast contains the abstract syntax tree root
	 * @param outputColumn the index of the output column in [TruthTable] to be written
	 * @param omitAndForSingleCharacterVariables `true` if AND operators are generally to be omitted
	 * if all variables of the expression consists of single characters. With longer variable names,
	 * variable names wouldn't be distinguishable anymore without AND operators
	 */
	fun write(
		truthTable: TruthTable,
		ast: Node,
		outputColumn: Int,
		omitAndForSingleCharacterVariables: Boolean = BaseModule.properties.getBoolean(BooleanExpressionNotation.PROP_OMIT_AND)
	) : String
}

/**
 * Base class for implementing [BooleanExpressionWriter].
 *
 * @property isNotPostfix `true` if NOT operators are to be written as postfix, i.e. after to
 * the negated factor
 */
abstract class AbstractBooleanExpressionWriter(
	private val isNotPostfix: Boolean = false
) : BooleanExpressionWriter {

	abstract fun writeAnd(builder: StringBuilder)
	abstract fun writeOr(builder: StringBuilder)
	abstract fun writeConstant(constant: Boolean, builder: StringBuilder)
	abstract fun writeNot(builder: StringBuilder)

	override fun write(
		truthTable: TruthTable,
		ast: Node,
		outputColumn: Int,
		omitAndForSingleCharacterVariables: Boolean
	): String {
		val builder = StringBuilder()
		ast.accept(Visitor(builder, omitAndForSingleCharacterVariables && truthTable.allInputNamesAreSingleChar))
		return "${truthTable.getColumnName(outputColumn)} = $builder"
	}

	private inner class Visitor(
		private val builder: StringBuilder,
		private val omitAndForSingleCharacterVariables: Boolean
	) : AbstractBooleanExpressionVisitor(isNotPostfix) {

		override fun handleAnd() {
			if (!omitAndForSingleCharacterVariables) {
				writeAnd(builder)
			}
		}

		override fun handleOr() {
			writeOr(builder)
		}

		override fun handleConstant(value: Boolean) {
			writeConstant(value, builder)
		}

		override fun handleNot() {
			writeNot(builder)
		}

		override fun handleVariable(name: String) {
			builder.append(name)
		}

		override fun handleCompound(begin: Boolean) {
			if (begin) {
				builder.append('(')
			} else {
				builder.append(')')
			}
		}
	}
}

class StandardBooleanExpressionWriter(
	private val andOp: String = "*",
	private val orOp: String = "+",
	private val trueConst: String = "1",
	private val falseConst: String = "0",
	private val notOp: String = "'",
	isNotPostfix: Boolean = false
): AbstractBooleanExpressionWriter(isNotPostfix) {

	companion object {

		/** Format: A * B' + A' * B + 0 */
		val ARITHMETIC = StandardBooleanExpressionWriter(
			andOp = "*",
			orOp = "+",
			trueConst = "1",
			falseConst = "0",
			notOp = "'",
			isNotPostfix = true
		)

		val LOGIC = StandardBooleanExpressionWriter(
			andOp = "∧",
			orOp = "∨",
			trueConst = "1",
			falseConst = "0",
			notOp = "¬",
			isNotPostfix = false
		)

		val PROGRAMMING = StandardBooleanExpressionWriter(
			andOp = "&&",
			orOp = "||",
			trueConst = "1",
			falseConst = "0",
			notOp = "!",
			isNotPostfix = false
		)

		val VERBOSE = StandardBooleanExpressionWriter(
			andOp = "AND",
			orOp = "OR",
			trueConst = "true",
			falseConst = "false",
			notOp = "NOT ",
			isNotPostfix = false
		)
	}

	override fun writeAnd(builder: StringBuilder) {
		builder.append(" $andOp ")
	}

	override fun writeOr(builder: StringBuilder) {
		builder.append(" $orOp ")
	}

	override fun writeNot(builder: StringBuilder) {
		builder.append(notOp)
	}

	override fun writeConstant(constant: Boolean, builder: StringBuilder) {
		if (constant) {
			builder.append(trueConst)
		} else {
			builder.append(falseConst)
		}
	}
}