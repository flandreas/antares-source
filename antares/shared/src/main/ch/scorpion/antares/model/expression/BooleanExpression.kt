package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.dsl.*

/**
 * Boolean expressions are are defined by the following parts of the standard Antares DSL
 * as defined by [Parser]:
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

	private val constantTrue = Literal(CodeLocation.UNDEFINED, Lexer.literalToken(true))
	private val constantFalse = Literal(CodeLocation.UNDEFINED, Lexer.literalToken(false))

	fun or(left: Node, right: Node): Node =
		BinaryOperation(CodeLocation.UNDEFINED, left, Lexer.OR_TOKEN, right)

	fun and(left: Node, right: Node): Node =
		BinaryOperation(CodeLocation.UNDEFINED, left, Lexer.AND_TOKEN, right)

	fun not(node: Node): Node =
		UnaryOperation(CodeLocation.UNDEFINED, Lexer.NOT_TOKEN, node)

	fun variable(name: String): Node =
		Variable(CodeLocation.UNDEFINED, Lexer.idToken(name))

	fun const(value: Boolean): Node =
		if (value) constantTrue else constantFalse

	fun parenthesis(node: Node): Node =
		Compound(CodeLocation.UNDEFINED, listOf(node))
}