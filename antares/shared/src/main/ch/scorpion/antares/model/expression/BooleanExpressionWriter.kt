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

	fun getOutput(truthTable: TruthTable, outputColumn: Int): String
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
		val output = getOutput(truthTable, outputColumn)
		builder.append("$output = ")
		ast.accept(Visitor(builder, omitAndForSingleCharacterVariables && truthTable.allInputNamesAreSingleChar))
		return builder.toString()
	}

	override fun getOutput(truthTable: TruthTable, outputColumn: Int): String {
		val builder = StringBuilder()
		val info = truthTable.getOutputColumnInfo(outputColumn)
		if (info.isNegated) {
			if (isNotPostfix) {
				builder.append(info.plainName)
				writeNot(builder)
			} else {
				writeNot(builder)
				builder.append(info.plainName)
			}
		} else {
			builder.append(info.plainName)
		}
		return builder.toString()
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
	private val notation: BooleanExpressionNotation
): AbstractBooleanExpressionWriter(notation.isNotPostfix) {

	companion object {

		val ARITHMETIC = StandardBooleanExpressionWriter(BooleanExpressionNotation.ARITHMETIC)
		val LOGIC = StandardBooleanExpressionWriter(BooleanExpressionNotation.LOGIC)
		val PROGRAMMING = StandardBooleanExpressionWriter(BooleanExpressionNotation.PROGRAMMING)
		val VERBOSE = StandardBooleanExpressionWriter(BooleanExpressionNotation.VERBOSE)

		fun ofNotation(notation: BooleanExpressionNotation): BooleanExpressionWriter =
			when (notation) {
				BooleanExpressionNotation.ARITHMETIC -> ARITHMETIC
				BooleanExpressionNotation.LOGIC -> LOGIC
				BooleanExpressionNotation.PROGRAMMING -> PROGRAMMING
				BooleanExpressionNotation.VERBOSE -> VERBOSE
			}

		fun ofPropertiesNotation(): BooleanExpressionWriter = ofNotation(BooleanExpressionNotation.fromProperties())
	}

	override fun writeAnd(builder: StringBuilder) {
		builder.append(" ${notation.andOp} ")
	}

	override fun writeOr(builder: StringBuilder) {
		builder.append(" ${notation.orOp} ")
	}

	override fun writeNot(builder: StringBuilder) {
		builder.append(notation.notOp)
	}

	override fun writeConstant(constant: Boolean, builder: StringBuilder) {
		if (constant) {
			builder.append(notation.trueConst)
		} else {
			builder.append(notation.falseConst)
		}
	}
}