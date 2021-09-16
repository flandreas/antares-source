package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.dsl.*

/**
 * Extends the grammar in [Parser] by the following productions.
 *
 * <pre>
 *     statement : super.statement
 *               | initStatement
 *     initStatement : "init" block
 * </pre>
 */
open class GraphDslParser(
	lexer: Lexer,
	semanticAnalyser: SemanticAnalyser? = SemanticAnalyser()
) : Parser(lexer, semanticAnalyser) {

	constructor(program: String): this(Lexer(program))

	override fun statement(): Node {
		return when (currentToken!!.type) {
			TokenType.INIT -> init()
			else -> super.statement()
		}
	}

	private fun init(): Node {
		eat(TokenType.INIT)
		return InitStatement(lexer.location, block())
	}
}