package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.*
import ch.scorpion.jabbah.base.dsl.BaseTokenType.*
import ch.scorpion.jabbah.base.dsl.DslLexer.Companion.NOT_TOKEN
import ch.scorpion.jabbah.base.dsl.DslLexer.Companion.OR_TOKEN
import ch.scorpion.jabbah.base.dsl.DslTokenType.*
import ch.scorpion.jabbah.base.parser.AbstractParser
import ch.scorpion.jabbah.base.parser.Token

/**
 * Parses sentences of the following grammar and creates a corresponding AST.
 *
 * <pre>
 *     assignmentList : assignment
 *               | assignment assignmentList
 *     assignment : output "=" expr
 *     output : outputVar
 *            | prefixNotOp outputVar
 *            | outputVar postfixNotOp
 *     outputVar : ["("] variable [")"]
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
 *
 * If [singleCharIdentifier] is `true`, the following EBNF production rules apply instead:
 *
 * <pre>
 *     term : factor ([andOp] factor)*
 *     identifier : LETTER
 * </pre>
 */
class BooleanExpressionParser(
	private val expectAssignment: Boolean,
	lexer: BooleanExpressionLexer,
	private val singleCharIdentifier: Boolean = lexer.singleCharIdentifier
) : AbstractParser(lexer) {

	constructor(
		expectAssignment: Boolean,
		text: String,
		singleCharIdentifier: Boolean = false
	): this(expectAssignment, BooleanExpressionLexer(text, singleCharIdentifier))

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
			val variable = output()
			eat(ASSIGN)
			val right = expr()
			return Assignment(location, variable, right)
		}
	}

	private fun output(): Variable {

		var variable = if (currentToken!!.type in PREFIX_NOT_OPERATORS) {
			eat(currentToken!!.type)
			outputVar(negated = true)
		} else {
			outputVar()
		}

		if (currentToken!!.type in POSTFIX_NOT_OPERATORS) {
			eat(currentToken!!.type)
			variable = variable.negate()
		}

		return variable
	}

	private fun outputVar(negated: Boolean = false): Variable {
		return if (currentToken!!.type == LPAREN) {
			eat(LPAREN)
			val variable = variable(negated)
			eat(RPAREN)
			variable
		} else {
			variable(negated)
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

	private fun term(): Node =
		if (singleCharIdentifier) {
			singleCharIdentifierTerm()
		} else {
			multiCharIdentifierTerm()
		}

	private fun multiCharIdentifierTerm(): Node {
		var node = factor()
		while (currentToken!!.type in AND_OPERATORS) {
			lexer.location.let { location ->
				eat(currentToken!!.type)
				node = BinaryOperation(location, left = node, op = DslLexer.AND_TOKEN, right = factor())
			}
		}
		return node
	}

	private fun singleCharIdentifierTerm(): Node {
		val row = lexer.row
		var node = factor()

		while (currentToken!!.type in AND_OPERATORS || currentToken!!.type == ID && row == lexer.row) {
			lexer.location.let { location ->
				if (currentToken!!.type in AND_OPERATORS) {
					eat(currentToken!!.type)
				}
				node = BinaryOperation(location, left = node, op = DslLexer.AND_TOKEN, right = factor())
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

	private fun variable(negated: Boolean = false): Variable = Variable(lexer.location, identifier(), negated)

	private fun identifier(): Token<String> {
		val identifier = currentToken as Token<String>
		eat(ID)
		return identifier
	}
}