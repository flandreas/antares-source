package ch.scorpion.antares.dsl

import ch.scorpion.antares.dsl.TokenType.*

/**
 * Parses sentences of the following grammar and creates a corresponding AST.
 *
 * <pre>
 *     statementList : statement
 *               | statement statementList
 *     statement : expr
 *               | assignment
 *               | block
 *               | declaration
 *               | ifStatement
 *               | empty
 *     assignment : variable "=" expr
 *     empty : ""
 *     block : "{" statementList "}"
 *     declaration : "var" (variable | assignment)
 *     ifStatement : "if" "(" expr ")" statement [ "else" statement ]
 *     expr : term (("+" | "-" | binaryLogicOperator) term)*
 *     term : factor (("*" | "/" | "%" | comparisonOperator | shiftOperator) factor)*
 *     comparisonOperator : "==" | "!=" | "<" | ">"
 *     factor : "+" factor
 *            | "-" factor
 *            | "not"
 *            | number
 *            | "(" expr ")"
 *            | variable
 *     binaryLogicOperator : "and" | "or"
 *     shiftOperator : "<<" | ">>"
 *     number : INTEGER | hexLiteral
 *     hexLiteral : "0x" INTEGER
 *     variable : ID
 * </pre>
 */
class Parser(private val lexer: Lexer) {

	constructor(text: String): this(Lexer(text))

	companion object {
		private val BINARY_LOGIC_OPERATORS = setOf(AND, OR)
		private val COMPARISON_OPERATORS = setOf(EQUAL, DIFF, SMALLER, GREATER, SMALLER_EQUAL, GREATER_EQUAL)
		private val SHIFT_OPERATORS = setOf(SHIFT_LEFT, SHIFT_RIGHT)
		private val FACTOR_OPERATORS = setOf(MULTIPLY, DIVIDE, MOD) + COMPARISON_OPERATORS + SHIFT_OPERATORS
		private val TERM_OPERATORS = setOf(PLUS, MINUS) + BINARY_LOGIC_OPERATORS
	}

	/** Contains the current [Token] as determined by [Lexer.nextToken].*/
	private var currentToken: Token<Any>? = lexer.nextToken()

	/**
	 * Parses the sentence this [Parser] was created with and returns the corresponding AST.
	 * @throws SyntaxError if the sentence is syntactically invalid
	 */
	fun parse(): Node {
		return Compound(lexer.location, statementList()).also {
			// Semantic analysis
			it.accept(SemanticAnalyser())
		}
	}

	private fun statementList(): List<Node> {
		val node = statement()
		val list = mutableListOf(node)
		while (currentToken!!.type != EOF && currentToken!!.type != RCURLEY) {
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
			IF -> ifStatement()
			VAR -> declaration()
			else -> expr()
		}
	}

	private fun assignment(): Assignment {
		lexer.location.let { location ->
			val left = variable()
			val token = currentToken as Token<Assignment>
			eat(ASSIGN)
			val right = expr()
			return Assignment(location, left, token, right)
		}
	}

	private fun variable(): Variable {
		val node = Variable(lexer.location, currentToken as Token<String>)
		eat(ID)
		return node
	}

	private fun block(): Node {
		lexer.location.let { location ->
			eat(LCURLEY)
			val statementList = statementList()
			eat(RCURLEY)
			return Block(location, statementList)
		}
	}

	private fun declaration(): Node {
		lexer.location.let { location ->
			eat(VAR)
			return if (lexer.peekNextToken().type == ASSIGN) {
				val assignment = assignment()
				Declaration(location, assignment.left, assignment.right)
			} else {
				Declaration(location, variable(), null)
			}
		}
	}

	private fun ifStatement(): Node {
		lexer.location.let { location ->
			eat(IF)
			eat(LPAREN)
			val condition = expr()
			eat(RPAREN)
			val thenStatement = statement()
			var elseStatement: Node? = null
			if (currentToken!!.type == ELSE) {
				eat(ELSE)
				elseStatement = statement()
			}
			return IfStatement(location, condition, thenStatement, elseStatement)
		}
	}

	private fun empty(): Node = NoOp(lexer.location)

	private fun expr(): Node {
		var node = term()
		while (currentToken!!.type in TERM_OPERATORS) {
			lexer.location.let { location ->
				val token = currentToken!!
				if (TERM_OPERATORS.contains(token.type)) {
					eat(token.type)
				} else {
					throw SyntaxError(location, "Unexpected token ${token.type.name}")
				}

				node = BinaryOperation(location, left = node, op = token, right = term())
			}
		}
		return node
	}

	private fun term(): Node {
		var node = factor()
		while (currentToken!!.type in FACTOR_OPERATORS) {
			lexer.location.let { location ->
				val token = currentToken!!
				if (FACTOR_OPERATORS.contains(token.type)) {
					eat(token.type)
				} else {
					throw SyntaxError(location, "Unexpected token ${token.type.name}")
				}
				node = BinaryOperation(location, left = node, op = token, right = factor())
			}
		}
		return node
	}

	private fun factor(): Node {
		lexer.location.let { location ->
			val token = currentToken!!
			return when (token.type) {
				PLUS -> {
					eat(PLUS)
					return UnaryOperation(location, token, factor())
				}
				MINUS -> {
					eat(MINUS)
					return UnaryOperation(location, token, factor())
				}
				NOT -> {
					eat(NOT)
					return UnaryOperation(location, token, factor())
				}
				LONG -> {
					eat(LONG)
					return Number(location, token as Token<Long>)
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
				else -> throw SyntaxError(location, "Unexpected token ${token.type.name}")
			}
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
			throw SyntaxError(lexer.location, "Expected ${type.name}")
		}
	}
}