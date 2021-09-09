package ch.scorpion.antares.dsl

import ch.scorpion.antares.dsl.TokenType.*

/**
 * Parses sentences of the following grammar and creates a corresponding AST.
 *
 * <pre>
 *     statementList : statement
 *               | statement EOL (EOL)* statementList
 *     statement : expr
 *               | assignment
 *               | block
 *               | declaration
 *               | empty
 *     assignment : variable "=" expr
 *     empty : ""
 *     block : "{" (EOL)* statementList (EOL)* "}"
 *     declaration : VAR (variable | assignment)
 *     expr : term (("+" | "-") term)*
 *     term : factor (("*" | "/") factor)*
 *     factor : "+" factor
 *            | "-" factor
 *            | INTEGER
 *            | "(" expr ")"
 *            | variable
 *     variable : ID
 * </pre>
 */
class Parser(private val lexer: Lexer) {

	constructor(text: String): this(Lexer(text))

	companion object {
		private val FACTOR_OPERATORS = setOf(MULTIPLY, DIVIDE)
		private val TERM_OPERATORS = setOf(PLUS, MINUS)
	}

	/** Contains the current [Token] as determined by [Lexer.nextToken].*/
	private var currentToken: Token<Any>? = lexer.nextToken()

	/**
	 * Parses the sentence this [Parser] was created with and returns the corresponding AST.
	 * @throws SyntaxError if the sentence is syntactically invalid
	 */
	fun parse(): Node = Compound(statementList())

	private fun statementList(): List<Node> {
		val node = statement()
		val list = mutableListOf(node)
		while (currentToken!!.type == EOL) {
			eat(EOL)
			eatNewlines()
			list.add(statement())
		}
		list.lastOrNull()?.let {
			if (it is NoOp) {
				list.removeLast()
			}
		}
		return list
	}

	private fun statement(): Node {
		return when (currentToken!!.type) {
			EOF -> empty()
			ID -> {
				when (lexer.peekNextToken().type) {
					ASSIGN -> assignment()
					else -> expr()
				}
			}
			LCURLEY -> block()
			RCURLEY -> empty()
			VAR -> declaration()
			else -> expr()
		}
	}

	private fun assignment(): Assignment {
		val left = variable()
		val token = currentToken as Token<Assignment>
		eat(ASSIGN)
		val right = expr()
		return Assignment(left, token, right)
	}

	private fun variable(): Variable {
		val node = Variable(currentToken as Token<String>)
		eat(ID)
		return node
	}

	private fun block(): Node {
		eat(LCURLEY)
		eatNewlines()
		val statementList = statementList()
		eatNewlines()
		eat(RCURLEY)
		eatNewlines()
		return Compound(statementList)
	}

	private fun declaration(): Node {
		eat(VAR)
		return if (lexer.peekNextToken().type == ASSIGN) {
			val assignment = assignment()
			Declaration(assignment.left, assignment.right)
		} else {
			Declaration(variable(), null)
		}
	}

	private fun empty(): Node = NoOp()

	private fun expr(): Node {
		var node = term()
		while (currentToken!!.type in TERM_OPERATORS) {
			val token = currentToken!!
			when (token.type) {
				PLUS -> eat(PLUS)
				MINUS -> eat(MINUS)
				else -> throw SyntaxError("Unexpected token ${token.type.name} at ${lexer.currentLocation}")
			}
			node = BinaryOperation(left = node, op = token, right = term())
		}
		return node
	}

	private fun term(): Node {
		var node = factor()
		while (currentToken!!.type in FACTOR_OPERATORS) {
			val token = currentToken!!
			when (token.type) {
				MULTIPLY -> eat(MULTIPLY)
				DIVIDE -> eat(DIVIDE)
				else -> throw SyntaxError("Unexpected token ${token.type.name} at ${lexer.currentLocation}")
			}
			node = BinaryOperation(left = node, op = token, right = factor())
		}
		return node
	}

	private fun factor(): Node {
		val token = currentToken!!
		return when (token.type) {
			PLUS -> {
				eat(PLUS)
				return UnaryOperation(token, factor())
			}
			MINUS -> {
				eat(MINUS)
				return UnaryOperation(token, factor())
			}
			INTEGER -> {
				eat(INTEGER)
				return Number(token as Token<Int>)
			}
			LPAREN -> {
				eat(LPAREN)
				val node = expr()
				eat(RPAREN)
				node
			}
			ID -> {
				variable()
			}
			else -> throw SyntaxError("Unexpected token ${token.type.name} at ${lexer.currentLocation}")
		}
	}

	/**
	 * Compares the current [TokenType] with the passed [TokenType] and, if they match,
	 * then "eats" the current [Token] and assigns the next [Token] to [currentToken],
	 * otherwise throws [SyntaxError].
	 */
	private fun eat(type: TokenType) {
		if (currentToken!!.type == type) {
			currentToken = lexer.nextToken()
		} else {
			throw SyntaxError("Expected ${type.name} at ${lexer.currentLocation}")
		}
	}

	private fun eatNewlines() {
		while (currentToken != null && currentToken!!.type == EOL) {
			eat(EOL)
		}
	}
}