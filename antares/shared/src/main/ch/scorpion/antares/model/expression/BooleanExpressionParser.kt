package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.dsl.Lexer.Companion.NOT_TOKEN
import ch.scorpion.jabbah.base.dsl.TokenType.*

/**
 * Parses sentences of the following grammar and creates a corresponding AST.
 *
 * <pre>
 *     assignmentList : assignment
 *               | assignment assignmentList
 *     assignment : variable "=" expr
 *     expr : term ("+" term)*
 *     term : factor ("*" factor)*
 *     factor : literal
 *            | "(" expr ")"
 *            | factor unaryNot
 *            | variable
 *      unaryNot: "'"
 *      literal: "0" | "1"
 *      variable : identifier
 *      identifier : LETTER (LETTER | DIGIT)*
 * </pre>
 */
class BooleanExpressionParser(
	private val expectAssignment: Boolean,
	lexer: BooleanExpressionLexer
) : AbstractBaseParser(lexer) {

	constructor(expectAssignment: Boolean, text: String): this(expectAssignment, BooleanExpressionLexer(text))

	companion object {
		private val TERM_OPERATORS = setOf(PLUS)
		private val FACTOR_OPERATORS = setOf(MULTIPLY)

	}

	override fun parse(): Node = if (expectAssignment) {
		Compound(lexer.location, assignmentList())
	} else {
		expr()
	}

	private fun assignmentList(): List<Node> {
		val node = assignment()
		val list = mutableListOf(node)
		while (currentToken!!.type != TokenType.EOF) {
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

	private fun variable(): Variable = Variable(lexer.location, identifier())

	private fun identifier(): Token<String> {
		val identifier = currentToken as Token<String>
		eat(ID)
		return identifier
	}

	private fun expr(): Node {
		var node = term()
		while (currentToken!!.type in TERM_OPERATORS) {
			lexer.location.let { location ->
				val token = when (currentToken!!.type) {
					PLUS -> Lexer.OR_TOKEN
					else -> throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", currentToken!!.type.id))
				}
				eat(currentToken!!.type)
				node = BinaryOperation(location, left = node, op = token, right = term())
			}
		}
		return node
	}

	private fun term(): Node {
		var node = factor()
		while (currentToken!!.type in FACTOR_OPERATORS) {
			lexer.location.let { location ->
				val token = when (currentToken!!.type) {
					MULTIPLY -> Lexer.AND_TOKEN
					else -> throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", currentToken!!.type.id))
				}
				eat(currentToken!!.type)
				node = BinaryOperation(location, left = node, op = token, right = factor())
			}
		}
		return node
	}

	private fun factor(): Node {
		lexer.location.let { location ->
			val token = currentToken!!
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

			return if (currentToken!!.type == SINGLE_QUOTE) {
				eat(SINGLE_QUOTE)
				UnaryOperation(location, NOT_TOKEN, node)
			} else {
				node
			}
		}
	}

	private fun literal(): Node {
		val literal = Literal(lexer.location, currentToken!!)
		eat(LITERAL)
		return literal
	}
}