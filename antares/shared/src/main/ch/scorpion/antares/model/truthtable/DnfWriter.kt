package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.dsl.*

interface DnfWriter {
	fun write(truthTable: TruthTable, ast: Node, outputColumn: Int) : String
}

abstract class AbstractDnfWriter(
	private val isNotPrefix: Boolean = false
) : DnfWriter {

	override fun write(truthTable: TruthTable, ast: Node, outputColumn: Int): String {
		val builder = StringBuilder()
		ast.accept(Visitor(builder))
		return "${truthTable.getColumnName(outputColumn)} = $builder"
	}

	abstract fun writeAnd(builder: StringBuilder)
	abstract fun writeOr(builder: StringBuilder)
	abstract fun writeConstant(constant: Boolean, builder: StringBuilder)
	abstract fun writeNot(builder: StringBuilder)

	private inner class Visitor(
		private val builder: StringBuilder
	) : EmptyHierarchyVisitor() {

		override fun visitEnter(node: Any): Boolean {
			when (node) {
				is UnaryOperation -> {
					when (node.op.type) {
						TokenType.NOT -> if (!isNotPrefix) {
							writeNot(builder)
						}
					}
				}
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
						TokenType.AND -> writeAnd(builder)
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
						TokenType.NOT -> if (isNotPrefix) {
							writeNot(builder)
						}
						else -> throw IllegalStateException("unsupported unary operation ${node.op.type}")
					}
				}
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
	isNotPrefix: Boolean = false
): AbstractDnfWriter(isNotPrefix) {

	companion object {

		/** Format: A * B' + A' * B + 0 */
		val ARITHMETIC = StandardDnfWriter(
			andOp = "*",
			orOp = "+",
			trueConst = "1",
			falseConst = "0",
			notOp = "'",
			isNotPrefix = true
		)

		val LOGIC = StandardDnfWriter(
			andOp = "∧",
			orOp = "∨",
			trueConst = "1",
			falseConst = "0",
			notOp = "¬",
			isNotPrefix = false
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