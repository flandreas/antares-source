package ch.scorpion.antares.model.truthtable

import ch.scorpion.jabbah.base.EmptyHierarchyVisitor
import ch.scorpion.jabbah.base.dsl.*

class DnfWriter(
	private val truthTable: TruthTable,
	private val ast: Node
) {

	fun write(outputColumn: Int): String {
		val builder = StringBuilder()
		ast.accept(Visitor(builder))
		return "${truthTable.getColumnName(outputColumn)} = $builder"
	}

	private class Visitor(private val builder: StringBuilder) : EmptyHierarchyVisitor() {

		override fun visit(node: Any): Boolean {
			when (node) {
				is Variable -> {
					builder.append(node.token.value)
				}
				is Literal -> {
					when (node.token.value) {
						true -> builder.append("1")
						false -> builder.append("0")
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
						TokenType.OR -> builder.append(" + ")
						TokenType.AND -> builder.append(" * ")
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
						TokenType.NOT -> builder.append("'")
						else -> throw IllegalStateException("unsupported unary operation ${node.op.type}")
					}
				}
			}
			return true
		}
	}
}