package io.antarescircuit.antares.model.expression

import io.antarescircuit.jabbah.base.dsl.*
import io.antarescircuit.jabbah.base.parser.TextLocation

/**
 * Boolean expressions are defined by the following parts of the standard Antares DSL
 * as defined by [DslParser]:
 *
 * <pre>
 *     expr : term ("or" term)*
 *     term : factor ("and"  factor)*
 *     factor : "not" factor
 *            | literal
 *            | "(" expr ")"
 *            | variable
 *     literal : "true" | "false"
 * </pre>
 */
object BooleanExpression {

	private val constantTrue = Literal(TextLocation.UNDEFINED, BaseLexer.literalToken(true))
	private val constantFalse = Literal(TextLocation.UNDEFINED, BaseLexer.literalToken(false))

	fun or(left: Node, right: Node): Node =
		BinaryOperation(TextLocation.UNDEFINED, left, DslLexer.OR_TOKEN, right)

	fun and(left: Node, right: Node): Node =
		BinaryOperation(TextLocation.UNDEFINED, left, DslLexer.AND_TOKEN, right)

	fun not(node: Node): Node =
		UnaryOperation(TextLocation.UNDEFINED, DslLexer.NOT_TOKEN, node)

	fun variable(name: String): Node =
		Variable(TextLocation.UNDEFINED, BaseLexer.idToken(name))

	fun const(value: Boolean): Node =
		if (value) constantTrue else constantFalse

	fun parenthesis(node: Node): Node =
		Compound(TextLocation.UNDEFINED, listOf(node))
}