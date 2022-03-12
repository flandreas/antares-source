package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.dsl.Lexer.Companion.NOT_TOKEN
import ch.scorpion.jabbah.base.dsl.Lexer.Companion.OR_TOKEN
import ch.scorpion.jabbah.base.dsl.TokenType.*

/**
 * Parses sentences of the following grammar and creates a corresponding AST.
 *
 * <pre>
 *     assignmentList : assignment
 *               | assignment assignmentList
 *     assignment : variable "=" expr
 *     expr : term (orOp term)*
 *     term : factor (andOp factor)*
 *     factor : literal
 *            | "(" expr ")"
 *            | factor postfixNotOp
 *            | prefixNotOp factor
 *            | variable
 *     unaryNot: "'"
 *     orOp : "+" | "∨" | "||" | "OR"
 *     andOp : "*" | "∧ | "&&" | "AND"
 *     postfixNotOp: "'"
 *     prefixNotOp: "¬" | "!" | "NOT"
 *     literal: "0" | "1" | "true" | "false"
 *     variable : identifier
 *     identifier : LETTER (LETTER | DIGIT)*
 * </pre>
 */
class BooleanExpressionParser(
	private val expectAssignment: Boolean,
	lexer: BooleanExpressionLexer
) : AbstractBaseParser(lexer) {

	constructor(expectAssignment: Boolean, text: String): this(expectAssignment, BooleanExpressionLexer(text))

	companion object {
		private val OR_OPERATORS = setOf(PLUS, LOGIC_OR, PROGRAMMING_OR, OR)
		private val AND_OPERATORS = setOf(MULTIPLY, LOGIC_AND, PROGRAMMING_AND, AND)
		private val PREFIX_NOT_OPERATORS = setOf(LOGIC_NOT, PROGRAMMING_NOT, NOT)
		private val POSTFIX_NOT_OPERATORS = setOf(SINGLE_QUOTE)
	}

	override fun parse(): Node = if (expectAssignment) {
		Compound(lexer.location, assignmentList())
	} else {
		expr()
	}

	private fun assignmentList(): List<Node> {
		val node = assignment()
		val list = mutableListOf(node)
		while (currentToken!!.type != EOF) {
			list.add(assignment())
		}
		return list
	}

	private fun assignment(): Assignment {
		lexer.location.let { location ->
			val variable = variable()
			val op = currentToken as Token<Assignment>
			eat(ASSIGN)
			val right = expr()
			return Assignment(location, variable, op, right)
		}
	}

	private fun expr(): Node {
		var node = term()
		while (currentToken!!.type in OR_OPERATORS) {
			lexer.location.let { location ->
				eat(currentToken!!.type)
				node = BinaryOperation(location, left = node, op = OR_TOKEN, right = term())
			}
		}
		return node
	}

	private fun term(): Node {
		var node = factor()
		while (currentToken!!.type in AND_OPERATORS) {
			lexer.location.let { location ->
				eat(currentToken!!.type)
				node = BinaryOperation(location, left = node, op = Lexer.AND_TOKEN, right = factor())
			}
		}
		return node
	}

	private fun factor(): Node {
		lexer.location.let { location ->
			val token = currentToken!!

			if (currentToken!!.type in PREFIX_NOT_OPERATORS) {
				eat(currentToken!!.type)
				return UnaryOperation(location, NOT_TOKEN, factor())
			}

			val node = when (token.type) {
				LITERAL -> literal()
				LPAREN -> {
					eat(LPAREN)
					val node = expr()
					eat(RPAREN)
					node
				}
				ID -> variable()
				else -> throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", token.type.id))
			}

			if (currentToken!!.type in POSTFIX_NOT_OPERATORS) {
				eat(currentToken!!.type)
				return UnaryOperation(location, NOT_TOKEN, node)
			}

			return node
		}
	}

	private fun literal(): Node {
		val literal = Literal(lexer.location, currentToken!!)
		eat(LITERAL)
		return literal
	}

	private fun variable(): Variable = Variable(lexer.location, identifier())

	private fun identifier(): Token<String> {
		val identifier = currentToken as Token<String>
		eat(ID)
		return identifier
	}
}