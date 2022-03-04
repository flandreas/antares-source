package ch.scorpion.antares.model.quinemccluskey

import ch.scorpion.antares.model.expression.BooleanExpression
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.jabbah.base.dsl.Node
import kotlin.math.abs

/**
 * Converts the output of Quine-McCluskey's [minimizeToDNF] to [BooleanExpression].
 */
class QmcToBooleanExpression(
	private val truthTable: TruthTable,
	private val dnf: DNF
) {

	fun build(): Node {
		if (dnf.isEmpty()) {
			return BooleanExpression.const(false)
		}
		return buildOrTermNode(dnf.map { buildAndTermNodes(it) })
	}

	private fun buildOrTermNode(nodes: List<Node>): Node =
		when (nodes.size) {
			0 -> throw IllegalArgumentException("no node")
			1 -> nodes.first()
			2 -> BooleanExpression.or(nodes[0], nodes[1])
			else -> BooleanExpression.or(nodes.first(), buildOrTermNode(nodes.subList(1, nodes.size)))
	}

	private fun buildAndTermNodes(literals: List<Literal>): Node {
		if (literals.isEmpty()) {
			return BooleanExpression.const(true)
		}
		return buildAndTermNode(literals.map { buildFactorNode(it) })
	}

	private fun buildAndTermNode(nodes: List<Node>): Node =
		when (nodes.size) {
			0 -> throw IllegalArgumentException("no node")
			1 -> nodes.first()
			2 -> BooleanExpression.and(nodes[0], nodes[1])
			else -> BooleanExpression.and(nodes.first(), buildAndTermNode(nodes.subList(1, nodes.size)))
		}

	private fun buildFactorNode(literal: Literal): Node {
		val name = truthTable.getColumnName(abs(literal) - 1)
		return if (literal > 0) {
			BooleanExpression.not(BooleanExpression.variable(name))
		} else {
			BooleanExpression.variable(name)
		}
	}
}