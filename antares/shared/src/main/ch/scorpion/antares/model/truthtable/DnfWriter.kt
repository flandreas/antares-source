package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.antares.model.expression.BooleanExpression
import ch.scorpion.antares.model.expression.BooleanExpressionNotation
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Creates a textual representation of a [BooleanExpression] (represented by its root AST node)
 * that was generated from the specified [TruthTable], containing the variable names.
 */
interface DnfWriter {

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
 * Base class for implementing [DnfWriter].
 *
 * @property isNotPostfix `true` if NOT operators are to be written as postfix, i.e. after to
 * the negated factor
 */
abstract class AbstractDnfWriter(
	private val isNotPostfix: Boolean = false,
) : DnfWriter {

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
	) : EmptyHierarchyVisitor() {

		override fun visitEnter(node: Any): Boolean {
			when (node) {
				is UnaryOperation -> {
					when (node.op.type) {
						TokenType.NOT -> if (!isNotPostfix) {
							writeNot(builder)
						}
					}
				}
				is Compound -> builder.append('(')
			}
			return true
		}

		override fun visit(node: Any): Boolean {
			when (node) {
				is Variable -> {
					builder.append(node.token.value)
				}
				is Literal -> {
					when (node.token.value) {
						true -> writeConstant(true, builder)
						false -> writeConstant(false, builder)
						else -> throw IllegalStateException("unsupported literal ${node.token.value}")
					}
				}

			}
			return true
		}

		override fun visitInfix(node: Any, child: Any): Boolean {
			when (node) {
				is BinaryOperation -> {
					when (node.op.type) {
						TokenType.OR -> writeOr(builder)
						TokenType.AND -> if (!omitAndForSingleCharacterVariables) {
							writeAnd(builder)
						}
						else -> throw IllegalStateException("unsupported binary operation ${node.op.type}")
					}
				}
			}
			return true
		}

		override fun visitLeave(node: Any): Boolean {
			when (node) {
				is UnaryOperation -> {
					when (node.op.type) {
						TokenType.NOT -> if (isNotPostfix) {
							writeNot(builder)
						}
						else -> throw IllegalStateException("unsupported unary operation ${node.op.type}")
					}
				}
				is Compound -> builder.append(')')
			}
			return true
		}
	}
}

class StandardDnfWriter(
	private val andOp: String = "*",
	private val orOp: String = "+",
	private val trueConst: String = "1",
	private val falseConst: String = "0",
	private val notOp: String = "'",
	isNotPostfix: Boolean = false
): AbstractDnfWriter(isNotPostfix) {

	companion object {

		/** Format: A * B' + A' * B + 0 */
		val ARITHMETIC = StandardDnfWriter(
			andOp = "*",
			orOp = "+",
			trueConst = "1",
			falseConst = "0",
			notOp = "'",
			isNotPostfix = true
		)

		val LOGIC = StandardDnfWriter(
			andOp = "∧",
			orOp = "∨",
			trueConst = "1",
			falseConst = "0",
			notOp = "¬",
			isNotPostfix = false
		)

		val PROGRAMMING = StandardDnfWriter(
			andOp = "&&",
			orOp = "||",
			trueConst = "1",
			falseConst = "0",
			notOp = "!",
			isNotPostfix = false
		)

		val VERBOSE = StandardDnfWriter(
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