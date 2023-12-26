package ch.scorpion.jabbah.base.dsl

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.dsl.BaseTokenType.*
import ch.scorpion.jabbah.base.dsl.DslTokenType.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.parser.AbstractParser
import ch.scorpion.jabbah.base.parser.Parser
import ch.scorpion.jabbah.base.parser.Token

/**
 * Creates a [Parser] for parsing a program text.
 * @param semanticAnalyser the optional [SemanticAnalyser], or `null` if either no semantic analysis
 * is to be done, or the [ParserFactory] decides to (and insists upon) applying a particular
 * [SemanticAnalyser] implementation.
 */
typealias ParserFactory = (program: String, semanticAnalyser: SemanticAnalyser?) -> Parser

/**
 * Parses sentences of the following grammar and creates a corresponding AST.
 *
 * ```
 *     statementList : statement
 *               | statement statementList
 *     statement : expr
 *               | assignment
 *               | block
 *               | declaration
 *               | ifStatement
 *               | whenStatement
 *               | forStatement
 *               | returnStatement
 *               | empty
 *     assignment : variable "=" expr
 *     empty : ""
 *     block : "{" statementList "}"
 *     declaration : varDeclaration | storeDeclaration
 *     varDeclaration : "var" (variable | assignment)
 *     storeDeclaration : "store" (variable | assignment)
 *     ifStatement : "if" "(" expr ")" statement [ "else" statement ]
 *     whenStatement : "when" "(" expr ")" "{" ( whenThen )* [ whenElse ] "}"
 *     whenThen : expr ":" statement
 *     whenElse : "else" ":" statement
 *     forStatement : "for" "(" variable "in" expr "to" expr ")" statement
 *     returnStatement : "return" [ expr ]
 *     functionCall : identifier "(" { expr ("," expr)* } ")"
 *     expr : term (("+" | "-" | binaryLogicOperator) term)*
 *     term : factor (("*" | "/" | "%" | "^" | comparisonOperator | shiftOperator) factor)*
 *     comparisonOperator : "==" | "!=" | "<" | ">" | "<=" | ">="
 *     factor : "+" factor
 *            | "-" factor
 *            | "not" factor
 *            | literal
 *            | "(" expr ")"
 *            | variable
 *            | procedureCall
 *     binaryLogicOperator : "and" | "or"
 *     shiftOperator : "<<" | ">>"
 *     literal : number | string
 *     number : LONG | FLOAT
 *     string : """ { CHAR }"""
 *     variable : identifier | assocArray
 *     assocArray : identifier "[" expr "]"
 *     identifier : LETTER (LETTER | DIGIT)* | "'" CHAR (CHAR)* "'"
 * ```
 *
 * @property lexer the [DslLexer] set up with the program code to scan
 * @property semanticAnalyser the [HierarchyVisitor] to perform semantic analysis. Provide
 * [EmptyHierarchyVisitor] to skip semantic analysis
 */
