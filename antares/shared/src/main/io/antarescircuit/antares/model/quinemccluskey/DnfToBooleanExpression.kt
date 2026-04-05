package io.antarescircuit.antares.model.quinemccluskey

import io.antarescircuit.antares.model.expression.BooleanExpression
import io.antarescircuit.antares.model.expression.BooleanExpressionNotation
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.jabbah.base.dsl.Node
import io.antarescircuit.jabbah.base.module.BaseModule
import kotlin.math.abs

/**
 * Converts the output of Quine-McCluskey's [minimizeToDNF] to [BooleanExpression] reflecting
 * the right sides of assignments.
 */
class DnfToBooleanExpression(
	private val truthTable: TruthTable,
	private val dnf: DNF,
	private val andParenthesis: Boolean = BaseModule.properties.getBoolean(BooleanExpressionNotation.PROP_AND_PARENTHESIS)
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
		val node = buildAndTermNode(literals.map { buildFactorNode(it) })
		return if (andParenthesis) {
			BooleanExpression.parenthesis(node)
		} else {
			node
		}
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