open class DslParser(
	lexer: DslLexer,
	private val semanticAnalyser: SemanticAnalyser? = BaseModule.semanticAnalyserFactory(null)
) : AbstractParser(lexer) {

	constructor(program: String): this(DslLexer(program))

	companion object {
		private val BINARY_LOGIC_OPERATORS = setOf(AND, OR)
		private val COMPARISON_OPERATORS = setOf(EQUAL, DIFF, SMALLER, GREATER, SMALLER_EQUAL, GREATER_EQUAL)
		private val SHIFT_OPERATORS = setOf(SHIFT_LEFT, SHIFT_RIGHT)
		private val FACTOR_OPERATORS = setOf(MULTIPLY, DIVIDE, MOD, CARET) + COMPARISON_OPERATORS + SHIFT_OPERATORS
		private val TERM_OPERATORS = setOf(PLUS, MINUS) + BINARY_LOGIC_OPERATORS
	}

	override fun parse(): Node = Compound(lexer.location, statementList()).also { semanticAnalyser?.analyse(it) }

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

	protected open fun statement(): Node {
		return when (currentToken!!.type) {
			EOF -> empty()
			ID -> lookAheadFromId()
			LCURLEY -> block()
			RCURLEY -> empty()
			IF -> ifStatement()
			WHEN -> whenStatement()
			FOR -> forStatement()
			RETURN -> returnStatement()
			VAR -> varDeclaration()
			STORE -> storeDeclaration()
			else -> expr()
		}
	}

	protected open fun lookAheadFromId(): Node =
		when (lexer.peekNextToken().type) {
			ASSIGN -> assignment()
			LEFT_BRACKET -> lookAheadFromLeftBracket()
			else -> expr()
		}

	private fun lookAheadFromLeftBracket(): Node {
		val assocArray = assocArray()
		return if (currentToken!!.type == ASSIGN) {
			assignment(assocArray)
		} else {
			assocArray
		}
	}

	private fun assignment(): Assignment = assignment(variable())

	protected fun assignment(variable: Variable): Assignment {
		lexer.location.let { location ->
			eat(ASSIGN)
			val right = expr()
			return Assignment(location, variable, right)
		}
	}

	protected fun identifier(): Token<String> {
		@Suppress("UNCHECKED_CAST")
		val identifier = currentToken as Token<String>
		eat(ID)
		return identifier
	}

	protected open fun variable(): Variable {
		return when (lexer.peekNextToken().type) {
			LEFT_BRACKET -> assocArray()
			else -> Variable(lexer.location, identifier())
		}
	}

	private fun assocArray(): AssocArray {
		lexer.location.let { location ->
			val variable = identifier()
			eat(LEFT_BRACKET)
			val expr = expr()
			eat(RIGHT_BRACKET)
			return AssocArray(location, variable, expr)
		}
	}

	protected fun block(): Block {
		lexer.location.let { location ->
			eat(LCURLEY)
			val statementList = statementList()
			eat(RCURLEY)
			return Block(location, statementList)
		}
	}

	private fun varDeclaration(): Node = declaration(VAR, store = false)

	private fun storeDeclaration(): Node = declaration(STORE, store = true)

	private fun declaration(tokenType: DslTokenType, store: Boolean): Node {
		lexer.location.let { location ->
			eat(tokenType)
			return if (lexer.peekNextToken().type == ASSIGN) {
				val assignment = assignment()
				Declaration(location, assignment.left, assignment.right, store = store)
			} else {
				Declaration(location, variable(), null, store = store)
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

	private fun whenStatement(): Node {
		lexer.location.let { location ->
			val clauses = mutableListOf<WhenClause>()
			eat(WHEN)
			eat(LPAREN)
			val expr = expr()
			eat(RPAREN)
			eat(LCURLEY)
			while (currentToken!!.type != EOF && currentToken!!.type != RCURLEY) {
				if (currentToken!!.type == ELSE) {
					clauses.add(whenElse())
					break
				} else {
					clauses.add(whenThen())
				}
			}
			eat(RCURLEY)

			return WhenStatement(location, expr, clauses)
		}
	}

	private fun whenThen(): WhenClause {
		lexer.location.let { location ->
			val condition = expr()
			eat(COLON)
			return WhenClause(location, condition, statement())
		}
	}

	private fun whenElse(): WhenClause {
		lexer.location.let { location ->
			eat(ELSE)
			eat(COLON)
			return WhenClause(location, null, statement())
		}
	}

	private fun forStatement(): Node {
		lexer.location.let { location ->
			eat(FOR)
			eat(LPAREN)
			val variable = variable()
			eat(IN)
			val inExpr = expr()
			eat(TO)
			val toExpr = expr()
			eat(RPAREN)
			val statement = statement()
			return ForStatement(location, variable, inExpr, toExpr, statement)
		}
	}

	private fun returnStatement(): Node {
		lexer.location.let { location ->
			eat(RETURN)
			return ReturnStatement(location, if (isExpr()) expr() else null)
		}
	}

	private fun functionCall(): Node {
		lexer.location.let { location ->
			val name = identifier()
			val params = mutableListOf<Node>()
			eat(LPAREN)
			if (currentToken!!.type != RPAREN) {
				params.add(expr())
			}
			while (currentToken!!.type == COMMA) {
				eat(COMMA)
				params.add(expr())
			}
			eat(RPAREN)

			return FunctionCall(location, name, params)
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
					throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", token.type.id))
				}

				node = BinaryOperation(location, left = node, op = token, right = term())
			}
		}
		return node
	}

	private fun isExpr(): Boolean = isTerm()

	private fun term(): Node {
		var node = factor()
		while (currentToken!!.type in FACTOR_OPERATORS) {
			lexer.location.let { location ->
				val token = currentToken!!
				if (FACTOR_OPERATORS.contains(token.type)) {
					eat(token.type)
				} else {
					throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", token.type.id))
				}
				node = BinaryOperation(location, left = node, op = token, right = factor())
			}
		}
		return node
	}

	private fun isTerm(): Boolean = isFactor()

	protected open fun factor(): Node {
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
				LITERAL, DOUBLE_QUOTE -> literal()
				LPAREN -> {
					eat(LPAREN)
					val node = expr()
					eat(RPAREN)
					node
				}
				ID -> {
					if (lexer.peekNextToken().type == LPAREN) {
						functionCall()
					} else {
						variable()
					}
				}
				else -> throw SyntaxError(location, Translations.getString("base.dsl.unexpectedToken.msg", token.type.id))
			}
		}
	}

	protected open fun isFactor(): Boolean {
		return when (currentToken!!.type) {
			PLUS -> true
			MINUS -> true
			NOT -> true
			LITERAL -> true
			LPAREN -> true
			ID -> true
			else -> false
		}
	}

	protected open fun literal(): Literal {
		return when (currentToken!!.type) {
			DOUBLE_QUOTE -> string()
			else -> number()
		}
	}

	private fun number(): Literal {
		val literal = Literal(lexer.location, currentToken!!)
		eat(LITERAL)
		return literal
	}

	private fun string(): Literal {
		eat(DOUBLE_QUOTE)
		val string = Literal(lexer.location, currentToken!!)
		eat(ID)
		eat(DOUBLE_QUOTE)
		return string
	}
}